package workshop.part1

import akka.actor.ActorRef
import akka.actor.Props
import akka.testkit.TestActorRef
import akka.testkit.TestProbe
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.fraudwordsservice.FraudWordService
import workshop.common.userservice.UserCriminalRecord
import workshop.common.userservice.UserService
import workshop.part1.VettingActor.*
import java.util.concurrent.TimeUnit


@RunWith(MockitoJUnitRunner::class)
class VettingActorTest : AkkaTest() {

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var fraudWordService: FraudWordService

    @Test
    fun acceptsAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.GOOD)

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(emptyList())

        val ad = createAd()
        createVettingActor().tell(ad, sender.ref())
        val verdict = sender.expectMsgClass(Verdict::class.java)

        assertThat(verdict, equalTo(Verdict.GOOD))
    }

    @Test
    fun doesNotAcceptAdWithFraudWords() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.GOOD)

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(listOf(FraudWord("nigeria")))

        createVettingActor().tell(createAd(), sender.ref())
        val verdict = sender.expectMsgClass(Verdict::class.java)

        assertThat(verdict, equalTo(Verdict.BAD))
    }

    @Test
    fun doesNotAcceptAdWithUserHavingCriminalRecord() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.EVIL)

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(emptyList())

        val ad = createAd()
        createVettingActor().tell(ad, sender.ref())
        val verdict = sender.expectMsgClass(Verdict::class.java)

        assertThat(verdict, equalTo(Verdict.BAD))
    }

    @Test
    fun respondsWithNumberOfVettedAds() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.EVIL)

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(emptyList())

        val vettingActor = createVettingActor()
        sender.send(vettingActor, GetNumVettedAds())
        assertThat(sender.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(0))

        sender.send(vettingActor, createAd())
        sender.expectMsgClass(Verdict::class.java)

        sender.send(vettingActor, GetNumVettedAds())
        assertThat(sender.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(1))
    }

    @Test
    fun reportsNumVettedAdsEveryConfiguredInterval() {
        createVettingActor(sender.ref(), Duration.create(1, TimeUnit.MILLISECONDS))

        assertThat(sender.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(0))
        assertThat(sender.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(0))
        assertThat(sender.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(0))
    }

    @Test
    fun reportsNumVettedAds() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.GOOD)

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(emptyList())

        val numVettedAdsActor = TestProbe.apply(system)
        val vettingActor = createVettingActor(numVettedAdsActor.ref(), Duration.create(24, TimeUnit.HOURS))

        sender.send(vettingActor, ReportNumVettedAds())
        assertThat(numVettedAdsActor.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(0))

        sender.send(vettingActor, createAd())
        sender.send(vettingActor, ReportNumVettedAds())
        assertThat(numVettedAdsActor.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(1))

        sender.send(vettingActor, createAd())
        sender.send(vettingActor, ReportNumVettedAds())
        assertThat(numVettedAdsActor.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(2))
    }

    private fun createAd(): Ad {
        return Ad(1, "Sofa", "Selling sofa")
    }

    private fun createVettingActor(numVettedAdsActor: ActorRef = TestProbe.apply(system).ref(), numVettedAdsInterval: FiniteDuration = Duration.create(1, TimeUnit.SECONDS)): TestActorRef<VettingActor> {
        return TestActorRef.create(system, Props.create(VettingActor::class.java) { VettingActor(userService, fraudWordService, numVettedAdsActor, numVettedAdsInterval) })
    }
}