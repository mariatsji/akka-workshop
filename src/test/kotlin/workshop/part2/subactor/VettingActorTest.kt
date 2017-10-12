package workshop.part2.subactor

import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.Terminated
import akka.testkit.TestActorRef
import akka.testkit.TestProbe
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.AkkaTest
import workshop.part1.VerdictType
import workshop.part2.FraudWordActor
import workshop.part2.FraudWordActor.ExamineWordsResult
import workshop.part2.UserActor.CheckUser
import workshop.part2.UserActor.CheckUserResult
import java.util.concurrent.TimeUnit

class VettingActorTest : AkkaTest() {

    var userActor = TestProbe.apply(system)
    var fraudWordActor = TestProbe.apply(system)

    @Test
    fun acceptsAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        sender.send(createVettingActor(), createAd())

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(emptyList()))

        val verdict = sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java)

        assertThat(verdict, equalTo(VerdictType.GOOD))
    }

    @Test
    fun doesNotAcceptAdWithFraudWords() {
        sender.send(createVettingActor(), createAd())

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(listOf(FraudWord("westernunion"))))

        val verdict = sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java)

        assertThat(verdict, equalTo(VerdictType.BAD))
    }

    @Test
    fun doesNotAcceptAdWithUserHavingCriminalRecord() {
        sender.send(createVettingActor(), createAd())

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.EVIL))

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(emptyList()))

        val verdict = sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java)

        assertThat(verdict, equalTo(VerdictType.BAD))
    }

    @Test
    fun repliesWithPendingVerdictWhenVettingTimeoutReached() {
        val vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS))

        sender.send(vettingActor, createAd())

        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, CheckUserResult(UserCriminalRecord.GOOD))
        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, ExamineWordsResult(emptyList()))

        assertThat(sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java), equalTo(VerdictType.PENDING))
    }

    @Test
    fun repliesWithPendingVerdictWhenUserActorTerminates() {
        val avoidTriggerTimeout = Duration.create(1, TimeUnit.DAYS)

        val vettingActor = createVettingActor(avoidTriggerTimeout)

        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())
        userActor.send(userActor.ref(), PoisonPill.getInstance())

        assertThat(sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java), equalTo(VerdictType.PENDING))
    }

    @Test
    fun terminatesAfterAdWithNoFraudWordsAndUserWithoutCriminalRecord() {
        val vettingActor = createVettingActor()
        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(emptyList()))

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java)
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated::class.java)
    }

    @Test
    fun terminatesAfterNotAcceptAdWithFraudWords() {
        val vettingActor = createVettingActor()
        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())

        userActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), CheckUser::class.java)
        userActor.reply(CheckUserResult(UserCriminalRecord.GOOD))

        fraudWordActor.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), FraudWordActor.ExamineWords::class.java)
        fraudWordActor.reply(ExamineWordsResult(listOf(FraudWord("westernunion"))))

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java)
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated::class.java)
    }

    @Test
    fun terminatesAfterVettingTimeoutReached() {
        val vettingActor = createVettingActor(Duration.create(0, TimeUnit.MILLISECONDS))
        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())

        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, CheckUserResult(UserCriminalRecord.GOOD))
        schedule(Duration.create(100, TimeUnit.MILLISECONDS), vettingActor, ExamineWordsResult(emptyList()))

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java)
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated::class.java)
    }

    @Test
    fun terminatesAfterUserActorTerminates() {
        val avoidTriggerTimeout = Duration.create(1, TimeUnit.DAYS)
        val vettingActor = createVettingActor(avoidTriggerTimeout)

        sender.watch(vettingActor)
        sender.send(vettingActor, createAd())

        userActor.send(userActor.ref(), PoisonPill.getInstance())

        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), VerdictType::class.java)
        sender.expectMsgClass(Duration.create(0, TimeUnit.SECONDS), Terminated::class.java)
    }

    private fun createAd(): Ad {
        return Ad(1, "Sofa", "Selling sofa")
    }

    private fun createVettingActor(timeoutVetting: FiniteDuration = Duration.create(10, TimeUnit.SECONDS)): TestActorRef<VettingSubActor> {
        return TestActorRef.create(system, Props.create(VettingSubActor::class.java) { VettingSubActor(userActor.ref(), fraudWordActor.ref(), timeoutVetting) })
    }
}