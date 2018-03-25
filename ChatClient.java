import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private static final int PORT = 1518;
    private String hostname;
    private String username;
    private Socket socket = null;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private JTextArea messageBoard = new JTextArea();
    private JTextArea messageInput = new JTextArea(3,1);

    public ChatClient(String hostname, String username) {
        this.hostname = hostname;
        this.username = username;
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient(args[0], args[1]);
        chatClient.initGUI();        
        chatClient.initClientSocket();
    }

    private void initGUI(){
        messageBoard.setLineWrap(true);
        messageBoard.setEditable(false);

        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        messageInput.setEditable(true);
        messageInput.setPreferredSize(new Dimension(300, 50));
        messageInput.setCaretPosition(0);
        messageInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keys = e.getKeyCode();
                if (keys == KeyEvent.VK_ENTER) {
                    output.println("TRANSMIT " + messageInput.getText().replace("\n", ""));
                    messageInput.setText("");
                    messageInput.setCaretPosition(0);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(messageBoard);
        JScrollPane chatScrollPane = new JScrollPane(messageInput);

        JLabel roomLabel = new JLabel("Room: 0");
        roomLabel.setPreferredSize(new Dimension(100, 20));

        JTextField roomInput = new JTextField();
        roomInput.setPreferredSize(new Dimension(100, 20));
        roomInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER) {
                    if(roomInput.getText().matches("^[a-zA-Z0-9_]+$")){
                        output.println("JOIN " + roomInput.getText().replaceAll("\\s+",""));
                        roomLabel.setText("Room: " + roomInput.getText().replaceAll("\\s+",""));
                    }else{
                        messageBoard.append("*Invalid* : Room must be an alphanumeric string\n");
                    }
                   roomInput.setText("");
                }
            }
        });

        JPanel roomPanel = new JPanel();
        roomPanel.add(roomLabel);
        roomPanel.add(roomInput);

        BorderLayout layout = new BorderLayout();
        layout.setHgap(10);
        layout.setVgap(10);

        JPanel panel = new JPanel();
        panel.setLayout(layout);
        panel.setSize(300,600);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(chatScrollPane, BorderLayout.SOUTH);
        panel.add(roomPanel, BorderLayout.NORTH);
        panel.setOpaque(true);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();

        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(300, 600);
        frame.setLocation((int) rect.getMaxX() - frame.getWidth(), 0);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                try{
                    output.println("EXIT");
                }catch(Exception e){
                    System.out.println(e);
                }
                System.exit(0);
            }
        });

        messageInput.requestFocus();
    }

    public void initClientSocket() {
        try {
            socket = new Socket(hostname, PORT);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.println("ENTER " + username);

            while(true){
                if(input.ready()){
                    String line = input.readLine();
                    if(line == null) break;
                    processLine(line);
                }
            }

            output.println("EXIT");
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + hostname);
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error: Error establishing communication with server.");
            System.out.println(e.getMessage());
        }finally{
            try {
                if (output != null) output.close();
                if (input != null) input.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                System.out.println("Error closing the streams.");
            }
        }
    }

    private void processLine(String inputLine){
        if(inputLine.startsWith("ENTERING")){
            String name = inputLine.substring(inputLine.indexOf(' ') + 1, inputLine.length());
            addToMessageBoard(name + " has entered the room.");
        }else if(inputLine.startsWith("EXITING")){
            String name = inputLine.substring(inputLine.indexOf(' ') + 1, inputLine.length());
            addToMessageBoard(name + " has left the room.");
        }else if(inputLine.startsWith("NEWMESSAGE")){
            inputLine = inputLine.substring(11, inputLine.length());
            String name = inputLine.substring(0, inputLine.indexOf(' '));
            addToMessageBoard(name + ": " + inputLine.substring(inputLine.indexOf(' ') + 1, inputLine.length()));
        }else if(inputLine.startsWith("ACK JOIN")){
            inputLine = inputLine.substring(9, inputLine.length());
            addToMessageBoard("Successfully joined room " + inputLine + ".");
        }else if(inputLine.startsWith("ACK ENTER")){
            addToMessageBoard("Successfully connected to the server.");
        }
    }

    private void addToMessageBoard(String message){
        messageBoard.append(message + "\n");
    }
}
