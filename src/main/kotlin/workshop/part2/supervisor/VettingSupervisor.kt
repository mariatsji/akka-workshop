package workshop.part2.supervisor

import akka.actor.AbstractActor
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import akka.actor.SupervisorStrategy.*
import akka.japi.pf.DeciderBuilder
import scala.concurrent.duration.Duration
import workshop.common.ad.Ad
import java.util.concurrent.TimeUnit.MINUTES

class VettingSupervisor internal constructor(private val vettingActorFactory: VettingActorFactory) : AbstractActor() {

    override fun supervisorStrategy(): SupervisorStrategy {
        return OneForOneStrategy(10, Duration.create(1, MINUTES), DeciderBuilder
                .match(UserNotFoundException::class.java) { resume() }
                .match(NullPointerException::class.java) { restart() }
                .matchAny { escalate() }
                .build())
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(Ad::class.java) { ad -> vettingActorFactory.create(context()).tell(ad, sender()) }
                .build()
    }
}
