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
import java.awt.Toolkit;

public class ChatClient {
    private String hostname;
    private int port;
    private static String clientName;
    private Socket socket = null;
    private static PrintWriter out = null;
    private BufferedReader in = null;
    //private Dimension screenSize = ToolKit.getDefaultToolKit.getScreenSize();
    private static JTextArea textArea = new JTextArea();
    private static JTextArea textAreaSend = new JTextArea();

    private static class ReturnAction extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
          int keys = e.getKeyCode();
          if (keys == KeyEvent.VK_ENTER) {
            out.println(clientName + ": " + textAreaSend.getText());
            textAreaSend.setText(null);
            textAreaSend.setCaretPosition(0);
          }
        }
      }

    public ChatClient(String hostname, int port, String clientName) {
        this.hostname = hostname;
        this.port = port;
        this.clientName = clientName;
    }

    public void run() {
        try {
            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            //textArea.append(clientName + " has just connected to the server\n");
            out.println(clientName + " has just connected to the server");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while(!socket.isClosed()){
                String line = in.readLine();
                textArea.append(line + "\n");
                if(line == null) break;
                System.out.println(line);
            }

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + hostname);
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error: Error establishing communication with server.");
            System.out.println(e.getMessage());
        }

        try {
            if (out != null) out.close();
	        if (in != null) in.close();
	        if (socket != null) socket.close();
	    } catch (IOException e) {
	        System.out.println("Error closing the streams.");
	    }
    }

    public static void main(String[] args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String clientName = args[2];

        SwingUtilities.invokeLater(() -> {
            BorderLayout layout = new BorderLayout();
//            layout.setHgap(10);
//            layout.setVgap(10);

            JPanel panel = new JPanel();
            panel.setLayout(layout);
            panel.setBorder(BorderFactory.createLineBorder(Color.black));

            JFrame frame = new JFrame("Chat Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            frame.setSize((int) rect.getWidth()/2, (int) rect.getHeight()/2);
            int x = (int) rect.getMaxX() - frame.getWidth();
            int y = 0;
            frame.setLocation(x, y);

            JPanel sendPanel = new JPanel();
            sendPanel.setPreferredSize(new Dimension((int) frame.getWidth()/8, (int) frame.getHeight()/8));
            sendPanel.setLayout(new BorderLayout());
            sendPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            textAreaSend.setLineWrap(true);
            textAreaSend.setFont(new Font("Arial", Font.PLAIN, 30));
            textAreaSend.addKeyListener(new ReturnAction());
            JScrollPane scrollPaneSend = new JScrollPane(textAreaSend);

            JButton sendButton = new JButton("Send");
            sendButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("clicked Send");
                    String message = textAreaSend.getText();
                    System.out.println(message);
                    out.println(clientName + ": " + message);
                    textAreaSend.setText(null);
                    textAreaSend.setCaretPosition(0);
                }
            });
            sendPanel.add(sendButton, BorderLayout.EAST);
            sendPanel.add(scrollPaneSend, BorderLayout.CENTER);

            panel.setLayout(new BorderLayout());

            textArea.setLineWrap(true);
            textArea.setEditable(false);
            textArea.setFont(new Font("Arial", Font.PLAIN, 30));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension((int) frame.getWidth()/2, (int) frame.getHeight()/2));

            //panel.setSize(300,600);
            panel.add(scrollPane, BorderLayout.NORTH);
            panel.add(sendPanel, BorderLayout.SOUTH);
            //panel.setOpaque(true);


            frame.pack();
            frame.setVisible(true);


        });

        ChatClient chatClient = new ChatClient(hostname, port, clientName);
        chatClient.run();
    }
}
