package workshop.common.fraudwordsservice;

import javaslang.collection.List;

public class FraudWordService {

    // returns a list of banned words from any list of words
    public List<FraudWord> examineWords(List<String> words) {
        return words
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(w -> allWords().map(FraudWord::getWord).contains(w))
                .map(FraudWord::new);
    }

    private List<FraudWord> allWords() {
        return List.of("westernunion", "advance", "nigeria", "wiretransfer").map(FraudWord::new);
    }
}
