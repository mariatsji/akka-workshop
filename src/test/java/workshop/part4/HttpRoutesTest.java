package workshop.part4;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.TestProbe;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Option;
import org.junit.Test;
import workshop.common.ad.Ad;
import workshop.part1.Verdict;

public class HttpRoutesTest extends JUnitRouteTest {

    private ActorSystem system = ActorSystem.create();
    private TestProbe dummyActor = TestProbe.apply(system);
    private TestRoute appRoute = testRoute(new HttpRoutes(dummyActor.ref(), new TestVerdictCache()).registerRoutes());

    @Test
    public void rootGives404() {
        appRoute.run(HttpRequest.GET("/"))
                .assertStatusCode(StatusCodes.NOT_FOUND);
    }

    @Test
    public void postAdToEvaluateWithUnfamiliarJsonShouldGive400() {
        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, "{\"fisk\":2}"))
                .assertStatusCode(StatusCodes.BAD_REQUEST);
    }

    @Test
    public void postAdToEvaluateWithoutLiveActorsBackendGives200() throws JsonProcessingException {
        Ad ad = new Ad(999, "fin sofa", "lite brukt");
        String adJson = new ObjectMapper()
                .writeValueAsString(ad);

        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, adJson))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"" + ad.getAdId() + "\",\"value\":\"FAILURE\"}");
    }

    @Test
    public void getVettingResultFromUnknownAdShouldGive404() {
        appRoute.run(HttpRequest.GET("/evaluate/42"))
                .assertStatusCode(StatusCodes.NOT_FOUND);
    }

    @Test
    public void getVettingResultFromPreviouslyPostedAdShouldGiveVerdict() {
        appRoute.run(HttpRequest.GET("/evaluate/43"))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"43\",\"value\":\"BAD\"}");
    }

    private class TestVerdictCache extends VerdictCache {

        public Option<Verdict> get(Integer adId) {
            if (adId == 43) {
                return Option.of(new Verdict("43", Verdict.VerdictType.BAD));
            } else {
                return Option.none();
            }
        }

    }
}
