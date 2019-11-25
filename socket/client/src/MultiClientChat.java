import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Computer Network Final Project
 *
 * @author Emily Hao
 * Client Side
 */
public class MultiClientChat {

    BufferedReader in;
    PrintWriter out;
    // set the default GUI size
    JFrame frame = new JFrame("Emily's Chatroom");
    JTextField textField = new JTextField(280);
    JTextArea messageArea = new JTextArea(16, 80);

    private MultiClientChat FrameUtils;

    public MultiClientChat() {

        // client GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

        textField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server.
             */
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }
    /**
     * the user would enter a non-duplicated name
     */
    private String enterName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Choose a screen name",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * this is to vibrate the widow when there is a new message comes in
     * @param frame
     */
    public static void windowShake(Frame frame) {
        try {
            final int X = frame.getLocationOnScreen().x;
            final int Y = frame.getLocationOnScreen().y;
            for (int i = 0; i < 5; i++) {
                Thread.sleep(10);
                frame.setLocation(X, Y + 5);
                Thread.sleep(10);
                frame.setLocation(X, Y - 5);
                Thread.sleep(10);
                frame.setLocation(X + 5, Y);
                Thread.sleep(10);
                frame.setLocation(X, Y);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {
        try {
            InetAddress address = InetAddress.getLocalHost();
            Socket socket = new Socket(address, 9001);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                System.out.println(line);
                if (line.startsWith("NickName")) {
                    out.println(enterName());
                } else if (line.startsWith("OKName")) {
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    System.out.println("new message!");
                    FrameUtils.windowShake(frame);
                    messageArea.append(line.substring(8) + "\n");
                } else if (line.startsWith("SEND")) {
                    messageArea.append(line.substring(8) + "\n");
                }else {
                    messageArea.append(line.substring(8) + "\n");
                }
            }
        } catch (NullPointerException e) {
            messageArea.append("Chatroom is closed, good bye" + "\n");
            System.out.println("Server has terminated, good bye");
        }
    }
    /**
     * the main function
     */
    public static void main(String[] args) throws Exception {
        MultiClientChat client = new MultiClientChat();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();

    }
}