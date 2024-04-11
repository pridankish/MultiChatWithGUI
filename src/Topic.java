import java.util.ArrayList;
import java.util.List;

public class Topic {
    private String name;
    private List<ClientHandler> subscribers;

    public Topic(String name) {
        this.name = name;
        this.subscribers = new ArrayList<>();
    }

    public void subscribe(ClientHandler clientHandler) {
        subscribers.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        subscribers.remove(clientHandler);
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : subscribers) {
            clientHandler.sendTopicMessage(message);
        }
    }

    public String getName() {
        return name;
    }
}
