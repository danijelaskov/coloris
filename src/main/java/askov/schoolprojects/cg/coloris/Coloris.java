/*
 * Copyright (C) 2019  Danijel Askov
 *
 * This file is part of Coloris.
 *
 * Coloris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coloris is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package askov.schoolprojects.cg.coloris;


import askov.schoolprojects.cg.coloris.gamelogic.GameLogic;
import askov.schoolprojects.cg.coloris.sprites.GameLogo;
import askov.schoolprojects.cg.coloris.sprites.Block;
import askov.schoolprojects.cg.coloris.sprites.GameFont;
import askov.schoolprojects.cg.coloris.sprites.GameOverLabel;
import askov.schoolprojects.cg.coloris.sprites.LabeledValue;
import askov.schoolprojects.cg.coloris.sprites.SquareMatrix;
import askov.schoolprojects.cg.coloris.sprites.Universe;
import askov.schoolprojects.cg.coloris.sprites.Well;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


/**
 *
 * @author Danijel Askov
 */
public class Coloris extends Application {

    private static final double WIDTH = 700;
    private static final double HEIGHT = 650;
    private static final double BLOCK_SIZE = WIDTH / 28;
    private static final double SQUARE_MATRIX_WIDTH = SquareMatrix.DEFAULT_NUM_COLUMNS * BLOCK_SIZE;
    private static final double SQUARE_MATRIX_HEIGHT = (SquareMatrix.DEFAULT_NUM_ROWS + Block.DEFAULT_NUM_SQUARES) * BLOCK_SIZE;

    private static final String RESTART_BUTTON_STYLE = "-fx-background-color: #111100; -fx-border-color: #0000DD; -fx-border-width: 3; "
            + "-fx-background-radius: 14; -fx-border-radius: 14; -fx-cursor: hand;";
    private static final String RESTART_BUTTON_HOVER_STYLE = "-fx-background-color: #000044; -fx-border-color: #7788AA; -fx-border-width: 3; "
            + "-fx-background-radius: 14; -fx-border-radius: 14; -fx-cursor: hand;";

    private final Universe universe = new Universe(WIDTH, HEIGHT);
    private Well well;

    private Block fallingBlock;
    private Block nextBlock;
    private LabeledValue score;

    private Group root;

    private GameLogic gameLogic;
    private AnimationTimer animationTimer;

    private Scene scene;

    private final Button restartButton = new Button("REINICIAR");

    private final Media media = new Media(Thread.currentThread().getContextClassLoader().getResource("music/music.mp3").toExternalForm());
    private final MediaPlayer mediaPlayer = new MediaPlayer(media);

    @Override
    public void start(Stage primaryStage) {
        root = new Group();

        scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnKeyPressed(this::handleKeyPressed);

        // Sem foco, para que as setas do teclado continuem controlando o bloco
        restartButton.setFocusTraversable(false);
        restartButton.setOnAction(event -> startNewGame());

        restartButton.setFont(new GameFont(GameFont.FontType.MAIN_GAME_FONT, 26).getFont());
        restartButton.setTextFill(Color.web("0xEEEE00"));
        restartButton.setPrefHeight(50);
        restartButton.setStyle(RESTART_BUTTON_STYLE);
        restartButton.setOnMouseEntered(event -> restartButton.setStyle(RESTART_BUTTON_HOVER_STYLE));
        restartButton.setOnMouseExited(event -> restartButton.setStyle(RESTART_BUTTON_STYLE));

        primaryStage.setTitle("Coloris");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                update();
            }
        };

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
        });

        startNewGame();

        primaryStage.show();
    }

    /**
     * Monta um jogo novo do zero. Usado na primeira partida e a cada reinício.
     */
    private void startNewGame() {
        animationTimer.stop();
        root.getChildren().clear();

        well = new Well(SQUARE_MATRIX_WIDTH, SQUARE_MATRIX_HEIGHT);
        nextBlock = new Block(BLOCK_SIZE, BLOCK_SIZE);

        root.getChildren().addAll(universe, well);

        well.setTranslateX(-well.getWidth() / 2 + WIDTH / 3.);
        well.setTranslateY(-well.getBorderSize());

        fallingBlock = well.getFallingBlock();

        nextBlock.setTranslateX(-BLOCK_SIZE / 2 + 0.50 * well.getTranslateX());
        nextBlock.setTranslateY(0.15 * HEIGHT);

        GameLogo gameLogo = new GameLogo();
        gameLogo.setTranslateX(-gameLogo.getWidth() / 2 + 0.50 * well.getTranslateX());
        gameLogo.setTranslateY(nextBlock.getTranslateY() + nextBlock.getHeight() + GameLogo.FONT_SIZE + 10);

        root.getChildren().addAll(nextBlock, gameLogo);

        score = new LabeledValue(SQUARE_MATRIX_WIDTH, HEIGHT / 6, "SCORE", 0);
        score.setTranslateX(-score.getWidth() / 2 + (WIDTH - (WIDTH - (WIDTH / 3. + well.getWidth() / 2)) / 2));
        score.setTranslateY(-score.getHeight() / 2 + HEIGHT / 2.);

        restartButton.setPrefWidth(score.getWidth());
        restartButton.setTranslateX(score.getTranslateX());
        restartButton.setTranslateY(score.getTranslateY() + score.getHeight() + 15);

        root.getChildren().addAll(score, restartButton);

        gameLogic = new GameLogic(well.getSquareMatrix(), fallingBlock, nextBlock, score);

        animationTimer.start();

        mediaPlayer.seek(Duration.ZERO);
        mediaPlayer.play();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.R) {
            startNewGame();
            return;
        }

        if (fallingBlock == null || gameLogic.isGameOver()) {
            return;
        }

        switch (event.getCode()) {
            case UP -> gameLogic.reorderFallingBlockSquares();
            case DOWN -> gameLogic.speedUpFallingBlock();
            case LEFT -> gameLogic.moveFallingBlockHorizontally(GameLogic.MoveDirection.LEFT);
            case RIGHT -> gameLogic.moveFallingBlockHorizontally(GameLogic.MoveDirection.RIGHT);
            default -> {
            }
        }
    }

    private void update() {
        if (!gameLogic.squareDestruction()) {
            gameLogic.moveFallingBlockVertically();
            gameLogic.checkCollision();

            gameLogic.destroyAdjacentSquares();

            if (gameLogic.isGameOver()) {
                well.update();

                animationTimer.stop();
                mediaPlayer.stop();

                GameOverLabel gameOverLabel = new GameOverLabel();
                root.getChildren().add(gameOverLabel);

                gameOverLabel.setTranslateX(-gameOverLabel.getBoundsInParent().getWidth() / 2 + WIDTH / 3.);
                gameOverLabel.setTranslateY(-gameOverLabel.getBoundsInParent().getHeight() / 2 + 0.20 * well.getSquareMatrix().getHeight());

                return;
            }

            fallingBlock.update();
            nextBlock.update();
        }
        well.update();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
