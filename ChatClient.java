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
    // default host port
    private static final int PORT = 1518;
    // server hostname
    private String hostname;
    // clients username
    private String username;
    // socket connection
    private Socket socket = null;
    // used for writing to server
    private PrintWriter clientOutput = null;
    // used for reading server input
    private BufferedReader serverInput = null;
    // messageBoard is where incoming messages will be displayed
    private JTextArea messageBoard = new JTextArea();
    // messageOutput is where all outgoing messages will be typed
    private JTextArea messageOutput = new JTextArea(3,1);

    // Initialize the gui and then attempt to make a socket connection
    // @Params: args[0] == hostname and args[0] == username
    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient(args[0], args[1]);
        chatClient.initGUI();        
        chatClient.initClientSocket();
    }

    public ChatClient(String hostname, String username) {
        this.hostname = hostname;
        this.username = username;
    }

    private void initGUI(){
        messageBoard.setLineWrap(true);
        messageBoard.setEditable(false);
        messageOutput.setLineWrap(true);
        messageOutput.setWrapStyleWord(true);
        messageOutput.setEditable(true);
        messageOutput.setPreferredSize(new Dimension(300, 50));
        messageOutput.setCaretPosition(0);
        messageOutput.addKeyListener(new KeyAdapter() {
            // send client message out to the server when the client presses
            // enter key within the input field
            public void keyPressed(KeyEvent e) {
                int keys = e.getKeyCode();
                if (keys == KeyEvent.VK_ENTER) {
                    clientOutput.println("TRANSMIT " + messageOutput.getText().replace("\n", ""));
                    messageOutput.setText("");
                    messageOutput.setCaretPosition(0);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(messageBoard);
        JScrollPane chatScrollPane = new JScrollPane(messageOutput);

        JLabel roomLabel = new JLabel("Room: 0");
        roomLabel.setPreferredSize(new Dimension(100, 20));

        JTextField roomInput = new JTextField();
        roomInput.setPreferredSize(new Dimension(100, 20));
        roomInput.addKeyListener(new KeyAdapter() {
            // submits clients clients request for a room change to the server
            // whe enter key pressed within the room field
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER) {
                    if(roomInput.getText().matches("^[a-zA-Z0-9_]+$")){
                        clientOutput.println("JOIN " + roomInput.getText().replaceAll("\\s+",""));
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
        // use a border layout for entire gui
        BorderLayout layout = new BorderLayout();
        layout.setHgap(10);
        layout.setVgap(10);
        // default jpanel which will house our components
        JPanel panel = new JPanel();
        panel.setLayout(layout);
        panel.setSize(300,600);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(chatScrollPane, BorderLayout.SOUTH);
        panel.add(roomPanel, BorderLayout.NORTH);
        panel.setOpaque(true);
        // get client screen specifications to set location of the gui
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        // initialize jframe
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
                    clientOutput.println("EXIT");
                }catch(Exception e){
                    System.out.println(e);
                }
                System.exit(0);
            }
        });

        messageOutput.requestFocus();
    }

    // Attempt to initialize the socket connection with the server.
    // Assuming everything went well: (specific error handling will be logged
    // to the console) 
    // 1. We send an enter protocol to the server.
    // 2a. We enter a while loop the continuously checks if the input line has 
    //     anything to read. 
    // 2b. If there is input, process the line. 
    // 2c. If the input is null, something went wrong so break out of the loop,
    //     send a disconnect transmission, and close all resources.
    public void initClientSocket() {
        try {
            socket = new Socket(hostname, PORT);
            clientOutput = new PrintWriter(socket.getOutputStream(), true);
            serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientOutput.println("ENTER " + username);

            while(true){
                if(serverInput.ready()){
                    String line = serverInput.readLine();
                    if(line == null) break;
                    processServerInput(line);
                }
            }
            clientOutput.println("EXIT");
            
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + hostname);
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error: Error establishing communication with server.");
            System.out.println(e.getMessage());
        }finally{
            try {
                if (clientOutput != null) clientOutput.close();
                if (serverInput != null) serverInput.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                System.out.println("Error closing the streams.");
            }
        }
    }

    // Process the input lines coming from the server. Read the if statements to understand
    // the logic. substring and indexOf are used to skip over the protocol contained in the 
    // message string.
    private void processServerInput(String inputLine){
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

    // append a message to the clients message board.
    private void addToMessageBoard(String message){
        messageBoard.append(message + "\n");
    }
}
