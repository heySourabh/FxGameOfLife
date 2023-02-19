package in.spbhat;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Sourabh Bhat <spbhat.in>
 */
public class FxGameOfLife extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double SCALE = 5;
    private static final int FPS = 10;
    private static final double LIFE_PROBABILITY = 0.1;
    private static GraphicsContext gc;

    private static int[][] newState = new int[WIDTH][HEIGHT];
    private static int[][] currentState = new int[WIDTH][HEIGHT];

    private Parent createContent() {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged((event) -> {
            double x1 = (WIDTH - SCALE * WIDTH) / 2;
            double x2 = (WIDTH + SCALE * WIDTH) / 2;
            double y1 = (HEIGHT - SCALE * HEIGHT) / 2;
            double y2 = (HEIGHT + SCALE * HEIGHT) / 2;
            int x = (int) (WIDTH * (event.getX() - x1) / (x2 - x1));
            int y = (int) (HEIGHT * (event.getY() - y1) / (y2 - y1));
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    currentState[x + i][y + j] = 1;
                }
            }
        });
        return new StackPane(canvas);
    }

    private void initState() {
        Random rnd = new Random();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                currentState[x][y] = rnd.nextDouble() < LIFE_PROBABILITY ? 1 : 0;
            }
        }
    }

    private void drawCanvas() {
        gc.save();
        gc.translate(WIDTH * (1 - SCALE) / 2, HEIGHT * (1 - SCALE) / 2);
        gc.scale(SCALE, SCALE);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setFill(Color.BLACK);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (currentState[x][y] == 1) {
                    gc.fillRect(x, y, 1, 1);
                }
            }
        }
        // gc.setFill(Color.RED);
        // gc.fillRect(WIDTH / 2 - 20, HEIGHT / 2 - 20, 40, 40);
        gc.restore();
    }

    private boolean onEdge(int i, int j) {
        return i == 0 || j == 0 || i == WIDTH - 1 || j == HEIGHT - 1;
    }

    private int countNeighbors(int[][] grid, int x, int y) {
        if (onEdge(x, y)) {
            return 0;
        }
        int numNeighbors = 0;
        for (int j = -1; j <= 1; j++) {
            for (int i = -1; i <= 1; i++) {
                numNeighbors += grid[x + i][y + j];
            }
        }
        return numNeighbors - grid[x][y];
    }

    private void updateState() {
        for (int j = 0; j < HEIGHT; j++) {
            for (int i = 0; i < WIDTH; i++) {
                int numNeighbors = countNeighbors(currentState, i, j);

                newState[i][j] = currentState[i][j];
                if (currentState[i][j] == 1) { // live cell
                    if (numNeighbors < 2 || numNeighbors > 3) {
                        newState[i][j] = 0;
                    }
                } else { // dead cell
                    if (numNeighbors == 3) {
                        newState[i][j] = 1;
                    }
                }
            }
        }
        int[][] tempState = currentState;
        currentState = newState;
        newState = tempState;
    }

    private final AnimationTimer timer = new AnimationTimer() {
        private long then = 0;
        private boolean busy = false;

        @Override
        public void handle(long now) {
            if (busy) {
                return;
            }
            if (then == 0) {
                then = now;
                return;
            }
            double dt = (now - then) * 1e-9; // sec
            if (dt < 1.0 / FPS) {
                return;
            }
            busy = true;
            System.out.println("fps: " + (1 / dt));
            updateState();
            drawCanvas();
            then = now;
            busy = false;
        }
    };

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent(), WIDTH, HEIGHT);
        stage.setScene(scene);
        initState();
        timer.start();
        stage.setTitle("Game of Life!");
        stage.show();
    }
}
