package workshop.part3;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import org.junit.Test;

public class AkkaHttpFrontTest extends JUnitRouteTest {

    TestRoute appRoute = testRoute(new AkkaHttpFront().createRoute());

    @Test
    public void testRoute() {
        // test happy path
        appRoute.run(HttpRequest.POST("/evaluate"))
                .assertStatusCode(200);
    }

}
