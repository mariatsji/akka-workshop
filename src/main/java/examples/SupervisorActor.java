package examples;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.MINUTES;
import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.stop;

public class SupervisorActor extends AbstractActor {

    @Override
    public SupervisorStrategy supervisorStrategy() {
        boolean loggingEnabled = false;
        return new OneForOneStrategy(10, Duration.create(1, MINUTES), loggingEnabled, DeciderBuilder
            .match(ArithmeticException.class, e -> resume())
            .match(NullPointerException.class, e -> restart())
            .match(IllegalArgumentException.class, e -> stop())
            .matchAny(o -> escalate()).build());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Props.class, props -> {
                ActorRef testActor = context().actorOf(props);
                testActor.tell("print this string, please", self());
            }).build();
    }

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MySystem");

        ActorRef supervisor = system.actorOf(Props.create(SupervisorActor.class), "supervisor");
        Props testActorProps = Props.create(TestActor.class);
        supervisor.tell(testActorProps, ActorRef.noSender());

        // There are better ways to ensure message are received before termination
        Thread.sleep(100);
        system.terminate();
    }
}
