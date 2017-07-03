package examples;

public class Message {

    public final String messageId;
    public final String correlationId;
    public final String content;

    public Message(String messageId, String correlationId, String content) {
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
