package workshop.part2.subactor

import akka.actor.Props
import akka.actor.Terminated
import akka.testkit.TestActorRef
import akka.testkit.TestProbe
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.AkkaTest
import workshop.part1.Verdict
import workshop.part2.FraudWordActor
import workshop.part2.FraudWordActor.ExamineWordsResult
import workshop.part2.UserActor.CheckUser
import workshop.part2.UserActor.CheckUserResult
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class VettingActorTest : AkkaTest() {

    var userActor = TestProbe.apply(system)
    var fraudWordActor = TestProbe.apply(system)

    @Test
    fun acceptsAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        sender.send(createVettingActor(), createAd())

        userActor.expectMsgClass(CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(emptyList()))

        val verdict = sender.expectMsgClass(Verdict::class.java)

        assertThat(verdict, `is`(Verdict.GOOD))
    }

    @Test
    fun doesNotAcceptAdWithFraudWords() {
        sender.send(createVettingActor(), createAd())

        userActor.expectMsgClass(CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(listOf(FraudWord("westernunion"))))

        val verdict = sender.expectMsgClass(Verdict::class.java)

        assertThat(verdict, `is`(Verdict.BAD))
    }

    @Test
    fun doesNotAcceptAdWithUserHavingCriminalRecord() {
        sender.send(createVettingActor(), createAd())

        userActor.expectMsgClass(CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.EVIL))

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(emptyList()))

        val verdict = sender.expectMsgClass(Verdict::class.java)

        assertThat(verdict, `is`(Verdict.BAD))
    }

    @Test
    fun replaysWithUnknownVerdictWhenVettingTimeoutReached() {
        val vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS))

        sender.send(vettingActor, createAd())

        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, CheckUserResult(UserCriminalRecord.GOOD))
        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, ExamineWordsResult(emptyList()))

        assertThat(sender.expectMsgClass(Verdict::class.java), `is`(Verdict.UNKNOWN))
    }

    @Test
    fun terminatesAfterAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        val vettingActor = createVettingActor()
        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())

        userActor.expectMsgClass(CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(emptyList()))

        sender.expectMsgClass(Verdict::class.java)
        sender.expectMsgClass(Terminated::class.java)
    }

    @Test
    fun terminatesAfterNotAcceptAdWithFraudWords() {
        val vettingActor = createVettingActor()
        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())

        userActor.expectMsgClass(CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(listOf(FraudWord("westernunion"))))

        sender.expectMsgClass(Verdict::class.java)
        sender.expectMsgClass(Terminated::class.java)
    }

    @Test
    fun terminatesAfterVettingTimeoutReached() {
        val vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS))
        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())

        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, CheckUserResult(UserCriminalRecord.GOOD))
        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, ExamineWordsResult(emptyList()))

        sender.expectMsgClass(Verdict::class.java)
        sender.expectMsgClass(Terminated::class.java)
    }

    private fun createAd(): Ad {
        return Ad(1, "Sofa", "Selling sofa")
    }

    private fun createVettingActor(timeoutVetting: FiniteDuration = Duration.create(10, TimeUnit.SECONDS)): TestActorRef<VettingActor> {
        return TestActorRef.create(system, Props.create(VettingActor::class.java) { VettingActor(userActor.ref(), fraudWordActor.ref(), timeoutVetting) })
    }
}