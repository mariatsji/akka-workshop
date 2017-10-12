package workshop.part1

import akka.actor.*
import akka.actor.SupervisorStrategy.resume
import akka.japi.pf.DeciderBuilder
import akka.testkit.TestActorRef
import akka.testkit.TestProbe
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
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
        val verdict = sender.expectMsgClass(VerdictType::class.java)

        assertThat(verdict, equalTo(VerdictType.GOOD))
    }

    @Test
    fun doesNotAcceptAdWithFraudWords() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.GOOD)

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(listOf(FraudWord("nigeria")))

        createVettingActor().tell(createAd(), sender.ref())
        val verdict = sender.expectMsgClass(VerdictType::class.java)

        assertThat(verdict, equalTo(VerdictType.BAD))
    }

    @Test
    fun doesNotAcceptAdWithUserHavingCriminalRecord() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.EVIL)

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(emptyList())

        val ad = createAd()
        createVettingActor().tell(ad, sender.ref())
        val verdict = sender.expectMsgClass(VerdictType::class.java)

        assertThat(verdict, equalTo(VerdictType.BAD))
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
        sender.expectMsgClass(VerdictType::class.java)

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

    @Test
    fun doesNotCountVettingThatFails() {
        whenever(userService.vettUser(eq(1)))
                .thenReturn(UserCriminalRecord.GOOD)
                .thenThrow(RuntimeException("Vetting failed"))

        doThrow(RuntimeException("Vetting failed"))
                .whenever(userService)
                .vettUser(eq(2))

        whenever(fraudWordService.examineWords(any()))
                .thenReturn(emptyList())

        val vettingActor = createVettingActor()
        sender.send(vettingActor, createAd(userId = 1))
        sender.expectMsgClass(VerdictType::class.java)
        sender.send(vettingActor, createAd(userId = 2))

        sender.send(vettingActor, GetNumVettedAds())
        assertThat(sender.expectMsgClass(NumVettedAds::class.java).numVettedAds, equalTo(1))
    }

    private fun createAd(userId: Int = 1): Ad {
        return Ad(userId, "Sofa", "Selling sofa")
    }

    private fun createVettingActor(numVettedAdsActor: ActorRef = TestProbe.apply(system).ref(),
                                   numVettedAdsInterval: FiniteDuration = Duration.create(1, TimeUnit.SECONDS)): TestActorRef<VettingActor> {
        val supervisor = TestActorRef.create<Actor>(system, Props.create(ResumingSupervisor::class.java))

        return TestActorRef.create(system, Props.create(VettingActor::class.java) {
            VettingActor(userService, fraudWordService, numVettedAdsActor, numVettedAdsInterval) }, supervisor)
    }

    private class ResumingSupervisor : AbstractActor() {
        override fun supervisorStrategy(): SupervisorStrategy {
            return OneForOneStrategy(DeciderBuilder.matchAny { resume() }.build())
        }

        override fun createReceive(): Receive {
            return receiveBuilder().build()
        }
    }
}