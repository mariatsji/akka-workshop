package workshop.part1;

import java.util.function.Function;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import javaslang.collection.List;
import javaslang.control.Either;
import javaslang.control.Option;
import workshop.ad.ClassifiedAd;
import workshop.fraudwordsservice.FraudWord;
import workshop.fraudwordsservice.FraudWordService;
import workshop.fraudwordsservice.FraudWordServiceImpl;
import workshop.userservice.UserCriminalRecord;
import workshop.userservice.UserService;
import workshop.userservice.UserServiceImpl;

public class FraudControlActor extends AbstractActor {

    private final UserService userService = new UserServiceImpl();
    private final FraudWordService fraudWordService = new FraudWordServiceImpl();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().match(ClassifiedAd.class, this::performVetting).build();
    }

    private Verdict performVetting(ClassifiedAd ad) {
        Either<Throwable, Option<UserCriminalRecord>> userVetting = userService.vettUser(ad.userId);
        List<String> adWords = List.of(ad.title.split("\\W")).push(ad.description.split("\\W"));
        List<FraudWord> fraudWordsVetting = fraudWordService.examineWords(adWords).fold(t -> List.empty(), Function.identity());
        return new Verdict(ad.adId, fraudWordsVetting, toVerdictStatus(fraudWordsVetting, userVetting));
    }

    private VerdictStatus toVerdictStatus(List<FraudWord> fraudWordsVetting, Either<Throwable, Option<UserCriminalRecord>> userVetting) {
        boolean userOk = userVetting.fold(t -> Boolean.TRUE, ocr -> ocr.map(ucr -> ucr == UserCriminalRecord.GOOD).getOrElse(Boolean.FALSE));
        if (userOk && fraudWordsVetting.isEmpty()) {
            return VerdictStatus.GOOD;
        } else {
            return VerdictStatus.BAD;
        }
    }


}
