package workshop.part2.subactor;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.testkit.TestActor;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import javaslang.collection.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import workshop.common.ad.Ad;
import workshop.common.userservice.UserCriminalRecord;
import workshop.part1.AkkaTest;
import workshop.part1.Verdict;
import workshop.part2.FraudWordActor.ExamineWordsResult;
import workshop.part2.UserActor.CheckUserResult;

import static akka.actor.ActorRef.noSender;
import static akka.testkit.JavaTestKit.duration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VettingSupervisorTest extends AkkaTest {

    @Test
    public void createsVettingActorWhenAdIsReceived () {
        VettingActorFactory vettingActorFactory = mock(VettingActorFactory.class);

        when(vettingActorFactory.create(any()))
            .thenReturn(sender.ref());

        Ad ad = createAd(123);
        createVettingSupervisor(vettingActorFactory).tell(ad, sender.ref());

        verify(vettingActorFactory).create(isA(ActorContext.class));
        assertThat(sender.expectMsgClass(Ad.class), is(ad));
    }

    @Test
    public void handlesVettingRequestsInParallel() {
        new TestKit(system) {{
            TestKit userActor = new TestKit(system);
            userActor.setAutoPilot(new TestActor.AutoPilot() {
                public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                        sender.tell(new CheckUserResult(UserCriminalRecord.GOOD), noSender());
                        return keepRunning();
                }
            });

            TestKit fraudWordActor = new TestKit(system);
            fraudWordActor.setAutoPilot(new TestActor.AutoPilot() {
                public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                    sender.tell(new ExamineWordsResult(List.empty()), noSender());
                    return keepRunning();
                }
            });

            VettingActorFactory vettingActorFactory = new VettingActorFactory(userActor.testActor(), fraudWordActor.testActor());
            ActorRef vettingActor = system.actorOf(Props.create(VettingSupervisor.class, () -> new VettingSupervisor(vettingActorFactory)));

            vettingActor.tell(createAd(1), testActor());
            vettingActor.tell(createAd(2), testActor());

            within(duration("3 seconds"), () -> {

                // Ideally we should different responses
                assertThat(expectMsgClass(Verdict.class), is(Verdict.GOOD));
                assertThat(expectMsgClass(Verdict.class), is(Verdict.GOOD));

                return null;
            });
        }};
    }

    @Test
    public void restartsVettingActorWhenNullPointerException() {
        SupervisorStrategy.Directive strategy = createVettingSupervisor(mock(VettingActorFactory.class))
            .underlyingActor()
            .supervisorStrategy()
            .decider()
            .apply(new NullPointerException("test exception"));

        assertThat(strategy, is(SupervisorStrategy.restart()));
    }

    @Test
    public void escalatesExceptionWhenVettingActorFailsWithOtherExceptionThanNullPointerException() {
        SupervisorStrategy.Directive strategy = createVettingSupervisor(mock(VettingActorFactory.class))
            .underlyingActor()
            .supervisorStrategy()
            .decider()
            .apply(new RuntimeException("test exception"));

        assertThat(strategy, is(SupervisorStrategy.escalate()));
    }

    private TestActorRef<VettingSupervisor> createVettingSupervisor(VettingActorFactory vettingActorFactory) {
        return TestActorRef.create(system, Props.create(VettingSupervisor.class, () -> new VettingSupervisor(vettingActorFactory)));
    }

    private Ad createAd(int userId) {
        return new Ad(userId, "ad title", "description");
    }
}