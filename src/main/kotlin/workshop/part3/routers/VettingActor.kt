package workshop.part3.routers

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Terminated
import scala.concurrent.duration.FiniteDuration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.VerdictType
import workshop.part2.FraudWordActor.ExamineWords
import workshop.part2.FraudWordActor.ExamineWordsResult
import workshop.part2.UserActor.CheckUser
import workshop.part2.UserActor.CheckUserResult

class VettingActor(private val userActor: ActorRef, private val fraudWordActor: ActorRef, private val timeoutVetting: FiniteDuration) : AbstractActor() {
    private var checkUserResult: CheckUserResult? = null
    private var examineWordsResult: ExamineWordsResult? = null
    private var zender: ActorRef? = null

    @Throws(Exception::class)
    override fun preStart() {
        context().watch(userActor)
    }

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(Ad::class.java) { ad ->
                    userActor.tell(CheckUser(ad.userId), self())
                    fraudWordActor.tell(ExamineWords(ad.toAdWords()), self())

                    zender = sender()
                    scheduleTimeout(timeoutVetting)
                }
                .match(CheckUserResult::class.java) { m ->
                    if (examineWordsResult != null) {
                        sendVerdictAndTerminateSelf(toVerdictStatus(m.record, examineWordsResult!!.fraudWords), zender)
                    } else {
                        checkUserResult = m
                    }
                }
                .match(ExamineWordsResult::class.java) { m ->
                    if (checkUserResult != null) {
                        sendVerdictAndTerminateSelf(toVerdictStatus(checkUserResult!!.record, m.fraudWords), zender)
                    } else {
                        examineWordsResult = m
                    }
                }
                .match(TimeoutVetting::class.java) { _ -> sendVerdictAndTerminateSelf(VerdictType.PENDING, zender) }
                .match(Terminated::class.java) { _ -> sendVerdictAndTerminateSelf(VerdictType.PENDING, zender) }
                .build()
    }

    private fun toVerdictStatus(record: UserCriminalRecord, fraudWords: List<FraudWord>): VerdictType {
        return if (record === UserCriminalRecord.GOOD && fraudWords.isEmpty()) {
            VerdictType.GOOD
        } else {
            VerdictType.BAD
        }
    }

    private fun sendVerdictAndTerminateSelf(verdict: VerdictType, receiver: ActorRef?) {
        println(String.format("Got verdict %s - sending to receiver %s", verdict, receiver))
        receiver!!.tell(verdict, self())
        context().stop(self())
    }

    private fun scheduleTimeout(delay: FiniteDuration) {
        context().system().scheduler().scheduleOnce(delay, self(),
                TimeoutVetting(), context().system().dispatcher(), self())
    }

    private class TimeoutVetting
}
