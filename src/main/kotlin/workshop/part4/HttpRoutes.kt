package workshop.part4

import akka.actor.ActorRef
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.Route
import akka.pattern.Patterns
import akka.util.Timeout
import scala.compat.java8.FutureConverters
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

class HttpRoutes(private val vettingActor: ActorRef, private val cache: VerdictCache) : AllDirectives() {

    /**
     * Dont forget to register your route here - otherwise you will be pondering 404s!
     */
    fun registerRoutes(): Route {
        return route(
                root(),
                verdict(),
                evaluate(),
                error()
        )
    }

    private fun root(): Route {
        return get { path("") { complete(StatusCodes.NOT_FOUND) } }
    }

    private fun error(): Route {
        return root() // todo
    }

    private fun evaluate(): Route {
        return root() // todo
    }

    private fun verdict(): Route {
        return root() // todo
    }

    private fun <T> ask(receiver: ActorRef, msg: Any): CompletionStage<T> {
        return FutureConverters.toJava(Patterns.ask(receiver, msg, Timeout(1, TimeUnit.SECONDS))) as CompletionStage<T>
    }

}
