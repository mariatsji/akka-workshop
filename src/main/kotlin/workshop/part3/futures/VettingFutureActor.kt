package workshop.part3.futures

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.pattern.Patterns
import akka.util.Timeout
import scala.compat.java8.FutureConverters
import scala.concurrent.duration.FiniteDuration
import workshop.part1.VerdictType
import java.util.concurrent.CompletionStage

class VettingFutureActor internal constructor(private val userActor: ActorRef, private val fraudWordActor: ActorRef, private val timeoutVetting: FiniteDuration) : AbstractActor() {
    override fun createReceive(): Receive {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun pipeTo(receiver: ActorRef, verdict: CompletionStage<VerdictType>) {
        Patterns.pipe(FutureConverters.toScala(verdict), context().system().dispatcher()).to(receiver)
    }

    private fun <T> ask(receiver: ActorRef, msg: Any): CompletionStage<T> {
        return FutureConverters.toJava(Patterns.ask(receiver, msg, Timeout(timeoutVetting))) as CompletionStage<T>
    }
}
