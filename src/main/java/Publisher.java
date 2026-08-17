import org.eclipse.paho.client.mqttv3.*;

public class Publisher {
    //  URL del broker
    //public static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    public static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    private MqttClient client;
    private String broker_url;
    private String topic;

    public Publisher(String broker_url, String gameId) throws MqttException {
        //  se non modificato utilizza il link standard di mosquitto
        this.broker_url = (broker_url != null && !broker_url.isEmpty()) ? broker_url : BROKER_URL;

        //  gameId identifica un tavolo da gioco (es. tavolo-1)
        this.topic = "hockey/" + gameId + "/events";
        connect();
    }

    private void connect() {
        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setConnectionTimeout(10);

            //  fine inaspettata della partita
            String lwtMessage = "{\"event_type\": \"CRASHED\", \"data\": {}}";
            connOpts.setWill(this.topic, lwtMessage.getBytes(), 1, false);

            //  identificatore univoco
            String clientId = "SensorPublisher-" + System.currentTimeMillis();
            client = new MqttClient(this.broker_url, clientId);
            client.connect(connOpts);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    //  pubblicazione evento nel topic
    public void publishEvent(String jsonPayload) throws MqttException {
        if(client != null && client.isConnected()) {
            MqttMessage message = new MqttMessage(jsonPayload.getBytes());
            message.setQos(1);
            client.publish(this.topic, message);
        } else {
            System.err.println("Impossibile inviare: Client MQTT non connesso.");
        }
    }

    //  disconnessione del publisher
    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("Disconnessione completata.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}