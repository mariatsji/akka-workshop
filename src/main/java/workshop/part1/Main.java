package workshop.part1;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.Duration;
import workshop.ad.Ad;
import workshop.fraudwordsservice.FraudWordService;
import workshop.userservice.UserService;

public class Main {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("MonolithActorSystem");

        ActorRef numVettedAdsActor = system.actorOf(Props.create(NumVettedAdsActor.class, NumVettedAdsActor::new));

        ActorRef vettingActor = system.actorOf(Props.create(VettingActor.class,
            () -> new VettingActor(new UserService(), new FraudWordService(), numVettedAdsActor, Duration.create(1, TimeUnit.SECONDS))), "vettingActor");

        vettingActor.tell(createGoodAd(), ActorRef.noSender());
        vettingActor.tell(createBadAd(), ActorRef.noSender());

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
            return ReceiveBuilder.create()
                .match(VettingActor.NumVettedAds.class, m -> {
                    System.out.println("Num vetted ads: " + m.numVettedAds);
                })
                .build();
        }
    }
}
