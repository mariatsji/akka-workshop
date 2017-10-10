package workshop.part4;

import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserService;
import workshop.part2.VettingActorFactory;
import workshop.part2.VettingSupervisor;
import workshop.part3.FraudWordActor;
import workshop.part3.UserActor;

public class AkkaHttpServer {

    public static final String HOST_BINDING = "localhost";
    public static final int PORT = 8080;

    public static void main(String[] args) {
        AkkaHttpServer app = new AkkaHttpServer();

        app.start(ActorSystem.create("routes"));
    }

    private void start(ActorSystem system) {
        ActorRef userActor = system.actorOf(Props.create(UserActor.class, () -> new UserActor(new UserService())));
        ActorRef fraudWordActor = system.actorOf(Props.create(FraudWordActor.class, () -> new FraudWordActor(new FraudWordService())));

        ActorRef vettingSupervisor = system.actorOf(Props.create(VettingSupervisor.class, () -> new VettingSupervisor(new VettingActorFactory(userActor, fraudWordActor))));

        final Http http = Http.get(system);

        final ActorMaterializer materializer = ActorMaterializer.create(system);

        Flow<HttpRequest, HttpResponse, NotUsed> flow = new HttpRoutes(vettingSupervisor, new VerdictCache()).registerRoutes().flow(system, materializer);

        CompletionStage<ServerBinding> binding = http.bindAndHandle(flow, ConnectHttp.toHost(
                HOST_BINDING, PORT), materializer);

        System.out.println(String.format("Server online at http://%s:%d/", HOST_BINDING, PORT));

        sleepForSeconds(24 * 60 * 60);

        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

    private static void sleepForSeconds(int seconds) {
        try { Thread.sleep(seconds * 1000); } catch (Exception i) { }
    }

}
