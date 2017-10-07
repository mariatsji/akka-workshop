package workshop.part2b.futures;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Recover;
import akka.japi.Function2;
import akka.pattern.Patterns;
import akka.util.Timeout;
import javaslang.collection.List;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import scala.reflect.ClassTag$;
import workshop.common.ad.Ad;
import workshop.common.userservice.UserCriminalRecord;
import workshop.part1.Verdict;
import workshop.part2b.FraudWordActor;
import workshop.part2b.UserActor.CheckUser;
import workshop.part2b.UserActor.CheckUserResult;

public class VettingFutureActor extends AbstractActor {

    private final ActorRef userActor;
    private final ActorRef fraudWordActor;
    private final FiniteDuration timeoutVetting;

    VettingFutureActor(ActorRef userActor, ActorRef fraudWordActor, FiniteDuration timeoutVetting) {
        this.userActor = userActor;
        this.fraudWordActor = fraudWordActor;
        this.timeoutVetting = timeoutVetting;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Ad.class, ad -> {
                Future<CheckUserResult> userFuture = Patterns.ask(userActor, new CheckUser(ad.userId), new Timeout(timeoutVetting))
                    .mapTo(ClassTag$.MODULE$.apply(CheckUserResult.class));

                Future<FraudWordActor.ExamineWordsResult> fraudWordFuture = Patterns.ask(fraudWordActor, new FraudWordActor.ExamineWords(ad.toAdWords()), new Timeout(timeoutVetting))
                    .mapTo(ClassTag$.MODULE$.apply(FraudWordActor.ExamineWordsResult.class));

                ExecutionContextExecutor ec = context().system().dispatcher();

                Future<Boolean> userOk = userFuture.map(result -> result.record == UserCriminalRecord.GOOD, ec);
                Future<Boolean> fraudWordsOk = fraudWordFuture.map(result -> result.fraudWords.isEmpty(), ec);

                Future<Verdict.VerdictType> verdict = Futures.reduce(List.of(userOk, fraudWordsOk),
                    (Function2<Boolean, Boolean, Boolean>) (result, current) -> result && current, ec)
                    .map(result -> result ? Verdict.VerdictType.GOOD : Verdict.VerdictType.BAD, ec)
                    .recover(new Recover<Verdict.VerdictType>() {
                        public Verdict.VerdictType recover(Throwable problem) throws Throwable {
                            return Verdict.VerdictType.PENDING;
                        }
                    }, ec);

                Patterns.pipe(verdict, ec).to(sender());
            })
            .build();
    }
}
