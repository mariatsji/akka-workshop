package workshop.part3.futures;

import java.util.concurrent.CompletionStage;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import javaslang.collection.List;
import scala.compat.java8.FutureConverters;
import scala.concurrent.duration.FiniteDuration;
import workshop.common.ad.Ad;
import workshop.common.userservice.UserCriminalRecord;
import workshop.part1.Verdict;
import workshop.part2.FraudWordActor;
import workshop.part2.UserActor.CheckUser;
import workshop.part2.UserActor.CheckUserResult;

import static workshop.part1.Verdict.VerdictType;

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
                CompletionStage<CheckUserResult> userFuture = ask(userActor, new CheckUser(ad.userId));
                CompletionStage<FraudWordActor.ExamineWordsResult> fraudWordFuture = ask(fraudWordActor, new FraudWordActor.ExamineWords(ad.toAdWords()));

                CompletionStage<Boolean> userOk = userFuture.thenApply(result -> result.record == UserCriminalRecord.GOOD);
                CompletionStage<Boolean> fraudWordOk = fraudWordFuture.thenApply(result -> result.fraudWords.isEmpty());

                CompletionStage<Verdict.VerdictType> verdict = userOk.thenCombine(fraudWordOk, (user, fraudWord) -> List.of(user, fraudWord))
                    .thenApply(res -> res.forAll(r -> r) ? VerdictType.GOOD : VerdictType.BAD)
                    .handle((ok, ex) -> {
                        if (ok != null) {
                            return ok;
                        } else {
                            return VerdictType.PENDING;
                        }
                    });

                pipeTo(sender(), verdict);
            })
            .build();


    }

    private void pipeTo(ActorRef receiver, CompletionStage<VerdictType> verdict) {
        Patterns.pipe(FutureConverters.toScala(verdict), context().system().dispatcher()).to(receiver);
    }

    @SuppressWarnings("unchecked")
    private <T> CompletionStage<T> ask(ActorRef receiver, Object msg) {
        return (CompletionStage<T>) FutureConverters.toJava(Patterns.ask(receiver, msg, new Timeout(timeoutVetting)));
    }
}
