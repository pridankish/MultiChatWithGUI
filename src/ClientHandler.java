import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    // Сокет, который передается из серверного класса, используется для соединения
    private Socket socket;
    private Server server;
    // Отправка/Получение данных клиенту 
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    public static ArrayList<Topic> subscribedTopics;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.server = server;
            this.subscribedTopics = new ArrayList<>();
            this.clientUsername = bufferedReader.readLine();
            System.out.println("User " + clientUsername + " connected.");

            String topicsToSubscribe = bufferedReader.readLine();
            while (topicsToSubscribe != null && topicsToSubscribe.startsWith("SUBSCRIBE:")) {
                String topicName = topicsToSubscribe.substring("SUBSCRIBE:".length()).trim();
                subscribeToTopic(topicName);
                topicsToSubscribe = bufferedReader.readLine();
            }

            if (topicsToSubscribe != null) {
                bufferedWriter.write("Enter your message (topic:message):\n");
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        try {
            while ((messageFromClient = bufferedReader.readLine()) != null) {
                if (messageFromClient.startsWith("CREATE:")) {
                    String topic = messageFromClient.substring("CREATE:".length());
                    server.createTopic(topic); // Вызываем метод createTopic из класса Server
                    bufferedWriter.write("CREATED:" + topic);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } else if (messageFromClient.startsWith("SUBSCRIBE:")) {
                    String topic = messageFromClient.substring("SUBSCRIBE:".length());
                    subscribeToTopic(topic);
                    bufferedWriter.write("SUBSCRIBED:" + topic);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } else {
                    broadcastTopicMessage(messageFromClient);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastTopicMessage(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String topicName = parts[0].trim();
            String messageContent = message.substring(topicName.length() + 1);
            Topic topic = server.getTopic(topicName.substring(0));

            if (topic != null) {
                topic.broadcastMessage(clientUsername + ": " + messageContent);
            }
        }
    }

    public void subscribeToTopic(String topicName) throws IOException {
        Topic topic = server.getTopic(topicName);
        if (topic != null) {
            topic.subscribe(this);
            subscribedTopics.add(topic);
            bufferedWriter.write("Subscribe to topic: " + topicName + "\n");
            bufferedWriter.flush();
        } else {
            bufferedWriter.write("Topic not found: " + topicName + "\n");
            bufferedWriter.flush();
        }
    }

    public void sendTopicMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
