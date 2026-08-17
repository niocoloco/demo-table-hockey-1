import org.eclipse.paho.client.mqttv3.MqttException;

public class MainEdge {
    public static void main(String[] args) {
        try {
            System.out.println("Avvio Edge Node...");

            Subscriber edgeNode = new Subscriber("");

            System.out.println("Edge Node in esecuzione. In attesa di eventi MQTT.");
        } catch (MqttException e) {
            System.err.println("Errore fatale: Impossibile avviare l'Edge. " + e.getMessage());
            e.printStackTrace();
        }
    }
}