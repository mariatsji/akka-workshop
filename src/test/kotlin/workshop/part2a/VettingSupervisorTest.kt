package workshop.part2a

import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.testkit.TestActorRef
import akka.testkit.TestProbe
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.mockito.Mockito.mock
import workshop.common.ad.Ad
import workshop.part1.AkkaTest
import workshop.part1.Verdict


class VettingSupervisorTest : AkkaTest() {

    @Test
    fun createsVettingActorWhenAdIsReceived() {
        val vettingActorFactory = mock(VettingActorFactory::class.java)

        whenever(vettingActorFactory.create(any()))
                .thenReturn(sender.ref())

        val ad = createAd(123)
        createVettingSupervisor(vettingActorFactory).tell(ad, sender.ref())

        verify(vettingActorFactory).create(any())
        assertThat(sender.expectMsgClass(Ad::class.java), equalTo(ad))
    }

    @Test
    fun spoofsSenderWhenCreatingVettingActor() {
        val vettingActorFactory = mock(VettingActorFactory::class.java)

        val vettingActor = TestProbe.apply(system)
        whenever(vettingActorFactory.create(any()))
                .thenReturn(vettingActor.ref())

        val ad = createAd(123)
        createVettingSupervisor(vettingActorFactory).tell(ad, sender.ref())

        vettingActor.expectMsgClass(Ad::class.java)
        vettingActor.reply(Verdict.GOOD)
        assertThat(sender.expectMsgClass(Verdict::class.java), equalTo(Verdict.GOOD))
    }

    @Test
    fun resumesVettingActorWhenUserNotFoundException() {
        val strategy = createVettingSupervisor(mock(VettingActorFactory::class.java))
                .underlyingActor()
                .supervisorStrategy()
                .decider()
                .apply(UserNotFoundException("user not found exception"))

        assertThat(strategy, equalTo(SupervisorStrategy.resume() as SupervisorStrategy.Directive))
    }

    @Test
    fun restartsVettingActorWhenNullPointerException() {
        val strategy = createVettingSupervisor(mock(VettingActorFactory::class.java))
                .underlyingActor()
                .supervisorStrategy()
                .decider()
                .apply(NullPointerException("null pointer exception"))

        assertThat(strategy, equalTo(SupervisorStrategy.restart() as SupervisorStrategy.Directive))
    }

    @Test
    fun escalatesExceptionWhenVettingActorFailsWithOtherException() {
        val strategy = createVettingSupervisor(mock(VettingActorFactory::class.java))
                .underlyingActor()
                .supervisorStrategy()
                .decider()
                .apply(RuntimeException("other exception"))

        assertThat(strategy, equalTo(SupervisorStrategy.escalate() as SupervisorStrategy.Directive))
    }

    private fun createVettingSupervisor(vettingActorFactory: VettingActorFactory): TestActorRef<VettingSupervisor> {
        return TestActorRef.create(system, Props.create(VettingSupervisor::class.java) { VettingSupervisor(vettingActorFactory) })
    }

    private fun createAd(userId: Int): Ad {
        return Ad(userId, "ad title", "description")
    }
}