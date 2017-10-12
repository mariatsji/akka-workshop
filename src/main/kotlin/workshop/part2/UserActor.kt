package workshop.part2

import akka.actor.UntypedActor
import workshop.common.userservice.UserCriminalRecord
import workshop.common.userservice.UserService

class UserActor(private val userService: UserService) : UntypedActor() {

    override fun onReceive(message: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class CheckUserResult(val record: UserCriminalRecord)

    class CheckUser(val userId: Int?)
}
