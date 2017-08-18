package workshop.part2.subactor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import javaslang.collection.List;
import scala.PartialFunction;
import scala.concurrent.duration.FiniteDuration;
import scala.runtime.BoxedUnit;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWord;
import workshop.common.userservice.UserCriminalRecord;
import workshop.part1.Verdict;
import workshop.part2.FraudWordActor.ExamineWords;
import workshop.part2.FraudWordActor.ExamineWordsResult;
import workshop.part2.UserActor.CheckUser;
import workshop.part2.UserActor.CheckUserResult;

public class VettingActor extends AbstractActor {

    private final ActorRef userActor;
    private final ActorRef fraudWordActor;
    private final FiniteDuration timeoutVetting;
    private CheckUserResult checkUserResult;
    private ExamineWordsResult examineWordsResult;
    private ActorRef sender;

    VettingActor(ActorRef userActor, ActorRef fraudWordActor, FiniteDuration timeoutVetting) {
        this.userActor = userActor;
        this.fraudWordActor = fraudWordActor;
        this.timeoutVetting = timeoutVetting;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.create()
            .match(Ad.class, ad -> {
                userActor.tell(new CheckUser(ad.userId), self());
                fraudWordActor.tell(new ExamineWords(ad.toAdWords()), self());

                sender = sender();
                scheduleTimeout(timeoutVetting);
            })
            .match(CheckUserResult.class, m -> {
                if (examineWordsResult != null) {
                    sendVerdictAndTerminateSelf(toVerdictStatus(m.record, examineWordsResult.fraudWords), sender);
                } else {
                    checkUserResult = m;
                }
            })
            .match(ExamineWordsResult.class, m -> {
                if (checkUserResult != null) {
                    sendVerdictAndTerminateSelf(toVerdictStatus(checkUserResult.record, m.fraudWords), sender);
                } else {
                    examineWordsResult = m;
                }
            })
            .match(TimeoutVetting.class, m -> sendVerdictAndTerminateSelf(Verdict.UNKNOWN, sender))
            .build();
    }

    private Verdict toVerdictStatus(UserCriminalRecord record, List<FraudWord> fraudWords) {
        if (record == UserCriminalRecord.GOOD && fraudWords.isEmpty()) {
            return Verdict.GOOD;
        } else {
            return Verdict.BAD;
        }
    }

    private void sendVerdictAndTerminateSelf(Verdict verdict, ActorRef receiver) {
        receiver.tell(verdict, self());
        context().stop(self());
    }

    private void scheduleTimeout(FiniteDuration delay) {
        context().system().scheduler().scheduleOnce(delay, self(),
            new TimeoutVetting(), context().system().dispatcher(), self());
    }

    private static class TimeoutVetting {
    }
}
