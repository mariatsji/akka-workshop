package workshop.part3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.Futures;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.reflect.ClassTag;
import workshop.common.Utils;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserService;
import workshop.part1.Verdict;
import workshop.part2a.VettingActorFactory;
import workshop.part2a.VettingSupervisor;
import workshop.part2b.FraudWordActor;
import workshop.part2b.UserActor;

public class AkkaHttpFront extends AllDirectives {

    public static final String HOST_BINDING = "localhost";
    public static final int PORT = 8080;

    private ActorRef vettingSupervisor;

    public static void main(String[] args) {
        AkkaHttpFront app = new AkkaHttpFront();

        app.start();
    }

    private void start() {
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("routes");

        ActorRef userActor = system.actorOf(Props.create(UserActor.class, () -> new UserActor(new UserService())));
        ActorRef fraudWordActor = system.actorOf(Props.create(FraudWordActor.class, () -> new FraudWordActor(new FraudWordService())));
        vettingSupervisor = system.actorOf(Props.create(VettingSupervisor.class, () -> new VettingSupervisor(new VettingActorFactory(userActor, fraudWordActor))));

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        Flow<HttpRequest, HttpResponse, NotUsed> flow = createRoute().flow(system, materializer);

        CompletionStage<ServerBinding> binding = http.bindAndHandle(flow, ConnectHttp.toHost(
                HOST_BINDING, PORT), materializer);

        System.out.println(String.format("Server online at http://%s:%d/", HOST_BINDING, PORT));

        sleepForSeconds(1000);

        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

    private static void sleepForSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception i) {
        }
    }

    Route createRoute() {
        return route(
                post(() ->
                        path("evaluate", () ->
                            entity(Jackson.unmarshaller(Ad.class), ad -> {

                                Future<Verdict.VerdictType> verdictTypeFuture = performVetting(ad);
                                return onSuccess(
                                        () -> Utils.toJavaFuture(verdictTypeFuture),
                                        verdict -> completeOK(verdict, Jackson.marshaller()));
                            })))
        );
    }

    // (fake) async database query api
    private Future<Verdict.VerdictType> performVetting(final Ad ad) {
        return Patterns.ask(vettingSupervisor, ad, 1000)
                .mapTo(ClassTag.apply(Verdict.VerdictType.class));
    }




}
