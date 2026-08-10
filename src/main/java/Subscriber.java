import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Subscriber {
    public static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    public static final String TOPIC = "hockey/game/summary";

    String clientId = "GameSubscriber-" + System.currentTimeMillis();

    private MqttClient mqttClient;
    private String broker_url;

    public Subscriber(String broker_url) throws MqttException {
        this.broker_url = (broker_url != null && !broker_url.isEmpty()) ? broker_url : BROKER_URL;

        connect();
    }

    private void connect() throws MqttException {
        mqttClient = new MqttClient(broker_url, clientId);
        // TODO
        mqttClient.setCallback(new SubscribeCallback());

        mqttClient.connect();

        mqttClient.subscribe(TOPIC);
        System.out.println("Connected to " + broker_url + " with topic " + TOPIC);
    }
}
