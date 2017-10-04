package workshop.part4;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.TestProbe;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import workshop.common.ad.Ad;

public class HttpRoutesTest extends JUnitRouteTest {

    private ActorSystem system = ActorSystem.create();
    private TestProbe sender = TestProbe.apply(system);
    private TestRoute appRoute = testRoute(new HttpRoutes(sender.ref()).registerRoutes());


    @Test
    public void rootGives404() {
        appRoute.run(HttpRequest.GET("/"))
                .assertStatusCode(404);
    }

    @Test
    public void postAdToEvaluateWithoutActualBackendGives200() throws JsonProcessingException {
        String adJson = new ObjectMapper()
                .writeValueAsString(new Ad(999, "fin sofa", "lite brukt"));

        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, adJson))
                .assertStatusCode(200);
    }



}
