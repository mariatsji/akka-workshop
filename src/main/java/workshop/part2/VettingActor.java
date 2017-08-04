package workshop.part2;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.FiniteDuration;
import workshop.common.ad.Ad;
import workshop.part2.FraudWordActor.ExamineWordsResult;
import workshop.part2.UserActor.CheckUserResult;

import static workshop.common.ad.Ad.toVerdictStatus;

public class VettingActor extends AbstractActor {

    private final ActorRef userActor;
    private final ActorRef fraudWordActor;
    private CheckUserResult checkUserResult;
    private ExamineWordsResult examineWordsResult;
    private ActorRef sender;
    private FiniteDuration timeoutVetting;

    VettingActor(ActorRef userActor, ActorRef fraudWordActor, FiniteDuration timeoutVetting) {
        this.userActor = userActor;
        this.fraudWordActor = fraudWordActor;
        this.timeoutVetting = timeoutVetting;
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(Ad.class, ad -> {
                userActor.tell(new UserActor.CheckUser(ad.userId), self());
                fraudWordActor.tell(new FraudWordActor.ExamineWords(ad.toAdWords()), self());

                sender = sender();
                scheduleTimeout(timeoutVetting);
            })
            .match(CheckUserResult.class, m -> {
                if (examineWordsResult != null) {
                    sender.tell(toVerdictStatus(m.record, examineWordsResult.fraudWords), self());
                } else {
                    checkUserResult = m;
                }
            })
            .match(ExamineWordsResult.class, m -> {
                if (checkUserResult != null) {
                    sender.tell(toVerdictStatus(checkUserResult.record, m.fraudWords), self());
                } else {
                    examineWordsResult = m;
                }
            })
            .match(TimeoutVetting.class, m -> context().stop(self()))
            .build();
    }

    private void scheduleTimeout(FiniteDuration delay) {
        context().system().scheduler().scheduleOnce(delay, self(),
            new TimeoutVetting(), context().system().dispatcher(), self());
    }

    private static class TimeoutVetting {
    }
}
