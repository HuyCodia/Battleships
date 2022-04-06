package server.messages;

/**
 * @author Lennard Boetcher
 * Class which represents the Message for the End of the Game
 * Used Server -> Client
 */
public class MessageEndGame extends Message {

    private boolean won;
    private String winnerName;

    public MessageEndGame() {
        super(MessageType.END);
    }
    public MessageEndGame(boolean won, String winnerName) {
        super(MessageType.END);
        this.won = won;
        this.winnerName = winnerName;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 2)
            return false;
        won = message[0].equalsIgnoreCase("1");
        winnerName = message[1];
        return true;
    }

    @Override
    protected String getSendMessage() {
        return (won ? "1" : "0") + SEPARATOR_CHAR + winnerName;
    }

    @Override
    protected String toPrintableString() {
        return null;
    }

    public boolean isWon() {
        return won;
    }

    public String getWinnerName() {
        return winnerName;
    }
}
