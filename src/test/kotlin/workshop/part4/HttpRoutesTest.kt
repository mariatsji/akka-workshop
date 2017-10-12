package workshop.part4

import akka.actor.ActorSystem
import akka.http.javadsl.model.ContentTypes
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.testkit.JUnitRouteTest
import akka.testkit.TestProbe
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import javaslang.control.Option
import org.junit.Test
import workshop.common.ad.Ad
import workshop.part1.Verdict
import workshop.part1.VerdictType

class HttpRoutesTest : JUnitRouteTest() {

    private val system = ActorSystem.create()
    private val dummyActor = TestProbe.apply(system)
    private val appRoute = testRoute(HttpRoutes(dummyActor.ref(), TestVerdictCache()).registerRoutes())

    @Test
    fun rootGives404() {
        appRoute.run(HttpRequest.GET("/"))
                .assertStatusCode(StatusCodes.NOT_FOUND)
    }

    @Test
    fun postAdToEvaluateWithUnfamiliarJsonShouldGive400() {
        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, "{\"fisk\":2}"))
                .assertStatusCode(StatusCodes.BAD_REQUEST)
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun postAdToEvaluateWithoutLiveActorsBackendGives200() {
        val ad = Ad(999, "fin sofa", "lite brukt")
        val adJson = ObjectMapper()
                .writeValueAsString(ad)

        appRoute.run(HttpRequest.POST("/evaluate")
                .withEntity(ContentTypes.APPLICATION_JSON, adJson))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"" + ad.adId + "\",\"value\":\"FAILURE\"}")
    }

    @Test
    fun getVettingResultFromUnknownAdShouldGive404() {
        appRoute.run(HttpRequest.GET("/evaluate/42"))
                .assertStatusCode(StatusCodes.NOT_FOUND)
    }

    @Test
    fun getVettingResultFromPreviouslyPostedAdShouldGiveVerdict() {
        appRoute.run(HttpRequest.GET("/evaluate/43"))
                .assertStatusCode(StatusCodes.OK)
                .assertEntity("{\"id\":\"43\",\"value\":\"BAD\"}")
    }

    private inner class TestVerdictCache : VerdictCache() {

        override fun get(adId: Int?): Option<Verdict> {
            return if (adId === 43) {
                Option.of(Verdict("43", VerdictType.BAD))
            } else {
                Option.none()
            }
        }

    }
}
