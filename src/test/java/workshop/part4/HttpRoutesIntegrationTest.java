package workshop.part4;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserService;
import workshop.part2a.VettingActorFactory;
import workshop.part2a.VettingSupervisor;
import workshop.part2b.FraudWordActor;
import workshop.part2b.UserActor;

public class HttpRoutesIntegrationTest extends JUnitRouteTest {

    private ActorSystem system = ActorSystem.create();

    private ActorRef userActor;
    private ActorRef fraudWordActor;
    private ActorRef vettingSupervisor;

    @Before
    public void setupActors() {
        userActor = system.actorOf(Props.create(UserActor.class, () -> new UserActor(new UserService())));
        fraudWordActor = system.actorOf(Props.create(FraudWordActor.class, () -> new FraudWordActor(new FraudWordService())));

        vettingSupervisor = system.actorOf(Props.create(VettingSupervisor.class, () -> new VettingSupervisor(new VettingActorFactory(userActor, fraudWordActor))));
    }

    @Test
    public void shouldGetRealVettingResultVettingOKWhenUsingActualVettingActorsForGoodAd() throws JsonProcessingException {
        TestRoute appRoute = testRoute(new HttpRoutes(vettingSupervisor, new VerdictCache()).registerRoutes());

        Ad ad = new Ad(999, "fin sofa", "lite brukt");
        String adJson = new ObjectMapper()
                .writeValueAsString(ad);

        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, adJson))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"" + ad.getAdId() + "\",\"value\":\"GOOD\"}");
    }

    @Test
    public void shouldGetRealVettingResultVettingBADWhenUsingActualVettingActorsForBadAd() throws JsonProcessingException {
        TestRoute appRoute = testRoute(new HttpRoutes(vettingSupervisor, new VerdictCache()).registerRoutes());

        Ad ad = new Ad(99999999, "nigeria fin sofa", "lite brukt");
        String adJson = new ObjectMapper()
                .writeValueAsString(ad);

        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, adJson))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"" + ad.getAdId() + "\",\"value\":\"BAD\"}");
    }


}
