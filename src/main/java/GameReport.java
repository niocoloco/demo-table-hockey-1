public class GameReport {
    public String winner;
    public PlayerData player1;
    public PlayerData player2;
    public double avg_disk_speed_km_h;

    public static class PlayerData {
        public String name;
        public int score;
        public int shots;
        public double accuracy;
    }
}