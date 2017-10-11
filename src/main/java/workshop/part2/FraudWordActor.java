package workshop.part2;

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
            .build();
    }

    // Request message type
    public static class ExamineWords {
        public final List<String> words;

        public ExamineWords(List<String> words) {
            this.words = words;
        }
    }

    // Reply message type
    public static class ExamineWordsResult {
        public final List<FraudWord> fraudWords;

        public ExamineWordsResult(List<FraudWord> fraudWords) {
            this.fraudWords = fraudWords;
        }
    }
}
