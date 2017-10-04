package workshop.part4;

import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import javaslang.Function1;
import scala.concurrent.Future;
import scala.reflect.ClassTag$;
import workshop.common.Utils;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

class HttpRoutes extends AllDirectives {

    private final ActorRef vettingActor;

    public HttpRoutes(ActorRef vettingActor) {
        this.vettingActor = vettingActor;
    }

    /**
     * Dont forget to register your route - otherwise you will be pondering 404s!
     * @return
     */
    Route registerRoutes() {
        return route(
                root(),
                evaluate(),
                error()
        );
    }

    Route root() {
        return get(() ->
                        path("", () -> complete(StatusCodes.NOT_FOUND)));
    }

    Route error() {
        return post(() ->
                        path("evaluate", () ->
                                entity(Jackson.unmarshaller(Ad.class), ad ->
                                        completeOK(new Verdict(String.valueOf(ad.getAdId()), Verdict.VerdictType.FAILURE), Jackson.marshaller()))));
    }

    Route evaluate() {
        Function1<Ad, CompletionStage<Verdict>> vettingFunction = this::pendingVetting;
        Function1<Ad, CompletionStage<Verdict>> memoizedVettingFunction = vettingFunction.memoized();

        return post(() ->
                path("evaluate", () ->
                        entity(Jackson.unmarshaller(Ad.class), ad -> completeOrRecoverWith(
                                () -> memoizedVettingFunction.apply(ad),
                                Jackson.marshaller(),
                                t -> error()
                        )))
        );
    }

    private CompletionStage<Verdict> pendingVetting(final Ad ad) {

        String verdictId = String.valueOf(ad.getAdId());

        Future<Verdict.VerdictType> actorVerdict = Patterns.ask(vettingActor, ad, 1000)
                .mapTo(ClassTag$.MODULE$.apply(Verdict.VerdictType.class));

        CompletionStage<Verdict.VerdictType> vettingVerdict = Utils.toJavaFuture(actorVerdict);

        return vettingVerdict.whenCompleteAsync((Verdict.VerdictType vt, Throwable error) -> {
            if (error != null) {
                vettingVerdict.thenRunAsync(() -> new Verdict(verdictId, Verdict.VerdictType.FAILURE));
            } else {
                vettingVerdict.thenRunAsync(() -> new Verdict(verdictId, vt));
            }
        }).thenApply(vt -> new Verdict(verdictId, vt));

    }

}
