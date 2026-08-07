public class Main {
    public static void main(String[] args) {
        Game game = new Game(new Player ("A"), new Player("B"), 10);
        game.startSimulation();
    }
}
