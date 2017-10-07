package examples

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration.Duration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWordService
import workshop.common.userservice.UserService
import workshop.part1.VettingActor
import java.util.concurrent.TimeUnit

object Main {

    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val system = ActorSystem.create("MonolithActorSystem")

        val numVettedAdsActor = system.actorOf(Props.create(NumVettedAdsActor::class.java, { NumVettedAdsActor() }))

        val vettingActor = system.actorOf(Props.create(VettingActor::class.java
        ) { VettingActor(UserService(), FraudWordService(), numVettedAdsActor, Duration.create(1, TimeUnit.SECONDS)) }, "vettingActor")

        Thread.sleep(2000)
        vettingActor.tell(createGoodAd(), ActorRef.noSender())
        vettingActor.tell(createBadAd(), ActorRef.noSender())

        Thread.sleep(3000)
        system.terminate()
    }

    private fun createGoodAd(): Ad {
        return Ad(456, "fin sofa", "pent brukt - med blomstermønster")
    }

    private fun createBadAd(): Ad {
        return Ad(200001, "cute dog", "money in advance to nigeria via westernunion, please")
    }


    internal class NumVettedAdsActor : AbstractActor() {
        override fun createReceive(): Receive {
            return receiveBuilder()
                    .match(VettingActor.NumVettedAds::class.java) { m -> println("Num vetted ads: " + m.numVettedAds!!) }
                    .build()
        }
    }
}
