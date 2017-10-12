package workshop.part4

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.javadsl.model.ContentTypes
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.testkit.JUnitRouteTest
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWordService
import workshop.common.userservice.UserService
import workshop.part2.FraudWordActor
import workshop.part2.UserActor
import workshop.part2.supervisor.VettingActorFactory
import workshop.part2.supervisor.VettingSupervisor

class HttpRoutesIntegrationTest : JUnitRouteTest() {

    private val system = ActorSystem.create()

    private var userActor: ActorRef? = null
    private var fraudWordActor: ActorRef? = null
    private var vettingSupervisor: ActorRef? = null

    @Before
    fun setupActors() {
        userActor = system.actorOf(Props.create(UserActor::class.java) { UserActor(UserService()) })
        fraudWordActor = system.actorOf(Props.create(FraudWordActor::class.java) { FraudWordActor(FraudWordService()) })

        vettingSupervisor = system.actorOf(Props.create(VettingSupervisor::class.java) { VettingSupervisor(VettingActorFactory(userActor!!, fraudWordActor!!)) })
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldGetRealVettingResultVettingOKWhenUsingActualVettingActorsForGoodAd() {
        val appRoute = testRoute(HttpRoutes(vettingSupervisor!!, VerdictCache()).registerRoutes())

        val ad = Ad(999, "fin sofa", "lite brukt")
        val adJson = ObjectMapper()
                .writeValueAsString(ad)

        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, adJson))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"" + ad.adId + "\",\"value\":\"GOOD\"}")
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldGetRealVettingResultVettingBADWhenUsingActualVettingActorsForBadAd() {
        val appRoute = testRoute(HttpRoutes(vettingSupervisor!!, VerdictCache()).registerRoutes())

        val ad = Ad(99999999, "nigeria fin sofa", "lite brukt")
        val adJson = ObjectMapper()
                .writeValueAsString(ad)

        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, adJson))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"" + ad.adId + "\",\"value\":\"BAD\"}")
    }


}
