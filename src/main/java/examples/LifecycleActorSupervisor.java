package examples;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.OneForOneStrategy;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.MINUTES;
import static akka.actor.SupervisorStrategy.restart;

public class LifecycleActorSupervisor extends AbstractActor {

    static class CreateLifeCycleActor {
    }

    private static SupervisorStrategy strategy =
        new OneForOneStrategy(10, Duration.create(1, MINUTES),
            t -> restart(), false);

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(CreateLifeCycleActor.class, a -> {
                ActorRef actorRef = context().actorOf(Props.create(LifecycleActor.class), "lifecycleActor");
                sender().tell(actorRef, self());
            })
            .match(Object.class, o -> {
            })
            .build();
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException {
        ActorSystem system = ActorSystem.create("MySystem");

        ActorRef supervisor = system.actorOf(Props.create(LifecycleActorSupervisor.class), "lifecycleSupervisor");
        Inbox inbox = Inbox.create(system);
        inbox.send(supervisor, new CreateLifeCycleActor());

        ActorRef child = (ActorRef) inbox.receive(Duration.create(1, TimeUnit.MINUTES));
        child.tell(new RuntimeException("Ay, caramba!"), ActorRef.noSender());
        child.tell(PoisonPill.getInstance(), ActorRef.noSender());

        // There are better ways to ensure message are received before termination
        Thread.sleep(100);
        system.terminate();
    }

}
