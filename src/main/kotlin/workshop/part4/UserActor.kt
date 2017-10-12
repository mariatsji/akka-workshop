package workshop.part4

import akka.actor.AbstractActor
import workshop.common.userservice.UserCriminalRecord
import workshop.common.userservice.UserService

class UserActor(private val userService: UserService) : AbstractActor() {

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(CheckUser::class.java) { m ->
                    val result = userService.vettUser(m.userId)
                    sender().tell(CheckUserResult(result), sender())
                }
                .build()
    }

    class CheckUserResult(val record: UserCriminalRecord)

    class CheckUser(val userId: Int?)
}
