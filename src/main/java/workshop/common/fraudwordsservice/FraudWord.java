package workshop.common.fraudwordsservice;

public class FraudWord {

    public final String word;

    public FraudWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return "FraudWord{" +
                "word='" + word + '\'' +
                '}';
    }
}
