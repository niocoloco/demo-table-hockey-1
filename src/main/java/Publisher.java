import org.eclipse.paho.client.mqttv3.*;
import java.util.Date;

public class Publisher {
    public static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    public static final String TOPIC = "hockey/game/summary";
    private MqttClient client;
    private Date date = new Date();

    private String broker_url;

    public Publisher(String broker_url) throws MqttException {
        this.broker_url = (broker_url != null && !broker_url.isEmpty()) ? broker_url : BROKER_URL;
        connect();
    }

    private void connect() {
        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setConnectionTimeout(10);

            String lwtMessage = "{\"status\": \"CRASHED\", \"message\": \"Il simulatore si è interrotto\"}";

            connOpts.setWill("hockey/game/summary", lwtMessage.getBytes(), 1, false);

            String clientId = "GamePublisher-" + System.currentTimeMillis();
            client = new MqttClient(this.broker_url, clientId);

            client.connect(connOpts);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void publishGameSummary(String jsonPayload) throws MqttException {
        if(client != null && client.isConnected()) {
            MqttMessage message = new MqttMessage(jsonPayload.getBytes());
            message.setQos(1);

            client.publish(TOPIC, message);
            System.out.println("Message pubblicato su [" + TOPIC + "]: " + jsonPayload);
        }
        else {
            System.err.println("Impossibile inviare: Client MQTT non connesso.");
        }
    }

    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("Disconnessione da MQTT completata.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
