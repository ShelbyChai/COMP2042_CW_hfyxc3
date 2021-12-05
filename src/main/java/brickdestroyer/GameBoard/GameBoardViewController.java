package brickdestroyer.GameBoard;

import brickdestroyer.Actor.Ball;
import brickdestroyer.Actor.Brick;
import brickdestroyer.Actor.Player;
import brickdestroyer.BrickDestroyerApplication;
import brickdestroyer.DebugConsole.DebugConsoleController;
import brickdestroyer.DebugConsole.DebugConsoleModel;
import brickdestroyer.PauseMenu.PauseMenuController;
import brickdestroyer.PauseMenu.PauseMenuModel;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;


public class GameBoardViewController{
    public static final int DEF_WIDTH = 600;
    public static final int DEF_HEIGHT = 450;

    private final GameBoardModel gameBoardModel;
    private String message;
    private DebugConsoleController debugConsoleController;
    private PauseMenuController pauseMenuController;

    final private Canvas canvas;
    private String userKeyInput = "";
    final private GraphicsContext gc;
    private volatile boolean isTimerRunning;


    public GameBoardViewController() {

        isTimerRunning = false;
        message = "";
        gameBoardModel = new GameBoardModel(new Rectangle(0, 0, DEF_WIDTH, DEF_HEIGHT), new Point2D(300, 430));

        gameBoardModel.nextLevel();

        canvas = new Canvas(DEF_WIDTH, DEF_HEIGHT);
        canvas.setFocusTraversable(true);

        gc = canvas.getGraphicsContext2D();

        keyPressed(canvas);
        keyReleased(canvas);

        paint(gc, gameBoardModel);
    }

    public Scene getGameScene() {
        return new Scene(new StackPane(canvas));
    }

    final private AnimationTimer animationTimer = new AnimationTimer() {

        private long lastUpdate = 0;
        // Default: 10_000_000
        final private static long TIMER_DELAY = 1_000_000;

        @Override
        public void handle(long currentNanoTimer) {
            if (currentNanoTimer - lastUpdate >= TIMER_DELAY) {
                paint(gc, gameBoardModel);

                setUserKeyInput();
                gameBoardModel.move();
                gameBoardModel.findImpacts();

                repaintMessage(String.format("Bricks: %d Balls %d", gameBoardModel.getBrickCount(), gameBoardModel.getBallCount()));

                if (gameBoardModel.isBallLost()) {
                    if (gameBoardModel.ballEnd()) {
                        gameBoardModel.wallReset();
                        repaintMessage("Game Over");
                    }
                    gameBoardModel.resetPoint();
                    paint(gc, gameBoardModel);
                    animationTimer.stop();

                } else if (gameBoardModel.isDone()) {
                    if (gameBoardModel.hasLevel()) {
                        repaintMessage("Go to Next Level");
                        animationTimer.stop();
                        gameBoardModel.resetPoint();
                        gameBoardModel.wallReset();
                        gameBoardModel.nextLevel();

                    } else {
                        repaintMessage("All WALLS DESTROYED");
                        animationTimer.stop();
                    }
                }
                lastUpdate = currentNanoTimer;
            }
        }

        @Override
        public void start(){
            super.start();
            isTimerRunning = true;
        }

        @Override
        public void stop(){
            super.stop();
            isTimerRunning = false;
        }
    };

    private void paint(GraphicsContext gc, GameBoardModel gameBoardModel) {
        gc.clearRect(0,0,DEF_WIDTH,DEF_HEIGHT);
        drawBall(gameBoardModel.getBall(),gc);

        gc.setFill(Color.BLUE);
        gc.fillText(message, 250,225);

        for(Brick brick : gameBoardModel.getBrick())
            if(brick.isBroken()) {
                drawBrick(brick, gc);
                drawCrack(brick.getCrackPath(), gc);
            }

        drawPlayer(gameBoardModel.getPlayer(),gc);
    }

    private void drawCrack(Path crackPath, GraphicsContext gc) {
        if (crackPath != null && !crackPath.getElements().isEmpty()) {

            gc.beginPath();
            for (int step = 0; step < Brick.DEF_STEPS; step++) {
                String[] positionXY = parseStringPath(crackPath, step);
                if (step == 0)
                    gc.moveTo(Double.parseDouble(positionXY[0]), Double.parseDouble(positionXY[1]));
                gc.lineTo(Double.parseDouble(positionXY[0]), Double.parseDouble(positionXY[1]));
            }
            gc.fill();
            gc.closePath();
            gc.stroke();
        }
    }

    private String[] parseStringPath(Path crackPath, int index) {
        String path = crackPath.getElements().get(index).toString();
        String value = path.replaceAll("[a-zA-Z]","").replaceAll("[=]","").replaceAll("[\\[-\\]]","");
        return value.split(", ");
    }

