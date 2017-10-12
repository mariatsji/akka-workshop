package workshop.part4;

import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;
import scala.reflect.ClassTag$;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

import static akka.http.javadsl.server.PathMatchers.integerSegment;

class HttpRoutes extends AllDirectives {

    private final ActorRef vettingActor;
    private final VerdictCache cache;

    public HttpRoutes(ActorRef vettingActor, VerdictCache cache) {
        this.vettingActor = vettingActor;
        this.cache = cache;
    }

    /**
     * Dont forget to register your route here - otherwise you will be pondering 404s!
     */
    public Route registerRoutes() {
        return route(
                root(),
                verdict(),
                evaluate(),
                error()
        );
    }

    private Route root() {
        return get(() ->
                path("", () -> complete(StatusCodes.NOT_FOUND)));
    }

    private Route error() {
        return post(() ->
                path("evaluate", () ->
                        entity(Jackson.unmarshaller(Ad.class), ad ->
                                completeOK(new Verdict(String.valueOf(ad.getAdId()), Verdict.VerdictType.FAILURE), Jackson.marshaller()))));
    }

    private Route evaluate() {
        return post(() ->
                path("evaluate", () ->
                        entity(Jackson.unmarshaller(Ad.class), ad ->
                                cache.get(ad)
                                .map(a -> verdict())
                                .getOrElse(() -> completeOrRecoverWith(
                                        () -> doVetting(ad),
                                        Jackson.marshaller(),
                                        t -> error()))
                        ))
        );
    }

    private Route verdict() {
        return get(() ->
                pathPrefix("evaluate", () ->
                        path(integerSegment(), adId -> cache.get(adId)
                                .map(verdict -> complete(StatusCodes.OK, verdict, Jackson.marshaller()))
                                .getOrElse(() -> complete(StatusCodes.NOT_FOUND, String.format("No such vetting: %d", adId)))
                        )));
    }

    private CompletionStage<Verdict> doVetting(final Ad ad) {

        String verdictId = String.valueOf(ad.getAdId());

        Future<Verdict.VerdictType> actorVerdict = Patterns.ask(vettingActor, ad, 1000)
                .mapTo(ClassTag$.MODULE$.apply(Verdict.VerdictType.class));

        CompletionStage<Verdict.VerdictType> vettingVerdict = FutureConverters.toJava(actorVerdict);

        return vettingVerdict.whenCompleteAsync((Verdict.VerdictType vt, Throwable error) -> {
            if (error != null) {
                vettingVerdict.thenRunAsync(() -> new Verdict(verdictId, Verdict.VerdictType.FAILURE));
            } else {
                vettingVerdict.thenRunAsync(() -> {
                    Verdict verdict = new Verdict(verdictId, vt);
                    cache.put(ad, verdict);
                });
            }
        }).thenApply(vt -> new Verdict(verdictId, vt));

    }

}
