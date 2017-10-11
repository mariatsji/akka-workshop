package workshop.part2.subactor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import scala.concurrent.duration.FiniteDuration;
import workshop.part2.FraudWordActor.ExamineWordsResult;
import workshop.part2.UserActor.CheckUserResult;

public class VettingSubActor extends AbstractActor {

    private final ActorRef userActor;
    private final ActorRef fraudWordActor;
    private final FiniteDuration timeoutVetting;
    private CheckUserResult checkUserResult;
    private ExamineWordsResult examineWordsResult;
    private ActorRef sender;

    public VettingSubActor(ActorRef userActor, ActorRef fraudWordActor, FiniteDuration timeoutVetting) {
        this.userActor = userActor;
        this.fraudWordActor = fraudWordActor;
        this.timeoutVetting = timeoutVetting;
    }

    @Override
    public void preStart() throws Exception {
        context().watch(userActor);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .build();
    }

    // message object used to indicate a timeout from underlying service actors
    private static class TimeoutVetting {
    }
}
