import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

/**
 * Computer Network Final Project
 *
 * @author Emily Hao
 * Server Side
 */
public class ServerChat {

    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();
    /**
     * This set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> userMsg = new HashSet<PrintWriter>();
    private static Map<String, PrintWriter> UserInfo = new Hashtable<>();
    /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }
        public void sendMessage(String message) {
            System.out.println(name + ": " + message);
            String[] split = message.split("#");
            if (split.length > 1) {
                String[] users = split[0].split("@");
                if (users.length < 2) {
                    return;
                }
                String userName = users[1].trim();
                String msg = name + ": " + split[1];
                if ("".equals(userName) || "ALL".equals(userName.toUpperCase())) {

                    for (PrintWriter messager : userMsg) {
                         messager.println("MESSAGE " + msg);
                    }
                } else {//whisper to specific member
                    for (String user : users) {
                        if (!"".equals(user.trim())) {
                            sendMessageToSomeBody(name, user.trim(), msg);
                        }
                    }
                }
            }
        }
        public void sendMessageToSomeBody(String senderName, String username, String message) {
            if (!UserInfo.containsKey(username)) {
                UserInfo.get(senderName).println("        Sorry, this user does not exist in this chat room");
            }
            if (UserInfo.containsKey(username)) {
                UserInfo.get(senderName).println("SEND  " + message);
                UserInfo.get(username).println("MESSAGE " + message);
            }
        }
        public void run() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                // Request a name from this client.
                while (true) {
                    out.println("NickName");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (UserInfo) {
                        if (!UserInfo.containsKey(name)) {
                            //names.add(name);
                            break;
                        }
                    }
                }
                // Now that a successful name has been chosen, add to the sets and table
                out.println("OKName");
                userMsg.add(out);
                UserInfo.put(name, out);
                //display
                for (PrintWriter messager : userMsg) {
                    messager.println("          " + name + " entered the chat");
                    messager.println("          " + "Current people on this chat:" + UserInfo.keySet());
                }
                System.out.println(name + " entered the chat");
                System.out.println("Current people on this chat:" + UserInfo.keySet());
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    sendMessage(input);
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // Client Disconnect. Remove the client name and its userMsg from the set and the table and close its socket.
                if (name != null && out != null ) {
                    userMsg.remove(out);
                    UserInfo.remove(name, out);
                    System.out.println(name + " has left the chat");
                    System.out.println("currant people in the chat are :" + UserInfo.keySet());
                }
                for (PrintWriter messager : userMsg) {
                    messager.println("          " + name + " has left the chat");
                    messager.println("          " + "currant people in the chat are :" + UserInfo.keySet());
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}