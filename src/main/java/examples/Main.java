package examples;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserService;
import workshop.part1.NumVettedAdsActor;
import workshop.part1.VettingActor;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MonolithActorSystem");

        ActorRef numVettedAdsActor = system.actorOf(Props.create(NumVettedAdsActor.class, NumVettedAdsActor::new));

        ActorRef vettingActor = system.actorOf(Props.create(VettingActor.class,
            () -> new VettingActor(new UserService(), new FraudWordService(), numVettedAdsActor, Duration.create(1, TimeUnit.SECONDS))), "vettingActor");

        Thread.sleep(2000);
        vettingActor.tell(createGoodAd(), ActorRef.noSender());
        vettingActor.tell(createBadAd(), ActorRef.noSender());

        Thread.sleep(3000);
        system.terminate();
    }

    private static Ad createGoodAd() {
        return new Ad(456, "fin sofa", "pent brukt - med blomstermønster");
    }

    private static Ad createBadAd() {
        return new Ad(200001, "cute dog", "money in advance to nigeria via westernunion, please");
    }



}
