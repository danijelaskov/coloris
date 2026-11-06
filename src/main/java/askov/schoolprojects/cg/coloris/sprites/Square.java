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

package askov.schoolprojects.cg.coloris.sprites;

import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.HashSet;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author Danijel Askov
 */
@SuppressWarnings("unused")
public class Square extends Sprite {

    public enum SquareColor {

        WHITE    (Color.web("0xDDDDDD")),
        YELLOW   (Color.web("0xCCCC00")),
        BLUE     (Color.web("0x00AADD")),
        RED      (Color.web("0xCC0000")),
        CYCLAMEN (Color.web("0xCC00CC")),
        ORANGE   (Color.web("0xFF7700")),
        DARK_GREEN (Color.web("0x008844")),
        DARK_BLUE  (Color.web("0x003399")),
        PURPLE   (Color.web("0x7722CC")),
        LIME     (Color.web("0x66AA00")),
        NULL     (Color.TRANSPARENT);

        private static final Random RANDOM = new Random();

        private final Color color;

        SquareColor(Color color) {
            this.color = color;
        }

        Color getColor() {
            return color;
        }

        public String getDisplayName() {
            return switch (this) {
                case DARK_GREEN -> "DARK GREEN";
                case DARK_BLUE -> "DARK BLUE";
                case CYCLAMEN -> "PINK";
                case BLUE -> "LIGHT BLUE";
                case NULL -> "";
                default -> name();
            };
        }

        public static SquareColor randomSquareColor(boolean includeTransparent) {
            List<SquareColor> colors = activeSquareColors;
            int colorCount = colors.size() + (includeTransparent ? 1 : 0);
            int selectedIndex = RANDOM.nextInt(colorCount);
            return selectedIndex == colors.size() ? NULL : colors.get(selectedIndex);
        }

    }

    private static final List<SquareColor> AVAILABLE_SQUARE_COLORS = Arrays.stream(SquareColor.values())
            .filter(color -> color != SquareColor.NULL)
            .toList();
    private static final List<SquareColor> DEFAULT_SQUARE_COLORS = List.of(
            SquareColor.WHITE,
            SquareColor.YELLOW,
            SquareColor.BLUE,
            SquareColor.RED,
            SquareColor.CYCLAMEN
    );
    private static List<SquareColor> activeSquareColors = DEFAULT_SQUARE_COLORS;

    public static final SquareColor DEFAULT_SQUARE_COLOR = SquareColor.NULL;
    private static final double SQUARE_DESTRUCTION_DURATION = 0.30; // Seconds

    private final double width;
    private final double height;
    private SquareColor blockColor;

    private final Rectangle rectangle;
    private final Polygon triangle1;
    private final Polygon triangle2;

    private Timeline animation;

    public static List<SquareColor> getAvailableSquareColors() {
        return AVAILABLE_SQUARE_COLORS;
    }

    public static List<SquareColor> getActiveSquareColors() {
        return List.copyOf(activeSquareColors);
    }

    public static void setActiveSquareColors(SquareColor[] colors) {
        if (colors == null || colors.length != DEFAULT_SQUARE_COLORS.size()) {
            throw new IllegalArgumentException("The active palette must contain exactly five colors.");
        }

        List<SquareColor> palette = Arrays.asList(colors.clone());
        if (palette.contains(null) || palette.contains(SquareColor.NULL)
                || new HashSet<>(palette).size() != DEFAULT_SQUARE_COLORS.size()
                || !AVAILABLE_SQUARE_COLORS.containsAll(palette)) {
            throw new IllegalArgumentException("The active palette must contain five different block colors.");
        }
        activeSquareColors = List.copyOf(palette);
    }
    
    public Square(double width, double height, SquareColor blockColor) {
        this.width = width;
        this.height = height;
        this.blockColor = blockColor;

        rectangle = new Rectangle(0.15 * width, 0.15 * height, 0.70 * width, 0.70 * height);
        rectangle.setStroke(null);

        triangle1 = new Polygon(
                width, 0.0,
                width, height,
                0.0, height
        );
        triangle1.setStroke(null);

        triangle2 = new Polygon(
                0.0, 0.0,
                width, 0.0,
                0.0, height
        );
        triangle2.setStroke(null);

        super.getChildren().addAll(triangle1, triangle2, rectangle);
    }

    public Square(double width, double height) {
        this(width, height, DEFAULT_SQUARE_COLOR);
    }

    @Override
    public void update() {
        rectangle.setFill(blockColor.getColor());
        triangle1.setFill(blockColor.getColor().darker());
        triangle2.setFill(blockColor.getColor().brighter());
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    public SquareColor getSquareColor() {
        return blockColor;
    }
    
    public void setSquareColor(SquareColor squareColor) {
        this.blockColor = squareColor;
    }

    public void absorbColorFrom(Square square) {
        blockColor = square.getSquareColor();
    }
    
    public Animation getDestructionAnimation() {
        if (animation == null)  {
            animation = new Timeline();
            
            KeyValue xKeyValue1 = new KeyValue(rectangle.xProperty(), 0);
            KeyValue yKeyValue1 = new KeyValue(rectangle.yProperty(), 0);
            KeyValue widthKeyValue1 = new KeyValue(rectangle.widthProperty(), width);
            KeyValue heightKeyValue1 = new KeyValue(rectangle.heightProperty(), height);
            
            KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, xKeyValue1, yKeyValue1, widthKeyValue1, heightKeyValue1);
            
            KeyValue xKeyValue2 = new KeyValue(rectangle.xProperty(), 0.15 * width, Interpolator.DISCRETE);
            KeyValue yKeyValue2 = new KeyValue(rectangle.yProperty(), 0.15 * height, Interpolator.DISCRETE);
            KeyValue widthKeyValue2 = new KeyValue(rectangle.widthProperty(), 0.70 * width, Interpolator.DISCRETE);
            KeyValue heightKeyValue2 = new KeyValue(rectangle.heightProperty(), 0.70 * height, Interpolator.DISCRETE);
            
            KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(SQUARE_DESTRUCTION_DURATION), xKeyValue2, yKeyValue2, widthKeyValue2, heightKeyValue2);
            
            animation.getKeyFrames().addAll(keyFrame1, keyFrame2);
            animation.setCycleCount(1);
            animation.setAutoReverse(false);
            animation.setOnFinished(event -> blockColor = DEFAULT_SQUARE_COLOR);
        }
        return animation;
    }

}
