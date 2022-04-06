package server.messages;

import server.ConnectionThread;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor Lennard Boetcher
 * Main Network Message Class
 */
public abstract class Message {

    /** List of all available message types. Used to find the corresponding Network Message Class when receiving a Network Message */
    private static final Map<MessageType, Class<? extends Message>> messageTypes = new HashMap<>();
    /** Character / String, which separates every bit of Data in the Text Message, which are sent to Client / Server */
    protected static final String SEPARATOR_CHAR = "\n";

    private MessageType type;

    /**
     * Initialize All Messages
     */
    static {
        for (MessageType type : MessageType.values()) {
            messageTypes.put(type, type.getClazz());
        }
    }

    /**
     * Parse the incoming Data to a Network Message. Creates a new Object of the corresponding Class for the Network Message received. Returns null if parsing failed.
     * @param data Plain Text, which contains the Data of the Message
     * @return Parsed Network Message
     */
    public static Message parseNetworkMessage(String data) {
        if (data == null || data.length() <= 0)
            return null;
        String[] parts = data.split(SEPARATOR_CHAR, -1);
        String type = parts[0];
        MessageType messageType = MessageType.fromString(type);
        if (messageType == MessageType.UNKNOWN)
            return null;
        Class<? extends Message> clazz = messageTypes.get(messageType);
        if (clazz == null)
            return null;
        String[] rest = Arrays.copyOfRange(parts, 1, parts.length);
        try {
            Message mess = clazz.getDeclaredConstructor().newInstance();
            if (mess.parseFromNetworkMessage(rest))
                return mess;
            System.err.println("Failed to parse Network Message " + type + " Parts: " + parts.length + " Rest: " + rest.length);;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Message(MessageType type) {
        this.type = type;
    }

    /**
     * Create the Plain text message to send via the Network, which will be parsed on the other Connections end
     * @return String which represents the Network Message and can be parsed
     */
    public String toNetworkMessage() {
        return type.toString() + SEPARATOR_CHAR + getSendMessage();
    }

    /**
     * Parse the Network Specific Fields
     * @param message already broken down into the components by the Main Message class. Each String represents a part of the Specific Message data
     * @return true if the message was parsed successfully
     */
    protected abstract boolean parseFromNetworkMessage(String[] message);

    /**
     * Convert the Network Message to a string, that can be sent via the connection.
     * @return Network String
     */
    protected abstract String getSendMessage();

    /**
     * Generate a printable string for the console
     * @return string, which can be printed to the console to get the most important information to the Message
     */
    protected abstract String toPrintableString();

    @Override
    public String toString() {
        return type + ": " + toPrintableString();
    }

    /**
     * Get the Type of the Message
     * @return MessageType
     */
    public MessageType getType() {
        return type;
    }
}
