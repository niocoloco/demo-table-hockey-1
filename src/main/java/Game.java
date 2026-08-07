import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game {
    Player P1;
    Player P2;

    int score_P1 = 0;
    int score_P2 = 0;

    //stats
    int shots_on_goal_P1 = 0;
    int shots_on_goal_P2 = 0;

    double avg_disk_speed_km_h = 0;

    double accuracy_P1 = 0;
    double accuracy_P2 = 0;

    // game options

    int game_lenght_s;

    public Game(Player p1, Player p2, int game_lenght_s) {
        P1 = p1;
        P2 = p2;
        this.game_lenght_s = game_lenght_s;
    }

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Random random = new Random();

    public void startSimulation () {
        System.out.println("INIZIO PARTITA: ["+P1.username+"] VS ["+P2.username+"]");
        scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    int elapsed_time = 0;

    private void tick () {
        elapsed_time++;
        if (elapsed_time == game_lenght_s) {
            scheduler.shutdown();
            System.out.println("PARTITA TERMINATA");
            System.out.println("Risultato:  ["+P1.username+"] " + score_P1 + " : ["+P2.username+"] " + score_P2);
            if (score_P1 > score_P2){
                System.out.println(P1.username + " ha vinto la partita!");
            }
            else if (score_P1 < score_P2){
                System.out.println(P2.username + " ha vinto la partita!");
            }
            else if (score_P1 == score_P2){
                System.out.println("La partita è finita in pareggio!");
            }

            System.out.println("Tiri di " + P1.username + ":" + shots_on_goal_P1);

            if(shots_on_goal_P1 != 0){
                accuracy_P1 = (double) score_P1 / shots_on_goal_P1 * 100.0;
                System.out.println("Precisione di " + P1.username + ": " + new BigDecimal(accuracy_P1).setScale(2, RoundingMode.FLOOR) + "%");
            }
            else {
                System.out.println("Precisione di " + P1.username + ": 0%");
            }

            System.out.println("Tiri di " + P2.username + ":" + shots_on_goal_P2);

            if(shots_on_goal_P2 != 0){
                accuracy_P2 = (double) score_P2 / shots_on_goal_P2 * 100.0;
                System.out.println("Precisione di " + P2.username + ": " + new BigDecimal(accuracy_P2).setScale(2, RoundingMode.FLOOR) + "%");
                // new BigDecimal().setScale(2, RoundingMode.FLOOR)
            }
            else {
                System.out.println("Precisione di " + P2.username + ": 0%");
            }

            System.out.println("Velocità media del disco: " + new BigDecimal((double) avg_disk_speed_km_h / game_lenght_s).setScale(2, RoundingMode.FLOOR) + " km/h");

            return;
        }
        int event;

        event = random.nextInt(100);
        // 50% tiro di P1
        if(event < 50){
            System.out.print(P1.username + " effettua un tiro e... ");

            event = random.nextInt(100);

            // 30% fa goal
            if (event < 30){
                shots_on_goal_P1++;
                score_P1++;
                System.out.println("ha fatto goal!");
            }
            // 30% parata
            else if (event < 60) {
                shots_on_goal_P1++;
                System.out.println(P2.username + " ha parato il tiro!");
            }
            // 40% rimbalzo su sponda
            else {
                System.out.println(" il disco ha effettuato un rimbalzo sulla sponda!");
            }
        }
        else {
            System.out.print(P2.username + " effettua un tiro e... ");

            event = random.nextInt(100);

            // 30% fa goal
            if (event < 30){
                shots_on_goal_P2++;
                score_P2++;
                System.out.println("ha fatto goal!");
            }
            // 30% parata
            else if (event < 60) {
                shots_on_goal_P2++;
                System.out.println(P1.username + " ha parato il tiro!");
            }
            // 40% rimbalzo su sponda
            else {
                System.out.println(" il disco ha effettuato un rimbalzo sulla sponda!");
            }
        }

        avg_disk_speed_km_h += random.nextDouble(25, 60);
    }
}
