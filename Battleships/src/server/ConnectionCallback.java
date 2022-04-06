package server;

import server.messages.Message;

/**
 * @author Lennard Boetcher
 * Interface to implement the most important Callback function for receiving Network Messages
 */
public interface ConnectionCallback {

    void onDisconnectEvent(ConnectionThread con);
    void onMessageReceived(ConnectionThread con, Message message);
}
