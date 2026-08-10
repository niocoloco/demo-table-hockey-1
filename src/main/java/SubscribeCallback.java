import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.security.auth.callback.Callback;
import com.google.gson.Gson;

public class SubscribeCallback implements MqttCallback {

    private final Gson gson = new Gson();

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String jsonPayload = new String(message.getPayload());

        if(jsonPayload.contains("CRASHED")) {
            System.out.println("Il simulatore si è interrotto in maniera inaspettata.");
            return;
        }

        GameReport report = gson.fromJson(jsonPayload, GameReport.class);

        System.out.print("Esito partita: ");
        if(report.winner.equals("PAREGGIO")){
            System.out.println("Pareggio");
        }
        else {
            System.out.println(report.winner + " è il vincitore!");
        }

        System.out.println("Giocatore 1: " + report.player1.name);
        System.out.println("\tGol fatti: " + report.player1.score);
        System.out.println("\tTiri totali: " + report.player1.shots);
        System.out.println("\tPrecisione: " + report.player1.accuracy + "%");
        System.out.println("Giocatore 2: " + report.player2.name);
        System.out.println("\tGol fatti: " + report.player2.score);
        System.out.println("\tTiri totali: " + report.player2.shots);
        System.out.println("\tPrecisione: " + report.player2.accuracy + "%");
        System.out.println("\tVelocità media disco: " + report.avg_disk_speed_km_h + " km/h");

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}
