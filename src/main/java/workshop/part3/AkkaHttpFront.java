package workshop.part3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import javaslang.control.Option;
import scala.concurrent.Future;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserService;
import workshop.part1.Verdict;
import workshop.part2.FraudWordActor;
import workshop.part2.UserActor;
import workshop.part2.supervisor.VettingActorFactory;
import workshop.part2.supervisor.VettingSupervisor;

import static akka.http.javadsl.server.PathMatchers.longSegment;

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

    private Route createRoute() {

        return route(
                get(() ->
                        pathPrefix("verdict", () ->
                                path(longSegment(), (Long id) -> {
                                    final CompletionStage<Option<Verdict>> futureMaybeVerdict = fetchItem(id);
                                    return onSuccess(() -> futureMaybeVerdict, maybeVerdict ->
                                            maybeVerdict.map(verdict -> completeOK(verdict, Jackson.marshaller()))
                                                    .getOrElse(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                }))),
                post(() ->
                        path("evaluate", () ->
                            entity(Jackson.unmarshaller(Ad.class), ad -> {
                                CompletionStage<Verdict> futureVetted = performVetting(ad);
                                return onSuccess(() -> futureVetted, verdict -> completeOK(verdict, Jackson.marshaller()));
                            })))
        );
    }

    // (fake) async database query api
    private CompletionStage<Option<Verdict>> fetchItem(long itemId) {
        return CompletableFuture.completedFuture(Option.of(new Verdict(String.valueOf(itemId), Verdict.VerdictType.PENDING)));
    }

    // (fake) async database query api
    private CompletionStage<Verdict> performVetting(final Ad ad) {
        Future<Object> ask = Patterns.ask(vettingSupervisor, ad, 1000);//todo

        return CompletableFuture.completedFuture(new Verdict(String.valueOf(ad.getAdId()), Verdict.VerdictType.GOOD));
    }


}
