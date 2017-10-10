package workshop.part2.supervisor;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import workshop.part2.subactor.VettingSubActor;

public class VettingActorFactory {

    private ActorRef userActor;
    private ActorRef fraudWordActor;

    public VettingActorFactory(ActorRef userActor, ActorRef fraudWordActor) {
        this.userActor = userActor;
        this.fraudWordActor = fraudWordActor;
    }

    public ActorRef create(ActorContext context) {
        return context.system().actorOf(Props.create(VettingSubActor.class,
            () -> new VettingSubActor(userActor, fraudWordActor, Duration.create(1, TimeUnit.SECONDS))));
    }
}