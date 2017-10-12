package workshop.part2.subactor

import akka.actor.ActorRef
import akka.actor.UntypedActor
import scala.concurrent.duration.FiniteDuration
import workshop.part2.FraudWordActor.ExamineWordsResult
import workshop.part2.UserActor.CheckUserResult

class VettingSubActor(private val userActor: ActorRef,
                      private val fraudWordActor: ActorRef,
                      private val timeoutVetting: FiniteDuration) : UntypedActor() {

    private var checkUserResult: CheckUserResult? = null
    private var examineWordsResult: ExamineWordsResult? = null
    private var senderSaved: ActorRef? = null

    override fun onReceive(msg: Any?) {}

    private class TimeoutVetting
}
