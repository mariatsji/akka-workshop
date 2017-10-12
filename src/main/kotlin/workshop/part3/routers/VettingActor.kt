package workshop.part3.routers

import akka.actor.AbstractActor
import akka.actor.ActorRef
import scala.concurrent.duration.FiniteDuration
import workshop.part2.FraudWordActor.ExamineWordsResult
import workshop.part2.UserActor.CheckUserResult

class VettingActor(private val userActor: ActorRef, private val fraudWordActor: ActorRef, private val timeoutVetting: FiniteDuration) : AbstractActor() {
    override fun createReceive(): Receive {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var checkUserResult: CheckUserResult? = null
    private var examineWordsResult: ExamineWordsResult? = null
    private var zender: ActorRef? = null

    object TimeoutVetting
}
