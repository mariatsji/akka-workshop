package workshop.part1;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

public class VettingResultActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().match(Verdict.class, this::receiveVerdict).build();
    }

    private void receiveVerdict(Verdict verdict) {
        System.out.println(String.format("Received verdict %s, putting it in a database or on kafka or something", verdict));
    }

}
