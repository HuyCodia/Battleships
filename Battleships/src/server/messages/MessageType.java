package server.messages;
/**
 * @author Lennard Boetcher
 * Holds all the Network Messages and their Types
 */
public enum MessageType {

    UNKNOWN(null),
    ERROR(MessageError.class),
    CHAT(MessageChat.class),
    SHOOT(MessageShoot.class),
    HIT(MessageHit.class),
    LOBBY_CREATE(MessageLobbyCreate.class),
    LOBBY_JOIN(MessageLobbyJoin.class),
    LOBBY_LEAVE(MessageLobbyLeave.class),
    LOBBY_STATE(MessageLobbyState.class),
    PLACE_SHIPS(MessagePlaceShips.class),
    END(MessageEndGame.class),
    TURN(MessageTurn.class);

    /**
     * Get the Message Type of the provided string. Uses equalsIgnoreCase to check for string equality
     * @param type String which should be mapped to a MessageType
     * @return the MessageType of the String or UNKNOWN if not found
     */
    public static MessageType fromString(String type) {
        for (MessageType mType : values()) {
            if (mType.toString().equalsIgnoreCase(type))
                return mType;
        }
        return UNKNOWN;
    }

    private Class<? extends Message> clazz;

    MessageType(Class<? extends  Message> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends Message> getClazz() {
        return clazz;
    }

}
