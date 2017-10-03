package workshop.part4;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import javaslang.control.Option;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

class HttpRoutes extends AllDirectives {

    Route createRoutes() {
        return route(
                defaultRoute(),
                evaluate()
        );
    }

    Route defaultRoute() {
        return
                get(() ->
                        path("/", () -> complete(StatusCodes.NOT_FOUND, "Not Found")));
    }

    Route evaluate() {
        return post(() ->
                path("evaluate", () ->
                        entity(Jackson.unmarshaller(Ad.class), ad -> completeOrRecoverWith(
                                () -> performVetting(ad),
                                Jackson.marshaller(),
                                t -> defaultRoute()
                        )))
        );
    }

    // (fake) async database query api
    private CompletionStage<Verdict> performVetting(final Ad ad) {
        Option<Verdict> verdict = VettingRepository.getVerdict(ad);

        String vettingId = verdict
                .map(Verdict::getId)
                .getOrElse(IDProvider::nextId);

        return CompletableFuture.supplyAsync(() -> verdict.getOrElse(new Verdict(vettingId, Verdict.VerdictType.PENDING)));
    }

}
