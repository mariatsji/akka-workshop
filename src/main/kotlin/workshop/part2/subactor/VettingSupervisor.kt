package workshop.part2.subactor

import akka.actor.AbstractActor
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import akka.actor.SupervisorStrategy.escalate
import akka.actor.SupervisorStrategy.restart
import akka.japi.pf.DeciderBuilder
import akka.japi.pf.ReceiveBuilder
import scala.PartialFunction
import scala.concurrent.duration.Duration
import scala.runtime.BoxedUnit
import workshop.common.ad.Ad
import java.util.concurrent.TimeUnit.MINUTES

class VettingSupervisor internal constructor(private val vettingActorFactory: VettingActorFactory) : AbstractActor() {

    override fun supervisorStrategy(): SupervisorStrategy {
        return OneForOneStrategy(10, Duration.create(1, MINUTES), DeciderBuilder
                .match(NullPointerException::class.java) { restart() }
                .matchAny { escalate() }
                .build())
    }

    override fun receive(): PartialFunction<Any, BoxedUnit> {
        return ReceiveBuilder.create()
                .match(Ad::class.java) { ad -> vettingActorFactory.create(context()).tell(ad, sender()) }
                .build()
    }
}
