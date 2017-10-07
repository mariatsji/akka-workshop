package workshop.part2b.futures

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.dispatch.Futures
import akka.dispatch.Recover
import akka.pattern.Patterns
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag
import workshop.common.ad.Ad
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.Verdict
import workshop.part2b.FraudWordActor.ExamineWords
import workshop.part2b.FraudWordActor.ExamineWordsResult
import workshop.part2b.UserActor.CheckUser
import workshop.part2b.UserActor.CheckUserResult

class VettingFutureActor internal constructor(private val userActor: ActorRef, private val fraudWordActor: ActorRef, private val timeoutVetting: FiniteDuration) : AbstractActor() {

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(Ad::class.java) { ad ->
                    val userFuture: Future<CheckUserResult> = Patterns.ask(userActor, CheckUser(ad.userId), Timeout(timeoutVetting))
                            .mapTo(ClassTag.apply(CheckUserResult::class.java))

                    val fraudWordFuture: Future<ExamineWordsResult> = Patterns.ask(fraudWordActor, ExamineWords(ad.toAdWords()), Timeout(timeoutVetting))
                            .mapTo(ClassTag.apply(ExamineWordsResult::class.java))

                    val ec = context().system().dispatcher()

                    val userOk: Future<Boolean> = userFuture.map({ result -> result.record === UserCriminalRecord.GOOD }, ec)
                    val fraudWordsOk: Future<Boolean> = fraudWordFuture.map<Boolean>({ result -> result.fraudWords.isEmpty() }, ec)

                    val verdict = Futures.reduce(listOf(userOk, fraudWordsOk),
                            { result: Boolean, current: Boolean -> result && current }, ec)
                            .map({ result -> if (result) Verdict.GOOD else Verdict.BAD }, ec)
                            .recover(object : Recover<Verdict>() {
                                override fun recover(problem: Throwable): Verdict {
                                    return Verdict.PENDING
                                }
                            }, ec)

                    Patterns.pipe(verdict, ec).to(sender())
                }
                .build()
    }
}
