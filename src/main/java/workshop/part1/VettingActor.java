package workshop.part1;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import javaslang.collection.List;
import workshop.ad.ClassifiedAd;
import workshop.fraudwordsservice.FraudWord;
import workshop.fraudwordsservice.FraudWordService;
import workshop.userservice.UserCriminalRecord;
import workshop.userservice.UserService;

public class VettingActor extends AbstractActor {

    private final UserService userService;
    private final FraudWordService fraudWordService;

    public VettingActor(UserService userService, FraudWordService fraudWordService) {
        this.userService = userService;
        this.fraudWordService = fraudWordService;
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(ClassifiedAd.class, ad -> {
                sender().tell(performVetting(ad), getSelf());
            })
            .build();
    }

    private Verdict performVetting(ClassifiedAd ad) {
        UserCriminalRecord record = userService.vettUser(ad.userId);
        List<FraudWord> fraudWords = fraudWordService.examineWords(toAdWords(ad));

        return new Verdict(ad.adId, fraudWords, toVerdictStatus(fraudWords, record));
    }

    private static List<String> toAdWords(ClassifiedAd ad) {
        return List.of(ad.title.split("\\W"))
            .push(ad.description.split("\\W"));
    }

    private static VerdictStatus toVerdictStatus(List<FraudWord> fraudWords, UserCriminalRecord record) {
        if (record == UserCriminalRecord.GOOD && fraudWords.isEmpty()) {
            return VerdictStatus.GOOD;
        } else {
            return VerdictStatus.BAD;
        }
    }
}
