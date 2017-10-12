package workshop.part3.futures

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.pattern.Patterns
import akka.util.Timeout
import scala.compat.java8.FutureConverters
import scala.concurrent.duration.FiniteDuration
import workshop.common.ad.Ad
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.VerdictType
import workshop.part2.FraudWordActor
import workshop.part2.UserActor.CheckUser
import workshop.part2.UserActor.CheckUserResult
import java.util.concurrent.CompletionStage

class VettingFutureActor internal constructor(private val userActor: ActorRef, private val fraudWordActor: ActorRef, private val timeoutVetting: FiniteDuration) : AbstractActor() {

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(Ad::class.java) { ad ->
                    val userFuture = ask<CheckUserResult>(userActor, CheckUser(ad.userId))
                    val fraudWordFuture = ask<FraudWordActor.ExamineWordsResult>(fraudWordActor, FraudWordActor.ExamineWords(ad.toAdWords()))

                    val userOk = userFuture.thenApply { result -> result.record === UserCriminalRecord.GOOD }
                    val fraudWordOk = fraudWordFuture.thenApply { result -> result.fraudWords.isEmpty() }

                    val verdict = userOk.thenCombine(fraudWordOk) { user, fraudWord ->
                            listOf(user, fraudWord) }
                        .thenApply<VerdictType> { res -> if (res.all { r -> r }) VerdictType.GOOD else VerdictType.BAD }
                        .handle<VerdictType> { ok, throwable -> ok ?: VerdictType.PENDING}

                    pipeTo(sender(), verdict)
                }
                .build()
    }

    private fun pipeTo(receiver: ActorRef, verdict: CompletionStage<VerdictType>) {
        Patterns.pipe(FutureConverters.toScala(verdict), context().system().dispatcher()).to(receiver)
    }

    private fun <T> ask(receiver: ActorRef, msg: Any): CompletionStage<T> {
        return FutureConverters.toJava(Patterns.ask(receiver, msg, Timeout(timeoutVetting))) as CompletionStage<T>
    }
}
