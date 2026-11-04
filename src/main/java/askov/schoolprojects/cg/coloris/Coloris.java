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
import askov.schoolprojects.cg.coloris.sprites.GameFont;
import askov.schoolprojects.cg.coloris.sprites.GameLogo;
import askov.schoolprojects.cg.coloris.sprites.Block;
import askov.schoolprojects.cg.coloris.sprites.GameOverLabel;
import askov.schoolprojects.cg.coloris.sprites.LabeledValue;
import askov.schoolprojects.cg.coloris.sprites.SquareMatrix;
import askov.schoolprojects.cg.coloris.sprites.Universe;
import askov.schoolprojects.cg.coloris.sprites.Well;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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

    private final Universe universe = new Universe(WIDTH, HEIGHT);
    private final Well well = new Well(SQUARE_MATRIX_WIDTH, SQUARE_MATRIX_HEIGHT);

    private Block fallingBlock;
    private final Block nextBlock = new Block(BLOCK_SIZE, BLOCK_SIZE);

    private Group root;

    private GameLogic gameLogic;
    private AnimationTimer animationTimer;
    private boolean paused = false;
    private Text pauseText;
    
    private Scene scene;
    
    private final Media media = new Media(Thread.currentThread().getContextClassLoader().getResource("music/music.mp3").toExternalForm());
    private final MediaPlayer mediaPlayer = new MediaPlayer(media);

    @Override
    public void start(Stage primaryStage) {
        root = new Group();

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

        pauseText = new Text("PAUSED");
        pauseText.setFont(new GameFont(GameFont.FontType.MAIN_GAME_FONT, 34).getFont());
        pauseText.setFill(Color.web("0xEEEE00"));
        pauseText.setVisible(false);
        pauseText.setTranslateX(-pauseText.getBoundsInParent().getWidth() / 2 + WIDTH / 3.);
        pauseText.setTranslateY(-pauseText.getBoundsInParent().getHeight() / 2 + 0.20 * well.getSquareMatrix().getHeight());
        root.getChildren().add(pauseText);
        
        LabeledValue score = new LabeledValue(SQUARE_MATRIX_WIDTH, HEIGHT / 6, "SCORE", 0);
        score.setTranslateX(-score.getWidth() / 2 + (WIDTH - (WIDTH - (WIDTH / 3. + well.getWidth() / 2)) / 2));
        score.setTranslateY(-score.getHeight() / 2 + HEIGHT / 2.);
        
        root.getChildren().addAll(score);

        gameLogic = new GameLogic(well.getSquareMatrix(), fallingBlock, nextBlock, score);
        
        scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnKeyPressed(event -> {
            if (fallingBlock != null) {
                switch (event.getCode()) {
                    case UP -> {
                        if (!paused && !gameLogic.isGameOver()) {
                            gameLogic.reorderFallingBlockSquares();
                        }
                    }
                    case DOWN -> {
                        if (!paused && !gameLogic.isGameOver()) {
                            gameLogic.speedUpFallingBlock();
                        }
                    }
                    case LEFT -> {
                        if (!paused && !gameLogic.isGameOver()) {
                            gameLogic.moveFallingBlockHorizontally(GameLogic.MoveDirection.LEFT);
                        }
                    }
                    case RIGHT -> {
                        if (!paused && !gameLogic.isGameOver()) {
                            gameLogic.moveFallingBlockHorizontally(GameLogic.MoveDirection.RIGHT);
                        }
                    }
                    case P -> togglePause();
                    default -> {
                    }
                }
            }
        });

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
        animationTimer.start();
        
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
        });
        
        primaryStage.show();
    }

    private void togglePause() {
        if (gameLogic == null || gameLogic.isGameOver()) {
            return;
        }

        if (paused) {
            paused = false;
            pauseText.setVisible(false);
            animationTimer.start();
            mediaPlayer.play();
        } else {
            paused = true;
            pauseText.setVisible(true);
            animationTimer.stop();
            mediaPlayer.pause();
        }
    }

    private void update() {
        if (paused) {
            return;
        }

        if (!gameLogic.squareDestruction()) {
            gameLogic.moveFallingBlockVertically();
            gameLogic.checkCollision();

            gameLogic.destroyAdjacentSquares();

            if (gameLogic.isGameOver()) {
                well.update();
                
                animationTimer.stop();
                scene.setOnKeyPressed(event -> {});
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
