package server.messages;

/**
 * @author Lennard Boetcher
 * Class which represents an Error Message while performing an action
 * Used Server -> Client
 */
public class MessageError extends Message {

    private MessageType messageWithError;
    private String errorMessage = "";

    public MessageError() {
        super(MessageType.ERROR);
    }

    public MessageError(MessageType messageWithError, String errorMessage) {
        super(MessageType.ERROR);
        this.messageWithError = messageWithError;
        this.errorMessage = errorMessage;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 2)
            return false;
        messageWithError = MessageType.fromString(message[0]);
        errorMessage = message[1];
        return true;
    }

    @Override
    protected String getSendMessage() {
        return messageWithError.toString() + SEPARATOR_CHAR + errorMessage;
    }

    @Override
    protected String toPrintableString() {
        return messageWithError + ": " + errorMessage;
    }
}
