package server;

import server.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Lennard Boetcher
 * Simple Server handler, which can send and Receive plain text UFT-8 encoded data to the Server
 */
public class ConnectionThread {

    private Socket socket;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private ConnectionCallback callback;
    private boolean disconnected = false;
    private Thread connectionThread = null;
    boolean bot = false;

    /**
     * Connect to the Server specified in the Parameters
     * @param host of the target Server
     * @param port of the target Server
     * @param callback Connection Callback, which will handle Network Messages
     * @return a new Connection Thread Object
     * @throws IOException
     */
    public static ConnectionThread connectToServer(String host, int port, ConnectionCallback callback) throws IOException {
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("Port must be in range of 0 and 65535 !");
        ConnectionThread th = new ConnectionThread();
        th.setCallback(callback);
        th.socket = new Socket();
        th.socket.connect(new InetSocketAddress(host, port));
        th.start();
        return th;
    }

    /**
     * Get a Connection Thread Object for a Client, which connected to the Server
     * @param client Socket of the Client
     * @param callback Connection Callback, which will handle the Network Messages
     * @return a new Connection Thread Object
     */
    public static ConnectionThread newClientToServerConnection(Socket client, ConnectionCallback callback) {
        if (client == null)
            throw new IllegalArgumentException("Client must not be null !");
        ConnectionThread th = new ConnectionThread();
        th.setCallback(callback);
        th.socket = client;
        th.start();
        return th;
    }

    /**
     * Create a new BotPlayer class and let it connect to the local Server (Host: localhost). Uses connectToServer
     * @param port of the Local Server
     * @return a new Connection Thread Object with a bot handeling the Network Messages
     * @throws IOException
     */
    public static ConnectionThread newBotInstance(int port) throws IOException {
        BotPlayer bot = new BotPlayer();
        ConnectionThread con = connectToServer("localhost", port, bot);
        bot.setConnectionThread(con);
        con.bot = true;
        return con;
    }

    private ConnectionThread(){}

    /**
     * Initialize the Input and Output Streams
     * Start the Receive Data Thread
     * Thread will be a Deamon Thread to not block the program from exiting
     */
    private void start() {
        System.out.println("Starting Connection Thread to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        try {
            openStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionThread = new Thread(() -> {
            try {
                while (isConnected()) {
                    Message data = readMessage();
                    if (data == null)
                        continue;
                    callback.onMessageReceived(ConnectionThread.this, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }, "Connection Thread to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    /**
     * Tries to read an incoming Message. Will block the Thread until a Message is received
     * @return a Message or null if the received data was not a valid Network Message
     */
    private Message readMessage() {
        String data = readData();
        if (data == null)
            return null;
        return Message.parseNetworkMessage(data);
    }

    /**
     * Send a Network Message. Main Method to send Data to the Client / Server
     * @param message the network Message, which should be sent to the Client / Server. Must not be null !
     */
    public void sendMessage(Message message) {
        String data = message.toNetworkMessage();
        sendData(data);
    }

    /**
     * Change the callback, which should handle the Network Messages
     * @param callback new Callback. Must not be null !
     */
    public void setCallback(ConnectionCallback callback) {
        if (callback == null)
            throw new IllegalArgumentException("Callback must not be null !");
        this.callback = callback;
    }

    public ConnectionCallback getCallback() {
        return callback;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && socket.isBound() && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown() && !disconnected;
    }

    /**
     * Disconnect from the Server and close all Streams. Will invoke the Callbacks onDisconnectEvent
     */
    public void disconnect() {
        if (disconnected)
            return;
        try {
            disconnected = true;
            System.out.println("Disconnecting from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
            dataIn.close();
            dataOut.close();
            socket.close();
            if (callback != null)
                callback.onDisconnectEvent(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to read plain text data. Will block the Thread until data is received. Will return null, if the connection is closed
     * @return Plain Text Data if received
     */
    private String readData() {
        if (disconnected)
            return null;
        try {
            int length = dataIn.readInt();
            byte[] data = new byte[length];
            dataIn.read(data);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            disconnect();
        }
        return null;
    }

    /**
     * Sends the String of data as a plaintext message to the Server. UTF-8 will be used to encode the Data
     * @param data message to send
     */
    private void sendData(String data) {
        if (disconnected)
            return;
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            dataOut.writeInt(dataBytes.length);
            dataOut.write(dataBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens Input and Output Stream to send and receive data
     * @throws IOException
     */
    private void openStreams() throws IOException {
        dataOut = new DataOutputStream(socket.getOutputStream());
        dataIn = new DataInputStream(socket.getInputStream());
    }

    public boolean isBot() {
        return bot;
    }
}
