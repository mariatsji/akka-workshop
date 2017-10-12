package workshop.part4

import akka.actor.ActorRef
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers.integerSegment
import akka.http.javadsl.server.Route
import akka.pattern.Patterns
import scala.compat.java8.FutureConverters
import scala.reflect.`ClassTag$`
import workshop.common.ad.Ad
import workshop.part1.Verdict
import workshop.part1.VerdictType
import java.util.concurrent.CompletionStage

internal class HttpRoutes(private val vettingActor: ActorRef, private val cache: VerdictCache) : AllDirectives() {

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
        return post { path("evaluate") { entity(Jackson.unmarshaller(Ad::class.java)) { ad -> completeOK(Verdict(ad.adId.toString(), VerdictType.FAILURE), Jackson.marshaller()) } } }
    }

    private fun evaluate(): Route {
        return post {
            path("evaluate") {
                entity(Jackson.unmarshaller(Ad::class.java)
                ) { ad ->
                    cache[ad]
                            .map { a -> verdict() }
                            .getOrElse {
                                completeOrRecoverWith(
                                        { doVetting(ad) },
                                        Jackson.marshaller()
                                ) { t -> error() }
                            }
                }
            }
        }
    }

    private fun verdict(): Route {
        return get {
            pathPrefix("evaluate") {
                path(integerSegment()
                ) { adId ->
                    cache[adId]
                            .map { verdict -> complete(StatusCodes.OK, verdict, Jackson.marshaller()) }
                            .getOrElse({ complete(StatusCodes.NOT_FOUND, String.format("No such vetting: %d", adId)) })
                }
            }
        }
    }

    private fun doVetting(ad: Ad): CompletionStage<Verdict> {

        val verdictId = ad.adId.toString()

        val actorVerdict = Patterns.ask(vettingActor, ad, 1000)
                .mapTo(`ClassTag$`.`MODULE$`.apply<VerdictType>(VerdictType::class.java))

        val vettingVerdict = FutureConverters.toJava(actorVerdict)

        return vettingVerdict.whenCompleteAsync { vt: VerdictType, error: Throwable ->
            if (error != null) {
                vettingVerdict.thenRunAsync { Verdict(verdictId, VerdictType.PENDING) }
            } else {
                vettingVerdict.thenRunAsync {
                    val verdict = Verdict(verdictId, vt)
                    cache.put(ad, verdict)
                }
            }
        }.thenApply { vt -> Verdict(verdictId, vt) }

    }

}
