import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game {
    private Publisher publisher;

    //  nome del gioco (tavolo)
    String gameId;
    //  inizio e fine della partita
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    //  tempo passato
    private int elapsed_time = 0;

    //  durata totale del gioco
    private int game_length_s;

    //  generatore di eventi ogni secondo
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    //  generatore casuale di numeri per simulare le attivazione dei sensori
    private Random rand = new Random();

    public Game(String gameId, int game_length_s) throws MqttException {
        this.game_length_s = game_length_s;
        this.gameId = gameId;
        this.publisher = new Publisher("", this.gameId);
    }

    public void startSimulation() throws MqttException {
        startTime = LocalDateTime.now();
        sendEvent("START", "{\"startTime\": \"" + startTime.toString() + "\"}");
        scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        //  incremento di un secondo nel tempo di gioco trascorso
        elapsed_time++;
        if (elapsed_time >= game_length_s) {
            scheduler.shutdown();
            System.out.println("Tempo scaduto. Sensore invia segnale di fine.");
            endTime = LocalDateTime.now();
            sendEvent("END", "{\"endTime\": \"" + endTime.toString() + "\"}");
            publisher.disconnect();
            return;
        }

        if(rand.nextInt(100) < 50) {
            //  tiro effettuato dal lato A verso il lato B del campo
            System.out.println("Sensore di movimento attivato: tiro dal lato A");
            sendEvent("SHOT", "{\"side_field\": \"A\"}");

            //  il puck entra nella porta 2 (B)
            if (rand.nextInt(100) < 40) {
                System.out.println("Sensore porta B attivato (GOAL)");
                sendEvent("GOAL", "{\"goal\": \"B\"}");
            }
            //  negli altri casi è stato parato o è rimbalzato contro la parete
        }
        else {
            //  tiro effettuato dal lato B verso il lato A del campo
            System.out.println("Sensore di movimento attivato: tiro dal lato B");
            sendEvent("SHOT", "{\"side_field\": \"B\"}");

            //  il puck entra nella porta 1 (A)
            if (rand.nextInt(100) < 40) {
                System.out.println("Sensore porta A attivato (GOAL)");
                sendEvent("GOAL", "{\"goal\": \"A\"}");
            }
            //  negli altri casi è stato parato o è rimbalzato contro la parete
        }

        //  velocità istantanea del puck
        double currentSpeed = 25.0 + (rand.nextDouble() * 35.0);

        String speedStr = String.format(Locale.US, "%.2f", currentSpeed);
        System.out.println("Sensore ottico attivato: rilevato passaggio disco a " + speedStr + " km/h");

        sendEvent("SPEED_READ", "{\"km_h\": " + speedStr + "}");
    }

    private void sendEvent(String eventType, String data) {
        try {
            String jsonPayload = String.format("{\"event_type\": \"%s\", \"data\": %s}", eventType, data);
            publisher.publishEvent(jsonPayload);
        } catch (MqttException e) {
            System.err.println("Errore MQTT: " + e.getMessage());
        }
    }
}
