package workshop.part2.supervisor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.TestActor;
import akka.testkit.TestKit;
import javaslang.collection.List;
import org.junit.Test;
import workshop.common.ad.Ad;
import workshop.common.userservice.UserCriminalRecord;
import workshop.part1.AkkaTest;
import workshop.part1.Verdict;
import workshop.part2.FraudWordActor;
import workshop.part2.UserActor;

import static akka.actor.ActorRef.noSender;
import static akka.testkit.JavaTestKit.duration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class VettingSupervisorIntegrationTest extends AkkaTest {

    @Test
    public void handlesVettingRequestsInParallel() {
        new TestKit(system) {{

            TestKit userActor = new TestKit(system);
            userActor.setAutoPilot(new TestActor.AutoPilot() {
                public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                    sender.tell(new UserActor.CheckUserResult(UserCriminalRecord.GOOD), noSender());
                    return keepRunning();
                }
            });

            TestKit fraudWordActor = new TestKit(system);
            fraudWordActor.setAutoPilot(new TestActor.AutoPilot() {
                public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                    sender.tell(new FraudWordActor.ExamineWordsResult(List.empty()), noSender());
                    return keepRunning();
                }
            });

            VettingActorFactory vettingActorFactory = new VettingActorFactory(userActor.testActor(), fraudWordActor.testActor());
            ActorRef vettingSupervisor = system.actorOf(Props.create(VettingSupervisor.class, () -> new VettingSupervisor(vettingActorFactory)));

            vettingSupervisor.tell(createAd(1), testActor());
            vettingSupervisor.tell(createAd(2), testActor());

            within(duration("3 seconds"), () -> {
                assertThat(expectMsgClass(Verdict.VerdictType.class), is(Verdict.VerdictType.GOOD));
                assertThat(expectMsgClass(Verdict.VerdictType.class), is(Verdict.VerdictType.GOOD));

                return null;
            });
        }};
    }

    private Ad createAd(int userId) {
        return new Ad(userId, "ad title", "description");
    }
}