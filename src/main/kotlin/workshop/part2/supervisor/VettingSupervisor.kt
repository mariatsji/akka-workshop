package workshop.part2.supervisor

import akka.actor.AbstractActor

class VettingSupervisor internal constructor(private val vettingActorFactory: VettingActorFactory) : AbstractActor() {
    override fun createReceive(): Receive {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
