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
import askov.schoolprojects.cg.coloris.sprites.ControlsPanel;
import askov.schoolprojects.cg.coloris.sprites.CustomBlocksPanel;
import askov.schoolprojects.cg.coloris.sprites.GameFont;
import askov.schoolprojects.cg.coloris.sprites.GameLogo;
import askov.schoolprojects.cg.coloris.sprites.Block;
import askov.schoolprojects.cg.coloris.sprites.GameMenu;
import askov.schoolprojects.cg.coloris.sprites.GameOverLabel;
import askov.schoolprojects.cg.coloris.sprites.LabeledValue;
import askov.schoolprojects.cg.coloris.sprites.MenuOption;
import askov.schoolprojects.cg.coloris.sprites.MusicToggle;
import askov.schoolprojects.cg.coloris.sprites.Square;
import askov.schoolprojects.cg.coloris.sprites.SquareMatrix;
import askov.schoolprojects.cg.coloris.sprites.Universe;
import askov.schoolprojects.cg.coloris.sprites.Well;
import askov.schoolprojects.cg.coloris.sprites.RestartButton;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
    private static final double MENU_LOGO_FONT_SIZE = 1.4 * GameLogo.FONT_SIZE;
    private static final double MENU_WIDTH = 1.5 * SQUARE_MATRIX_WIDTH;
    private static final double MENU_HEIGHT = HEIGHT / 3.25;
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Coloris.class);
    private static final String CUSTOM_COLORS_KEY = "customBlockColors";

    private final Universe universe = new Universe(WIDTH, HEIGHT);
    private Well well;

    private Block fallingBlock;
    private Block nextBlock;

    private Group root;
    private Group viewportRoot;
    private Group scaledContent;
    private Rectangle fullScreenBackground;
    private Stage primaryStage;

    private GameLogic gameLogic;
    private AnimationTimer animationTimer;

    private boolean paused = false;
    private Text pauseText;

    private Scene scene;

    private GameMenu mainMenu;
    private ControlsPanel controlsPanel;
    private CustomBlocksPanel customBlocksPanel;
    private boolean musicMuted;

    private final Media media = new Media(Thread.currentThread().getContextClassLoader().getResource("music/music.mp3").toExternalForm());
    private final MediaPlayer mediaPlayer = new MediaPlayer(media);

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadCustomBlockColors();
        mainMenu = new GameMenu(MENU_WIDTH, MENU_HEIGHT,
                new MenuOption("PLAY", this::startGame),
                new MenuOption("CONTROLS", this::showControls),
                new MenuOption("CUSTOM BLOCKS", this::showCustomBlocks),
                new MenuOption("EXIT", Platform::exit));
        controlsPanel = new ControlsPanel(MENU_WIDTH, MENU_HEIGHT, this::showMainMenu);

        viewportRoot = new Group();
        fullScreenBackground = new Rectangle();
        fullScreenBackground.setFill(Color.BLACK);
        scaledContent = new Group();
        viewportRoot.getChildren().addAll(fullScreenBackground, universe, scaledContent);

        scene = new Scene(viewportRoot, WIDTH, HEIGHT);

        primaryStage.setTitle("Coloris");
        primaryStage.setScene(scene);
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        Runnable syncSceneToStage = () -> {
            double targetWidth = primaryStage.isFullScreen() ? primaryStage.getWidth() : WIDTH;
            double targetHeight = primaryStage.isFullScreen() ? primaryStage.getHeight() : HEIGHT;

            if (scene == null || scene.getWidth() != targetWidth || scene.getHeight() != targetHeight) {
                scene = new Scene(viewportRoot, targetWidth, targetHeight);
                primaryStage.setScene(scene);
            }
            updateViewportScale();
        };

        primaryStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> syncSceneToStage.run());
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (primaryStage.isFullScreen()) {
                syncSceneToStage.run();
            }
        });
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (primaryStage.isFullScreen()) {
                syncSceneToStage.run();
            }
        });
        scene.widthProperty().addListener((observable, oldValue, newValue) -> updateViewportScale());
        scene.heightProperty().addListener((observable, oldValue, newValue) -> updateViewportScale());

        syncSceneToStage.run();

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

        showMainMenu();

        primaryStage.show();
    }

    private void updateViewportScale() {
        double sceneWidth = scene.getWidth();
        double sceneHeight = scene.getHeight();

        fullScreenBackground.setWidth(sceneWidth);
        fullScreenBackground.setHeight(sceneHeight);
        universe.resizeTo(sceneWidth, sceneHeight);

        double scale = Math.min(sceneWidth / WIDTH, sceneHeight / HEIGHT);
        scaledContent.setScaleX(scale);
        scaledContent.setScaleY(scale);
        scaledContent.setTranslateX((sceneWidth - WIDTH * scale) / 2.0);
        scaledContent.setTranslateY((sceneHeight - HEIGHT * scale) / 2.0);
    }

    private void setSceneContent(Group content) {
        root = content;
        scaledContent.getChildren().setAll(root);
        updateViewportScale();
    }

    private void showMainMenu() {
        showMenuScreen(mainMenu);
        mediaPlayer.play();
    }

    private void showControls() {
        showMenuScreen(controlsPanel);
    }

    private void showCustomBlocks() {
        customBlocksPanel = new CustomBlocksPanel(
                MENU_WIDTH,
                MENU_HEIGHT,
                Square.getActiveSquareColors(),
                this::saveCustomBlockColors);
        showMenuScreen(customBlocksPanel);
    }

    private void saveCustomBlockColors() {
        Square.SquareColor[] colors = customBlocksPanel.getSelectedColors();
        Square.setActiveSquareColors(colors);
        PREFERENCES.put(CUSTOM_COLORS_KEY, String.join(",", Arrays.stream(colors)
                .map(Enum::name)
                .toList()));
        try {
            PREFERENCES.flush();
        } catch (BackingStoreException exception) {
            System.err.println("Could not persist the custom block colors: " + exception.getMessage());
        }
        showMainMenu();
    }

    private static void loadCustomBlockColors() {
        String savedColors = PREFERENCES.get(CUSTOM_COLORS_KEY, "");
        if (savedColors.isBlank()) {
            return;
        }

        String[] names = savedColors.split(",");
        if (names.length != 5) {
            return;
        }

        try {
            Square.SquareColor[] colors = Arrays.stream(names)
                    .map(Square.SquareColor::valueOf)
                    .toArray(Square.SquareColor[]::new);
            Square.setActiveSquareColors(colors);
        } catch (IllegalArgumentException exception) {
            // Ignore outdated or invalid preferences and keep the default palette.
        }
    }

    private void showMenuScreen(GameMenu menu) {
        GameLogo gameLogo = new GameLogo(GameLogo.Orientation.HORIZONTAL, MENU_LOGO_FONT_SIZE);
        Group colorStrip = createColorStrip((int) Math.round(gameLogo.getWidth() / BLOCK_SIZE));

        gameLogo.setTranslateX(-gameLogo.getBoundsInLocal().getMinX() + (WIDTH - gameLogo.getWidth()) / 2);
        gameLogo.setTranslateY(-gameLogo.getBoundsInLocal().getMinY() + 0.06 * HEIGHT);

        colorStrip.setTranslateX((WIDTH - colorStrip.getBoundsInLocal().getWidth()) / 2);
        colorStrip.setTranslateY(gameLogo.getBoundsInParent().getMaxY() + BLOCK_SIZE / 2);

        menu.setTranslateX((WIDTH - menu.getWidth()) / 2);
        menu.setTranslateY(colorStrip.getBoundsInParent().getMaxY() + (HEIGHT - colorStrip.getBoundsInParent().getMaxY() - menu.getHeight()) / 2);

        root = new Group();
        root.getChildren().addAll(gameLogo, colorStrip, menu, createMusicToggle());
        setSceneContent(root);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                return;
            }

            switch (event.getCode()) {
                case UP -> menu.selectPrevious();
                case DOWN -> menu.selectNext();
                case ENTER, SPACE -> menu.chooseSelected();
                case ESCAPE -> {
                    if (menu != mainMenu) {
                        showMainMenu();
                    }
                }
                default -> {
                }
            }
        });
    }

    private static Group createColorStrip(int numSquares) {
        Group colorStrip = new Group();
        for (int i = 0; i < numSquares; i++) {
            Square square = new Square(BLOCK_SIZE, BLOCK_SIZE, Square.SquareColor.randomSquareColor(false));
            square.setTranslateX(i * BLOCK_SIZE);
            square.update();
            colorStrip.getChildren().add(square);
        }
        return colorStrip;
    }

    private MusicToggle createMusicToggle() {
        MusicToggle toggle = new MusicToggle(musicMuted, muted -> {
            musicMuted = muted;
            mediaPlayer.setMute(muted);
        });
        toggle.setTranslateX(WIDTH - toggle.getWidth() - 14);
        toggle.setTranslateY(HEIGHT - toggle.getHeight() - 12);
        return toggle;
    }

    private RestartButton createRestartButton(MusicToggle musicToggle) {
        RestartButton button = new RestartButton(musicToggle.getWidth() * 0.8, musicToggle.getHeight(), this::restartGame);
        button.setTranslateX(musicToggle.getTranslateX() - button.getWidth() - 10);
        button.setTranslateY(musicToggle.getTranslateY());
        return button;
    }

    private void restartGame() {
        paused = false;
        animationTimer.stop();
        mediaPlayer.stop();
        startGame();
    }

    private void startGame() {
        well = new Well(SQUARE_MATRIX_WIDTH, SQUARE_MATRIX_HEIGHT);
        nextBlock = new Block(BLOCK_SIZE, BLOCK_SIZE);

        root = new Group();

        root.getChildren().addAll(well);

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

        // Nível atual (começa em 01 e sobe a cada vez que a pontuação-alvo é atingida)
        LabeledValue level = new LabeledValue(SQUARE_MATRIX_WIDTH, HEIGHT / 6, "LEVEL", 1, 2);
        level.setTranslateX(score.getTranslateX());
        level.setTranslateY(score.getTranslateY() - score.getHeight() - 15);

        root.getChildren().addAll(score, level);
        MusicToggle musicToggle = createMusicToggle();
        root.getChildren().addAll(musicToggle, createRestartButton(musicToggle));

        gameLogic = new GameLogic(well.getSquareMatrix(), fallingBlock, nextBlock, score, level);

        setSceneContent(root);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                return;
            }

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
                    case C -> {
                        if (!paused && !gameLogic.isGameOver()) {
                            gameLogic.swapFallingBlockWithNextBlock();
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

        animationTimer.start();

        mediaPlayer.play();
    }

    private void togglePause() {
        if (gameLogic == null || gameLogic.isGameOver() || gameLogic.isGameWon()) {
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

        if (gameLogic.isGameWon()) {
            well.update();

            animationTimer.stop();
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.F) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                }
            });
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
                scene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.F) {
                        primaryStage.setFullScreen(!primaryStage.isFullScreen());
                    }
                });
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
