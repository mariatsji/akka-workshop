package workshop.part2b;

import akka.actor.AbstractActor;
import javaslang.collection.List;
import workshop.common.fraudwordsservice.FraudWord;
import workshop.common.fraudwordsservice.FraudWordService;

public class FraudWordActor extends AbstractActor {

    private final FraudWordService fraudWordService;

    public FraudWordActor(FraudWordService fraudWordService) {
        this.fraudWordService = fraudWordService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ExamineWords.class, m -> {
                sender().tell(new ExamineWordsResult(fraudWordService.examineWords(m.words)), sender());
            })
            .build();
    }

    public static class ExamineWords {
        public final List<String> words;

        public ExamineWords(List<String> words) {
            this.words = words;
        }
    }

    public static class ExamineWordsResult {
        public final List<FraudWord> fraudWords;

        public ExamineWordsResult(List<FraudWord> fraudWords) {
            this.fraudWords = fraudWords;
        }
    }
}
