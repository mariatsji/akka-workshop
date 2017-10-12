package workshop.part4

import akka.actor.ActorRef
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers.integerSegment
import akka.http.javadsl.server.Route
import akka.pattern.Patterns
import akka.util.Timeout
import scala.compat.java8.FutureConverters
import workshop.common.ad.Ad
import workshop.part1.Verdict
import workshop.part1.VerdictType
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
        return post { path("evaluate") { entity(Jackson.unmarshaller(Ad::class.java)) { ad -> completeOK(Verdict(ad.adId.toString(), VerdictType.FAILURE), Jackson.marshaller()) } } }
    }

    private fun evaluate(): Route {
        return post {
            path("evaluate") {
                entity(Jackson.unmarshaller(Ad::class.java)
                ) { ad ->
                    cache[ad]
                            .map { _ -> verdict() }
                            .getOrElse {
                                completeOrRecoverWith(
                                        { doVetting(ad) },
                                        Jackson.marshaller(),
                                        { t ->
                                            t.printStackTrace()
                                            error() }
                                )
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

        val vettingVerdict = ask<VerdictType>(vettingActor, ad)


        return vettingVerdict.whenCompleteAsync { vt: VerdictType, error: Throwable? ->
            if (error != null) {
                vettingVerdict.thenRunAsync { Verdict(verdictId, VerdictType.PENDING) }
            } else {
                vettingVerdict.thenRunAsync {
                    val verdict = Verdict(verdictId, vt)
                    cache.put(ad, verdict)
                }
            }
        }.thenApply {
            vt -> Verdict(verdictId, vt)
        }

    }

    private fun <T> ask(receiver: ActorRef, msg: Any): CompletionStage<T> {
        return FutureConverters.toJava(Patterns.ask(receiver, msg, Timeout(1, TimeUnit.SECONDS))) as CompletionStage<T>
    }

}