    private void drawBrick(Brick brick, GraphicsContext gc) {
        gc.setFill(brick.getInnerColor());
        gc.fillRect(brick.getPosition().getX(),brick.getPosition().getY(),brick.getSize().getWidth(),brick.getSize().getHeight());

        gc.setFill(brick.getBorderColor());
        gc.strokeRect(brick.getPosition().getX(),brick.getPosition().getY(),brick.getSize().getWidth(),brick.getSize().getHeight());
    }

    private void drawPlayer(Player player, GraphicsContext gc) {
        gc.setFill(player.getInnerColor());
        gc.fillRect(player.getXUpperLeft(),player.getYUpperLeft(),player.getWidth(), player.getHeight());

        gc.setStroke(player.getBorderColor());
        gc.strokeRect(player.getXUpperLeft(), player.getYUpperLeft(), player.getWidth(), player.getHeight());
    }

    private void drawBall(Ball ball, GraphicsContext gc) {
        gc.setFill(ball.getInnerColor());
        gc.fillOval(ball.getUpperLeftX(),ball.getUpperLeftY(),ball.getRadius(),ball.getRadius());

        gc.setStroke(ball.getBorderColor());
        gc.strokeOval(ball.getUpperLeftX(),ball.getUpperLeftY(),ball.getRadius(),ball.getRadius());
    }

    private void setUserKeyInput(){
        if (userKeyInput.contains("A")) {
            gameBoardModel.getPlayer().moveLeft();
            gameBoardModel.getPlayer().move();
        }
        else if (userKeyInput.contains("D")) {
            gameBoardModel.getPlayer().moveRight();
            gameBoardModel.getPlayer().move();
        }
        else {
            gameBoardModel.getPlayer().stop();
        }
    }

    private void keyPressed(Canvas canvas) {
        canvas.setOnKeyPressed(keyEvent->{
            switch(keyEvent.getCode()){
                case A:
                    userKeyInput = "A";
                    break;

                case D:
                    userKeyInput = "D";
                    break;

                case ESCAPE:
                    animationTimer.stop();

                    Stage primaryStage = (Stage)((Node)keyEvent.getSource()).getScene().getWindow();

                    FXMLLoader pauseMenuLoader = new FXMLLoader(BrickDestroyerApplication.class.getResource("PauseMenuView.fxml"));
                    Stage pauseMenu = new Stage(StageStyle.TRANSPARENT);

                    primaryStage.getScene().getRoot().setEffect(new GaussianBlur());

                    try {
                        Scene scene = new Scene(pauseMenuLoader.load());
                        scene.getStylesheets().add(BrickDestroyerApplication.class.getResource("PauseMenuStyle.css").toExternalForm());
                        pauseMenu.setScene(scene);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    pauseMenu.initModality(Modality.APPLICATION_MODAL);
                    pauseMenu.initOwner(primaryStage);
                    pauseMenu.setX(primaryStage.getX());
                    pauseMenu.setY(primaryStage.getY());
                    pauseMenu.show();

                    PauseMenuModel pauseMenuModel = new PauseMenuModel(primaryStage, gameBoardModel, this);
                    PauseMenuController pauseMenuController = pauseMenuLoader.getController();
                    pauseMenuController.setPauseMenuModel(pauseMenuModel);


                    break;

                case SPACE:
                    if(isTimerRunning)
                        animationTimer.stop();
                    else
                        animationTimer.start();
                    break;

                case F1:
                    if (keyEvent.isShiftDown() && keyEvent.isAltDown()){
                        animationTimer.stop();

                        Stage debugConsole = new Stage();
                        FXMLLoader debugConsoleLoader = new FXMLLoader(BrickDestroyerApplication.class.getResource("DebugConsoleView.fxml"));

                        try {
                            debugConsole.setScene(new Scene(debugConsoleLoader.load()));
                        } catch (IOException ioException) {
                            ioException.printStackTrace();

                        }
                        // TODO Set Icon for the debug Console: https://icons8.com/icons/set/debug-console
                        debugConsole.setTitle("Debug Console");
                        debugConsole.setResizable(false);
                        debugConsole.initModality(Modality.APPLICATION_MODAL);
                        debugConsole.initOwner(((Node)keyEvent.getSource()).getScene().getWindow());
                        debugConsole.show();

                        DebugConsoleModel debugConsoleModel = new DebugConsoleModel(gameBoardModel);
                        DebugConsoleController debugConsoleController = debugConsoleLoader.getController();
                        debugConsoleController.setDebugConsoleModel(debugConsoleModel);

                    }
                    break;

                default:
                    break;
            }
        });
    }

    private void keyReleased(Canvas canvas) {
        canvas.setOnKeyReleased(e-> userKeyInput = "");
    }

    public void repaintMessage(String message) {
        this.message = message;
        paint(gc, gameBoardModel);
    }
}
