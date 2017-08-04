package workshop.part2;

import akka.actor.ActorContext;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.testkit.TestActorRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import workshop.common.ad.Ad;
import workshop.part1.AkkaTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VettingSupervisorTest extends AkkaTest {

    @Mock
    VettingActorFactory vettingActorFactory;

    @Test
    public void createsVettingActorWhenAdIsReceived () {
        when(vettingActorFactory.create(any()))
            .thenReturn(sender.ref());

        Ad ad = new Ad(123, "ad title", "description");
        createVettingSupervisor().tell(ad, sender.ref());

        verify(vettingActorFactory).create(isA(ActorContext.class));
        assertThat(sender.expectMsgClass(Ad.class), is(ad));
    }

    @Test
    public void restartsVettingActorWhenNullPointerException() {
        SupervisorStrategy.Directive strategy = createVettingSupervisor()
            .underlyingActor()
            .supervisorStrategy()
            .decider()
            .apply(new NullPointerException("test exception"));

        assertThat(strategy, is(SupervisorStrategy.restart()));
    }

    @Test
    public void escalatesExceptionWhenVettingActorFailsWithOtherExceptionThanNullPointerException() {
        SupervisorStrategy.Directive strategy = createVettingSupervisor()
            .underlyingActor()
            .supervisorStrategy()
            .decider()
            .apply(new RuntimeException("test exception"));

        assertThat(strategy, is(SupervisorStrategy.escalate()));
    }

    private TestActorRef<VettingSupervisor> createVettingSupervisor() {
        return TestActorRef.create(system, Props.create(VettingSupervisor.class, () -> new VettingSupervisor(vettingActorFactory)));
    }
}