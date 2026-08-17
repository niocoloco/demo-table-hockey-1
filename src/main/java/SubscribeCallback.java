import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SubscribeCallback implements MqttCallback {

    //  mappa per mantenere i dati delle partite in corso
    //  la chiave è l'identificatore del tavolo
    private Map<String, GameState> activeGames = new HashMap<>();

    //  stato di una partita in corso
    private class GameState {
        int scoreSide1 = 0; // punteggio lato A
        int scoreSide2 = 0; // punteggio lato B
        int shotsSide1 = 0; // tiri effettuati dal lato A
        int shotsSide2 = 0; // tiri effettuati dal lato B
        double sumSpeed = 0.0; // somma delle velocità rilevate
        int speedReadingsCount = 0; // contatore rilevazioni della velocità
        String startTime = ""; // data e ora di inizio
        String endTime = "";   // data e ora di fine
    }

    @Override
    public void connectionLost(Throwable cause) {
        //  errore
        System.err.println("Connessione Edge interrotta. Causa: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        //  conversione da payload java in stringa
        String jsonPayload = new String(message.getPayload());

        //  estrazione id del gioco dalla stringa del topic
        String[] topicParts = topic.split("/");
        if (topicParts.length < 3) return; //   se non contiene tutte le parti (hockey, gmaeId e eventi) ritorna
        String gameId = topicParts[1];

        //  parsing della stringa in JsonObject (classe della libreria Gson)
        JsonObject payloadObj = JsonParser.parseString(jsonPayload).getAsJsonObject();

        //  controlla esistenza di event_type
        if (!payloadObj.has("event_type")) return;

        //  estrazione tipo evento (START, SHOT, ..., END)
        String eventType = payloadObj.get("event_type").getAsString();

        //  estrazione dati dal messaggio
        JsonObject data = payloadObj.has("data") && !payloadObj.get("data").isJsonNull()
                ? payloadObj.get("data").getAsJsonObject()
                : new JsonObject();

        //  gestione della disconnessione anomala del simulatore
        if (eventType.equals("CRASHED")) {
            System.err.println("[EDGE] Simulatore offline.");
            activeGames.remove(gameId); //  eliminazione della partita corrotta
            return;
        }

        //  recupera se esiste un gamestate dalla mappa, se non c'è lo crea
        GameState state = activeGames.computeIfAbsent(gameId, k -> new GameState());

        switch (eventType) {
            case "START":
                System.out.println("[EDGE] Iniziata nuova partita su " + gameId);
                //  sovrascrive i dati presenti. Non può creare errori, è impossibile che due partite inizino sullo stesso tavolo prima della fine della prima.
                GameState newState = new GameState();

                //  data e ora di inizio
                newState.startTime = data.get("startTime").getAsString();
                activeGames.put(gameId, newState);
                break;

            case "SHOT":
                //  estrazione del lato dalla quale arriva il tiro
                String side_field = data.get("side_field").getAsString();

                if (side_field.equals("A")) state.shotsSide1++;
                else state.shotsSide2++;
                break;

            case "GOAL":
                //  estrazione del lato in cui è entrato il puck
                String portaTarget = data.get("goal").getAsString();

                if (portaTarget.equals("B")) {
                    state.scoreSide1++;
                    System.out.println("Goal per il lato A su " + gameId);
                } else if (portaTarget.equals("A")) {
                    state.scoreSide2++;
                    System.out.println("Goal per il lato B su " + gameId);
                }
                break;

            case "SPEED_READ":
                //  estrazione velocità istantanea del puck
                double speed = data.get("km_h").getAsDouble();

                state.sumSpeed += speed;
                state.speedReadingsCount++;
                break;

            case "END":
                state.endTime = data.get("endTime").getAsString();

                System.out.println("\n--- [EDGE] PARTITA TERMINATA SU " + gameId + " ---");

                //  elaborazione ed invio al server
                processAndSendToServer(gameId, state);

                //  eliminazione della partita
                // TODO: potrebbe non essere necessaria, prova ad eliminare
                activeGames.remove(gameId);
                break;

            default:

                System.err.println("Evento sconosciuto: " + eventType);
                break;
        }
    }

    private void processAndSendToServer(String gameId, GameState state) {
        //  calcolo precisione per il lato A
        double accuracySide1 = (state.shotsSide1 > 0) ? ((double) state.scoreSide1 / state.shotsSide1) * 100 : 0.0;

        // calcolo precisione per il lato B
        double accuracySide2 = (state.shotsSide2 > 0) ? ((double) state.scoreSide2 / state.shotsSide2) * 100 : 0.0;

        // velocità media puck
        double avgSpeed = (state.speedReadingsCount > 0) ? (state.sumSpeed / state.speedReadingsCount) : 0.0;

        // determina vincitore
        // TODO: cerca di capire come sostituire LATO A e B con il nome del gicoatore in maniera sensata
        String winner;
        if (state.scoreSide1 > state.scoreSide2) winner = "LATO_A";
        else if (state.scoreSide2 > state.scoreSide1) winner = "LATO_B";
        else winner = "PAREGGIO";

        //  formattazione JSON per l'invio al server
        // Locale.US forza il . al posto di , nei decimali
        String finalRestPayload = String.format(Locale.US,
                "{\n" +
                        "  \"gameId\": \"%s\",\n" +
                        "  \"startTime\": \"%s\",\n" +
                        "  \"endTime\": \"%s\",\n" +
                        "  \"winner\": \"%s\",\n" +
                        "  \"stats_A\": { \"goal\": %d, \"shot\": %d, \"accuracy\": %.2f },\n" +
                        "  \"stats_B\": { \"goal\": %d, \"shot\": %d, \"accuracy\": %.2f },\n" +
                        "  \"avg_speed_km/h\": %.2f\n" +
                        "}",
                gameId, state.startTime, state.endTime, winner,
                state.scoreSide1, state.shotsSide1, accuracySide1,
                state.scoreSide2, state.shotsSide2, accuracySide2,
                avgSpeed
        );

        System.out.println("JSON pronto per l'invio al server:");
        System.out.println(finalRestPayload);
        System.out.println("---------------------------------------------------\n");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}