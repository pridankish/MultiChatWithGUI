import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    // прослушиваем входящие подключения и создаем объект Socket для взаимодействия с ними
    private ServerSocket serverSocket;
    private Map<String, Topic> topics;
    
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.topics = new HashMap<>();
    }

    public void startServer() {
        try {
             while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                // Объект класса, который имплементирует runnable и работает в отдельном потоке 
                ClientHandler clientHandler = new ClientHandler(socket, this);
            
                Thread thread = new Thread(clientHandler);
                // запускает обработчик клиента
                thread.start();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTopic(String topicName) {
        if (!topics.containsKey(topicName)) {
            topics.put(topicName, new Topic(topicName));
        }
    }

    public Topic getTopic(String topicName) {
        return topics.get(topicName);
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
