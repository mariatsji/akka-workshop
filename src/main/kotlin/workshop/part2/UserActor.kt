package workshop.part2

import akka.actor.UntypedActor
import workshop.common.userservice.UserCriminalRecord
import workshop.common.userservice.UserService

class UserActor(private val userService: UserService) : UntypedActor() {

    override fun onReceive(msg: Any?) = when (msg) {
        is CheckUser -> {
            val result = userService.vettUser(msg.userId)
            sender().tell(CheckUserResult(result), sender())
        }
        else -> unhandled(msg)
    }

    class CheckUserResult(val record: UserCriminalRecord)

    class CheckUser(val userId: Int?)
}
