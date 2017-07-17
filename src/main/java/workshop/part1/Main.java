package workshop.part1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import workshop.ad.ClassifiedAd;
import workshop.fraudwordsservice.FraudWordService;
import workshop.userservice.UserService;

public class Main extends AbstractActor {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("MonolithActorSystem");
        final ActorRef vettingActor = system.actorOf(Props.create(VettingActor.class,
            () -> new VettingActor(new UserService(), new FraudWordService())), "vettingActor");

        vettingActor.tell(createGoodAd(), ActorRef.noSender());
        vettingActor.tell(createBadAd(), ActorRef.noSender());

        system.terminate();
    }

    private static ClassifiedAd createGoodAd() {
        return new ClassifiedAd(456, "fin sofa", "pent brukt - med blomstermønster");
    }

    private static ClassifiedAd createBadAd() {
        return new ClassifiedAd(200001, "cute dog", "money in advance to nigeria via westernunion, please");
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
