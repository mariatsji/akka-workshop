package workshop.part2.subactor

import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.ActorRef.noSender
import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.testkit.JavaTestKit.duration
import akka.testkit.TestActor
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.mockito.Matchers.isA
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner
import workshop.common.ad.Ad
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.AkkaTest
import workshop.part1.Verdict
import workshop.part2.FraudWordActor.ExamineWordsResult
import workshop.part2.UserActor.CheckUserResult

@RunWith(MockitoJUnitRunner::class)
class VettingSupervisorTest : AkkaTest() {

    @Test
    fun createsVettingActorWhenAdIsReceived() {
        val vettingActorFactory = mock(VettingActorFactory::class.java)

        `when`(vettingActorFactory.create(any()))
                .thenReturn(sender.ref())

        val ad = createAd(123)
        createVettingSupervisor(vettingActorFactory).tell(ad, sender.ref())

        verify(vettingActorFactory).create(isA(ActorContext::class.java))
        assertThat(sender.expectMsgClass(Ad::class.java), `is`(ad))
    }

    @Test
    fun handlesVettingRequestsInParallel() {
        object : TestKit(system) {
            init {
                val userActor = TestKit(system)
                userActor.setAutoPilot(object : TestActor.AutoPilot() {
                    override fun run(sender: ActorRef, msg: Any): TestActor.AutoPilot {
                        sender.tell(CheckUserResult(UserCriminalRecord.GOOD), noSender())
                        return keepRunning()
                    }
                })

                val fraudWordActor = TestKit(system)
                fraudWordActor.setAutoPilot(object : TestActor.AutoPilot() {
                    override fun run(sender: ActorRef, msg: Any): TestActor.AutoPilot {
                        sender.tell(ExamineWordsResult(emptyList()), noSender())
                        return keepRunning()
                    }
                })

                val vettingActorFactory = VettingActorFactory(userActor.testActor(), fraudWordActor.testActor())
                val vettingActor = system.actorOf(Props.create(VettingSupervisor::class.java) { VettingSupervisor(vettingActorFactory) })

                vettingActor.tell(createAd(1), testActor())
                vettingActor.tell(createAd(2), testActor())

                within<Any>(duration("3 seconds")) {

                    // Ideally we should different responses
                    assertThat(expectMsgClass(Verdict::class.java), `is`(Verdict.GOOD))
                    assertThat(expectMsgClass(Verdict::class.java), `is`(Verdict.GOOD))

                    null
                }
            }
        }
    }

    @Test
    fun restartsVettingActorWhenNullPointerException() {
        val strategy = createVettingSupervisor(mock(VettingActorFactory::class.java))
                .underlyingActor()
                .supervisorStrategy()
                .decider()
                .apply(NullPointerException("test exception"))

        assertThat(strategy, `is`<SupervisorStrategy.Directive>(SupervisorStrategy.restart()))
    }

    @Test
    fun escalatesExceptionWhenVettingActorFailsWithOtherExceptionThanNullPointerException() {
        val strategy = createVettingSupervisor(mock(VettingActorFactory::class.java))
                .underlyingActor()
                .supervisorStrategy()
                .decider()
                .apply(RuntimeException("test exception"))

        assertThat(strategy, `is`<SupervisorStrategy.Directive>(SupervisorStrategy.escalate()))
    }

    private fun createVettingSupervisor(vettingActorFactory: VettingActorFactory): TestActorRef<VettingSupervisor> {
        return TestActorRef.create(system, Props.create(VettingSupervisor::class.java) { VettingSupervisor(vettingActorFactory) })
    }

    private fun createAd(userId: Int): Ad {
        return Ad(userId, "ad title", "description")
    }
}