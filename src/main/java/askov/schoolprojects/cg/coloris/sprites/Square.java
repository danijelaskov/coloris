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
        NULL     (Color.TRANSPARENT);

        private static final List<SquareColor> SQUARE_COLORS = List.of(values());
        private static final int NUM_SQUARE_COLORS = SQUARE_COLORS.size();
        private static final Random RANDOM = new Random();

        private final Color color;

        SquareColor(Color color) {
            this.color = color;
        }

        Color getColor() {
            return color;
        }

        public static SquareColor randomSquareColor() {
            return SQUARE_COLORS.get(RANDOM.nextInt(NUM_SQUARE_COLORS));
        }

        public static SquareColor randomVisibleSquareColor() {
            return SQUARE_COLORS.get(RANDOM.nextInt(NUM_SQUARE_COLORS - 1));
        }

    }

    public static final SquareColor DEFAULT_SQUARE_COLOR = SquareColor.NULL;
    private static final double SQUARE_DESTRUCTION_DURATION = 0.30; // Seconds

    private final double width;
    private final double height;
    private SquareColor blockColor;

    private final Rectangle rectangle;
    private final Polygon triangle1;
    private final Polygon triangle2;

    private Timeline animation;
    
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
