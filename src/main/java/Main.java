import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    public static void main(String[] args) throws MqttException {
        Game game = new Game(new Player ("A"), new Player("B"), 3);
        game.startSimulation();
    }
}
