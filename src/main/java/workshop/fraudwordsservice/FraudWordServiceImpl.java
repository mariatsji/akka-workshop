package workshop.fraudwordsservice;

import javaslang.collection.List;
import javaslang.control.Either;

public class FraudWordServiceImpl implements FraudWordService {

    @Override
    public Either<Throwable, List<FraudWord>> examineWords(List<String> words) {
        return Either.right(
                words
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(w -> allWords().map(FraudWord::getWord).contains(w))
                .map(FraudWord::new));
    }

    private List<FraudWord> allWords() {
        return List.of("westernunion", "advance", "nigeria", "wiretransfer").map(FraudWord::new);
    }
}
