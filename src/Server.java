import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    // прослушиваем входящие подключения и создаем объект Socket для взаимодействия с ними
    private ServerSocket serverSocket;
    
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
             while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                // Объект класса, который имплементирует runnable, работает в отдельном потоке 
                ClientHandler clientHandler = new ClientHandler(socket);
            
                Thread thread = new Thread(clientHandler);
                // запускает обработчик клиента
                thread.start();
            }
        } catch(Exception e) {

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

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
