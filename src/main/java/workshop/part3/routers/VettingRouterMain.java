package workshop.part3.routers;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import workshop.common.ad.Ad;
import workshop.part1.VettingActor;

public class VettingRouterMain {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("router-test");

        ActorRef routerRef = system.actorOf(Props.create(VettingRouter.class));

        routerRef.tell(createBadAd(), ActorRef.noSender());
        routerRef.tell(createGoodAd(), ActorRef.noSender());
        Thread.sleep(100000);
        system.terminate();
    }

    private static Ad createGoodAd() {
        return new Ad(456, "fin sofa", "pent brukt - med blomstermønster");
    }

    private static Ad createBadAd() {
        return new Ad(200001, "cute dog", "money in advance to nigeria via westernunion, please");
    }


    static class NumVettedAdsActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                .match(VettingActor.NumVettedAds.class, m -> {
                    System.out.println("Num vetted ads: " + m.numVettedAds);
                })
                .build();
        }
    }
}
