package workshop.part2a;

import akka.actor.ActorContext;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import org.junit.Test;
import workshop.common.ad.Ad;
import workshop.part1.AkkaTest;
import workshop.part1.Verdict.VerdictType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VettingSupervisorTest extends AkkaTest {

    @Test
    public void createsVettingActorWhenAdIsReceived () {
        VettingActorFactory vettingActorFactory = mock(VettingActorFactory.class);

        TestProbe vettingActor = TestProbe.apply(system);
        when(vettingActorFactory.create(any()))
            .thenReturn(vettingActor.ref());

        Ad ad = createAd(123);
        createVettingSupervisor(vettingActorFactory).tell(ad, sender.ref());

        verify(vettingActorFactory).create(isA(ActorContext.class));
        assertThat(vettingActor.expectMsgClass(Ad.class), is(ad));
    }

    @Test
    public void spoofsSenderWhenCreatingVettingActor() {
        VettingActorFactory vettingActorFactory = mock(VettingActorFactory.class);

        TestProbe vettingActor = TestProbe.apply(system);
        when(vettingActorFactory.create(any()))
            .thenReturn(vettingActor.ref());

        Ad ad = createAd(123);
        createVettingSupervisor(vettingActorFactory).tell(ad, sender.ref());

        vettingActor.expectMsgClass(Ad.class);
        vettingActor.reply(VerdictType.GOOD);
        assertThat(sender.expectMsgClass(VerdictType.class), is(VerdictType.GOOD));
    }

    @Test
    public void resumesVettingActorWhenUserNotFoundException() {
        SupervisorStrategy.Directive strategy = createVettingSupervisor(mock(VettingActorFactory.class))
            .underlyingActor()
            .supervisorStrategy()
            .decider()
            .apply(new UserNotFoundException("user not found test exception"));

        assertThat(strategy, is(SupervisorStrategy.resume()));
    }

    @Test
    public void restartsVettingActorWhenNullPointerException() {
        SupervisorStrategy.Directive strategy = createVettingSupervisor(mock(VettingActorFactory.class))
            .underlyingActor()
            .supervisorStrategy()
            .decider()
            .apply(new NullPointerException("null pointer test exception"));

        assertThat(strategy, is(SupervisorStrategy.restart()));
    }

    @Test
    public void escalatesExceptionWhenVettingActorFailsWithOtherException() {
        SupervisorStrategy.Directive strategy = createVettingSupervisor(mock(VettingActorFactory.class))
            .underlyingActor()
            .supervisorStrategy()
            .decider()
            .apply(new RuntimeException("other exception"));

        assertThat(strategy, is(SupervisorStrategy.escalate()));
    }

    private TestActorRef<VettingSupervisor> createVettingSupervisor(VettingActorFactory vettingActorFactory) {
        return TestActorRef.create(system, Props.create(VettingSupervisor.class, () -> new VettingSupervisor(vettingActorFactory)));
    }

    private Ad createAd(int userId) {
        return new Ad(userId, "ad title", "description");
    }
}