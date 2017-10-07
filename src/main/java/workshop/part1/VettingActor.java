package workshop.part1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import javaslang.collection.List;
import scala.concurrent.duration.FiniteDuration;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWord;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserCriminalRecord;
import workshop.common.userservice.UserService;

public class VettingActor extends AbstractActor {

    private final UserService userService;
    private final FraudWordService fraudWordService;
    private final ActorRef numVettedAdsActor;
    private final FiniteDuration numVettedAdsInterval;
    private Integer numVettedAds = 0;

    public VettingActor(UserService userService, FraudWordService fraudWordService, ActorRef numVettedAdsActor, FiniteDuration numVettedAdsInterval) {
        this.userService = userService;
        this.fraudWordService = fraudWordService;
        this.numVettedAdsActor = numVettedAdsActor;
        this.numVettedAdsInterval = numVettedAdsInterval;
    }

    @Override
    public void preStart() throws Exception {
        scheduleReportNumVettedAds();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Ad.class, ad -> {
                Verdict.VerdictType verdict = performVetting(ad);
                numVettedAds += 1;
                sender().tell(verdict, self());
            })
            .match(GetNumVettedAds.class, m -> sender().tell(new NumVettedAds(numVettedAds), self()))
            .match(ReportNumVettedAds.class, m -> {
                numVettedAdsActor.tell(new NumVettedAds(numVettedAds), self());
                scheduleReportNumVettedAds();
            })
            .build();
    }

    private Verdict.VerdictType performVetting(Ad ad) {
        UserCriminalRecord record = userService.vettUser(ad.userId);
        List<FraudWord> fraudWords = fraudWordService.examineWords(ad.toAdWords());

        return toVerdictStatus(record, fraudWords);
    }

    private Verdict.VerdictType toVerdictStatus(UserCriminalRecord record, List<FraudWord> fraudWords) {
        if (record == UserCriminalRecord.GOOD && fraudWords.isEmpty()) {
            return Verdict.VerdictType.GOOD;
        } else {
            return Verdict.VerdictType.BAD;
        }
    }

    private void scheduleReportNumVettedAds() {
        context().system().scheduler().scheduleOnce(numVettedAdsInterval, self(),
            new ReportNumVettedAds(), context().system().dispatcher(), self());
    }

    static class GetNumVettedAds {
    }

    public static class NumVettedAds {
        public final Integer numVettedAds;

        public NumVettedAds(Integer numVettedAds) {
            this.numVettedAds = numVettedAds;
        }
    }

    static class ReportNumVettedAds {
    }
}
