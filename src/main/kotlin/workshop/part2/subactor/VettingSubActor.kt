package workshop.part2.subactor

import akka.actor.ActorRef
import akka.actor.Terminated
import akka.actor.UntypedActor
import scala.concurrent.duration.FiniteDuration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.VerdictType
import workshop.part2.FraudWordActor.ExamineWords
import workshop.part2.FraudWordActor.ExamineWordsResult
import workshop.part2.UserActor.CheckUser
import workshop.part2.UserActor.CheckUserResult

class VettingSubActor(private val userActor: ActorRef,
                      private val fraudWordActor: ActorRef,
                      private val timeoutVetting: FiniteDuration) : UntypedActor() {

    private var checkUserResult: CheckUserResult? = null
    private var examineWordsResult: ExamineWordsResult? = null
    private var senderSaved: ActorRef? = null

    override fun preStart() {
        context.watch(userActor)
    }

    override fun onReceive(msg: Any?) = when (msg) {
        is Ad -> {
            userActor.tell(CheckUser(msg.userId), self())
            fraudWordActor.tell(ExamineWords(msg.toAdWords()), self())

            senderSaved = sender()
            scheduleTimeout(timeoutVetting)
        }
        is CheckUserResult -> {
            if (examineWordsResult != null) {
                sendVerdictAndTerminateSelf(toVerdictStatus(msg.record, examineWordsResult!!.fraudWords), senderSaved)
            } else {
                checkUserResult = msg
            }
        }
        is ExamineWordsResult -> {
            if (checkUserResult != null) {
                sendVerdictAndTerminateSelf(toVerdictStatus(checkUserResult!!.record, msg.fraudWords), senderSaved)
            } else {
                examineWordsResult = msg
            }
        }
        is TimeoutVetting -> sendVerdictAndTerminateSelf(VerdictType.PENDING, senderSaved)
        is Terminated -> sendVerdictAndTerminateSelf(VerdictType.PENDING, senderSaved)
        else -> unhandled(msg)
    }

    private fun toVerdictStatus(record: UserCriminalRecord, fraudWords: List<FraudWord>): VerdictType {
        return if (record === UserCriminalRecord.GOOD && fraudWords.isEmpty()) {
            VerdictType.GOOD
        } else {
            VerdictType.BAD
        }
    }

    private fun sendVerdictAndTerminateSelf(verdict: VerdictType, receiver: ActorRef?) {
        receiver!!.tell(verdict, self())
        context().stop(self())
    }

    private fun scheduleTimeout(delay: FiniteDuration) {
        context().system().scheduler().scheduleOnce(delay, self(),
                TimeoutVetting(), context().system().dispatcher(), self())
    }

    private class TimeoutVetting
}