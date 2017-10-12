package workshop.part1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import scala.concurrent.duration.FiniteDuration;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserService;

public class VettingActor extends AbstractActor {

    private final UserService userService;
    private final FraudWordService fraudWordService;
    private final ActorRef numVettedAdsActor;
    private final FiniteDuration numVettedAdsInterval;

    public VettingActor(UserService userService, FraudWordService fraudWordService, ActorRef numVettedAdsActor, FiniteDuration numVettedAdsInterval) {
        this.userService = userService;
        this.fraudWordService = fraudWordService;
        this.numVettedAdsActor = numVettedAdsActor;
        this.numVettedAdsInterval = numVettedAdsInterval;
    }

    @Override
    // 1) Act on Ad message (reply with a Verdict.VerdictType) using UserService and FraudWordService
    // 2) Act on GetNumVettedAds message (reply with a NumVettedAds object)
    // 3) Act on ReportNumVettedAds - every numVettedAdsInterval schedule a message to NumVettedAdsActor telling it to print number of vetted ads
    public Receive createReceive() {
        return receiveBuilder()
            .build();
    }


    // reply with a NumVettedAds-message when receiving a message of this type
    static class GetNumVettedAds {
    }

    public static class NumVettedAds {
        public final Integer numVettedAds;

        public NumVettedAds(Integer numVettedAds) {
            this.numVettedAds = numVettedAds;
        }
    }

    // schedule a message of this type to myself every numVettedAdsInterval
    static class ReportNumVettedAds {
    }
}
