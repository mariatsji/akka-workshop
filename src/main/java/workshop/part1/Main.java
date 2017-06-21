package workshop.part1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import javaslang.control.Try;
import workshop.ad.ClassifiedAd;

public class Main extends AbstractActor {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("MonolithActorSystem");
        final ActorRef vettingActor = system.actorOf(Props.create(VettingActor.class), "vetting");

        vettingActor.tell(createGoodAd(), ActorRef.noSender());
        vettingActor.tell(createBadAd(), ActorRef.noSender());

        Try.of(() -> {
            Thread.sleep(200);
            return system.terminate();
        });
    }

    private static ClassifiedAd createGoodAd() {
        return new ClassifiedAd(123L, 456L, "fin sofa", "pent brukt - med blomstermønster");
    }

    private static ClassifiedAd createBadAd() {
        return new ClassifiedAd(124L, 200001L, "cute dog", "money in advance to nigeria via westernunion, please");
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
