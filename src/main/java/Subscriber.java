import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Subscriber {
    //public static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    public static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    //  + è una wildcard, iscriver il subscriber ad ogni topic (es. tavolo-1, tavolo-2, ...)
    public static final String TOPIC = "hockey/+/events";

    //  identificatore del nodo edge (locale)
    String clientId = "EdgeNode-" + System.currentTimeMillis();
    private MqttClient mqttClient;
    private String broker_url;

    public Subscriber(String broker_url) throws MqttException {
        this.broker_url = (broker_url != null && !broker_url.isEmpty()) ? broker_url : BROKER_URL;
        connect();
    }

    private void connect() throws MqttException {
        mqttClient = new MqttClient(broker_url, clientId);
        mqttClient.setCallback(new SubscribeCallback());
        mqttClient.connect();
        mqttClient.subscribe(TOPIC);
        System.out.println("Edge in ascolto su " + broker_url + " | topic " + TOPIC);
    }
}