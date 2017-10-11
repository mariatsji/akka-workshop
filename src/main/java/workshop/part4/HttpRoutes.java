package workshop.part4;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

class HttpRoutes extends AllDirectives {

    private final ActorRef vettingActor;
    private final VerdictCache cache;

    public HttpRoutes(ActorRef vettingActor, VerdictCache cache) {
        this.vettingActor = vettingActor;
        this.cache = cache;
    }

    public Route registerRoutes() {
        return route(
                root(),
                verdict(),
                evaluate()
        );
    }

    private Route root() {
        return get(() ->
                path("", () -> complete(StatusCodes.NOT_FOUND)));
    }

    // GET to /verdict/<numeric id> should result in the verdict of the ad with that adId e.g.
    // GET /verdict/2 -> {"id": "2", "value": "GOOD"}
    private Route verdict() {
        return root();//todo
    }

    // POST to /evaluate with a json-representation of an Ad.class :
    // {"adId": "2", "userId": "99999", "title": "fin sofa selges", "description": "brukt noen år da, si"}
    // should result in a json representation of Verdict.class :
    // {"id": "2", "value": "GOOD"}
    private Route evaluate() {
        return root(); //todo
    }

    // do actual vetting using a VettingActor and Futures here
    private CompletionStage<Verdict> doVetting(final Ad ad) {
        return CompletableFuture.supplyAsync(() -> new Verdict("todo", Verdict.VerdictType.PENDING));
    }

}
