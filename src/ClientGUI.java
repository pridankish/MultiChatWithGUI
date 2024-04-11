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

        JPanel toPanel = new JPanel();
        toPanel.setLayout(new FlowLayout());

        JButton createButton = new JButton("Create Topic");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                createTopic();
                } catch (IOException er) {
                    er.printStackTrace();
                }
            }
        });
        toPanel.add(createButton);

        JButton subscribeButton = new JButton("Subscribe to Topic");
        subscribeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            subscribeToTopic();
        }
        });
        toPanel.add(subscribeButton);

        frame.add(toPanel, BorderLayout.NORTH);

        // Отображение фрейма
        frame.setVisible(true);

        try {
            socket = new Socket("localhost", 1234);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            listenForTopicSubscriptions();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Отправка сообщений ClientHandler'у
    public void sendMessage() {
        String topic = JOptionPane.showInputDialog(frame, "Enter topic for your message: ");
        if (topic != null && !topic.isEmpty()) {
            String messageToSend = textField.getText();
            if (!messageToSend.isEmpty()) {
                try {
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    textField.setText("");

                    // Добавляем собственное сообщение в текстовую область
                    messageArea.append(username + ": " + messageToSend + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void listenForTopicSubscriptions() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String topics = bufferedReader.readLine();
                    String[] topicsToSubscribe = topics.split(",");

                    for (String topic : topicsToSubscribe) {
                        bufferedWriter.write("SUBSCRIBE: " + topic.trim() + "\n");
                        bufferedWriter.flush();
                    }

                    listenForMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
    
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        if (msgFromGroupChat.startsWith("CREATED:")) {
                            String topic = msgFromGroupChat.substring("CREATED:".length());
                            JOptionPane.showMessageDialog(frame, "Topic created: " + topic);
                        } else if (msgFromGroupChat.startsWith("SUBSCRIBED:")) {
                            String topic = msgFromGroupChat.substring("SUBSCRIBED:".length());
                            JOptionPane.showMessageDialog(frame, "Subscribed to topic: " + topic);
                        } else {
                            messageArea.append(msgFromGroupChat + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    

    public void subscribeToTopic() {
        String topic = JOptionPane.showInputDialog(frame, "Enter topic to subscribe:");
        if (topic != null && !topic.isEmpty()) {
            try {
                bufferedWriter.write("SUBSCRIBE:" + topic);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createTopic() throws IOException {
        String topic = JOptionPane.showInputDialog(frame, "Enter topic name to create:");
        bufferedWriter.flush();
        if (topic != null && !topic.isEmpty()) {
            try {
                bufferedWriter.write("CREATE:" + topic);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        String username = JOptionPane.showInputDialog(null, "Enter your username for the group chat: ");
        new ClientGUI(username);
    }
}
