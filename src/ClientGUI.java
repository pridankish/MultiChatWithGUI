import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientGUI {
    private JFrame frame;
    private JTextField textField;
    private JTextArea messageArea;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Socket socket;
    private String username;

    public ClientGUI(String username) {
        this.username = username;

        frame = new JFrame();
        textField = new JTextField();
        messageArea = new JTextArea();

        // Настройка фрейма
        frame.setTitle("Group Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);

        // Настройка текстовой области
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        // Настройка текстового поля
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Добавление компонентов на фрейм
        frame.add(messageArea, BorderLayout.CENTER);
        frame.add(textField, BorderLayout.SOUTH);

        // Отображение фрейма
        frame.setVisible(true);

        try {
            socket = new Socket("localhost", 1234);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            listenForMessage();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Отправка сообщений ClientHandler'у
    public void sendMessage() {
        try {
            String messageToSend = textField.getText();
            bufferedWriter.write(username + ": " + messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            textField.setText("");

            messageArea.append("You: " + messageToSend + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        messageArea.append(msgFromGroupChat + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        String username = JOptionPane.showInputDialog(null, "Enter your username for the group chat: ");
        new ClientGUI(username);
    }
}

