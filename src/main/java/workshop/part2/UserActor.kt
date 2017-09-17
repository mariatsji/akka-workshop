package workshop.part2

import akka.actor.AbstractActor
import akka.japi.pf.ReceiveBuilder
import scala.PartialFunction
import scala.runtime.BoxedUnit
import workshop.common.userservice.UserCriminalRecord
import workshop.common.userservice.UserService

class UserActor(private val userService: UserService) : AbstractActor() {

    override fun receive(): PartialFunction<Any, BoxedUnit> {
        return ReceiveBuilder.create()
                .match(CheckUser::class.java) { m ->
                    val result = userService.vettUser(m.userId)
                    sender().tell(CheckUserResult(result), sender())
                }
                .build()
    }

    class CheckUserResult(val record: UserCriminalRecord)

    class CheckUser(val userId: Int?)
}
