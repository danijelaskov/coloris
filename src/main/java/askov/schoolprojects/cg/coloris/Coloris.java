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
import askov.schoolprojects.cg.coloris.sprites.GameOverLabel;
import askov.schoolprojects.cg.coloris.sprites.LabeledValue;
import askov.schoolprojects.cg.coloris.sprites.SquareMatrix;
import askov.schoolprojects.cg.coloris.sprites.Universe;
import askov.schoolprojects.cg.coloris.sprites.Well;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

        LabeledValue score = new LabeledValue(SQUARE_MATRIX_WIDTH, HEIGHT / 6, "SCORE", 0);
        score.setTranslateX(-score.getWidth() / 2 + (WIDTH - (WIDTH - (WIDTH / 3. + well.getWidth() / 2)) / 2));
        score.setTranslateY(-score.getHeight() / 2 + HEIGHT / 2.);

        // Nível atual (começa em 01 e sobe a cada vez que a pontuação-alvo é atingida)
        LabeledValue level = new LabeledValue(SQUARE_MATRIX_WIDTH, HEIGHT / 6, "LEVEL", 1, 2);
        level.setTranslateX(score.getTranslateX());
        level.setTranslateY(score.getTranslateY() - score.getHeight() - 15);

        root.getChildren().addAll(score, level);

        gameLogic = new GameLogic(well.getSquareMatrix(), fallingBlock, nextBlock, score, level);

        scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnKeyPressed(event -> {
            if (fallingBlock != null) {
                switch (event.getCode()) {
                    case UP -> gameLogic.reorderFallingBlockSquares();
                    case DOWN -> gameLogic.speedUpFallingBlock();
                    case LEFT -> gameLogic.moveFallingBlockHorizontally(GameLogic.MoveDirection.LEFT);
                    case RIGHT -> gameLogic.moveFallingBlockHorizontally(GameLogic.MoveDirection.RIGHT);
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

    private void update() {
        if (gameLogic.isGameWon()) {
            well.update();

            animationTimer.stop();
            scene.setOnKeyPressed(event -> {});
            mediaPlayer.stop();

            // Faixa escura translúcida atrás do texto, para dar contraste
            Rectangle banner = new Rectangle(WIDTH, 140);
            banner.setFill(Color.rgb(0, 0, 0, 0.75));
            banner.setTranslateY(HEIGHT / 2. - banner.getHeight() / 2);

            // Texto dourado, grande, com contorno e sombra
            Text winLabel = new Text("YOU WIN!");
            winLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 96));
            winLabel.setFill(Color.GOLD);
            winLabel.setStroke(Color.web("0x8B4500"));
            winLabel.setStrokeWidth(3);
            winLabel.setEffect(new DropShadow(15, Color.BLACK));

            root.getChildren().addAll(banner, winLabel);

            // Centraliza o texto no centro da janela (horizontal e vertical)
            Bounds bounds = winLabel.getBoundsInLocal();
            winLabel.setTranslateX(WIDTH / 2. - (bounds.getMinX() + bounds.getWidth() / 2));
            winLabel.setTranslateY(HEIGHT / 2. - (bounds.getMinY() + bounds.getHeight() / 2));

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