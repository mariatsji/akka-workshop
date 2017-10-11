package workshop.part1;

import akka.actor.AbstractActor;

public class NumVettedAdsActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(VettingActor.NumVettedAds.class, m -> {
                    System.out.println("Num vetted ads: " + m.numVettedAds);
                })
                .build();
    }

}
