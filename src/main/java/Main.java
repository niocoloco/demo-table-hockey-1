import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    public static void main(String[] args) {
        try {
            Game tavolo1 = new Game("tavolo-1", 10);
            tavolo1.startSimulation();

            Game tavolo2 = new Game("tavolo-2", 15);
            tavolo2.startSimulation();

        } catch (MqttException e) {
            System.err.println("Errore simulazione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}