package workshop.part3.futures;

import java.util.concurrent.CompletionStage;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.compat.java8.FutureConverters;
import scala.concurrent.duration.FiniteDuration;

import static workshop.part1.Verdict.VerdictType;

public class VettingFutureActor extends AbstractActor {

    private final ActorRef userActor;
    private final ActorRef fraudWordActor;
    private final FiniteDuration timeoutVetting;

    VettingFutureActor(ActorRef userActor, ActorRef fraudWordActor, FiniteDuration timeoutVetting) {
        this.userActor = userActor;
        this.fraudWordActor = fraudWordActor;
        this.timeoutVetting = timeoutVetting;
    }

    @Override
    // receive an Ad and combine the Futures returned from underlying actors to reply with a VerdictType
    public Receive createReceive() {
        return receiveBuilder()
            .build();
    }

    // utility for passing a VerdictType wrapped in javas CompletionStage to a receiver
    private void pipeTo(ActorRef receiver, CompletionStage<VerdictType> verdict) {
        Patterns.pipe(FutureConverters.toScala(verdict), context().system().dispatcher()).to(receiver);
    }

    // handy converter from scala Future to javas CompletionStage
    private <T> CompletionStage<T> ask(ActorRef receiver, Object msg) {
        return (CompletionStage<T>) FutureConverters.toJava(Patterns.ask(receiver, msg, new Timeout(timeoutVetting)));
    }
}
