package workshop.part4;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import org.junit.Test;

public class HttpRoutesTest extends JUnitRouteTest {

    @Test
    public void testRoute() {

        TestRoute appRoute = testRoute(new HttpRoutes().createRoutes());

        // test happy path
        appRoute.run(HttpRequest.GET("/"))
                .assertStatusCode(404);
    }

}
