package workshop.part3.routers

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import workshop.common.ad.Ad
import workshop.part1.VettingActor

object VettingRouterMain {

    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val system = ActorSystem.create("router-test")

        val routerRef = system.actorOf(Props.create(VettingRouter::class.java))

        routerRef.tell(createBadAd(), ActorRef.noSender())
        routerRef.tell(createGoodAd(), ActorRef.noSender())
        Thread.sleep(100000)
        system.terminate()
    }

    private fun createGoodAd(): Ad {
        return Ad(456, "fin sofa", "pent brukt - med blomstermønster")
    }

    private fun createBadAd(): Ad {
        return Ad(200001, "cute dog", "money in advance to nigeria via westernunion, please")
    }


    internal class NumVettedAdsActor : AbstractActor() {
        override fun createReceive(): AbstractActor.Receive {
            return receiveBuilder()
                    .match(VettingActor.NumVettedAds::class.java) { m -> println("Num vetted ads: " + m.numVettedAds!!) }
                    .build()
        }
    }
}
