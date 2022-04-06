package server.messages;

/**
 * @author Lennard Boetcher
 * Class which represents a Chat Message
 * Used Server -> Client and Client -> Server
 */
public class MessageChat extends Message {

    private String chatMessage;

    protected MessageChat() {
        super(MessageType.CHAT);
    }

    public MessageChat(String chatMessage) {
        super(MessageType.CHAT);
        this.chatMessage = chatMessage;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length <= 0)
            return false;
        chatMessage = message[0];
        return true;
    }

    @Override
    protected String getSendMessage() {
        return chatMessage;
    }

    @Override
    protected String toPrintableString() {
        return getChatMessage();
    }
}
