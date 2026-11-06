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

import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

/**
 *
 * @author Danijel Askov
 */
@SuppressWarnings("unused")
public class Universe extends Sprite {

    @SuppressWarnings("WeakerAccess")
    private static class Star extends Group {

        private static final Color STAR_COLOR = Color.web("0x68C5F4");
        private static final double CRITICAL_STAR_SIZE = 2.5;
        private static final double RELATIVE_RAY_SIZE = 5.0;

        private static final Effect GLOW = new Glow(1);

        public Star(double size) {
            Circle body = new Circle(size);

            if (size > CRITICAL_STAR_SIZE) {
                Path rays = new Path();
                double raySize = RELATIVE_RAY_SIZE * size;

                rays.getElements().addAll(
                        new MoveTo(raySize, 0),
                        new CubicCurveTo(0.10 * raySize, 0, 0, 0.10 * raySize, 0, raySize),
                        new CubicCurveTo(0, 0.10 * raySize, -0.10 * raySize, 0, -raySize, 0),
                        new CubicCurveTo(-0.10 * raySize, 0, 0, -0.10 * raySize, 0, -raySize),
                        new CubicCurveTo(0, -0.10 * raySize, 0.10 * raySize, 0, raySize, 0),
                        new ClosePath()
                );

                rays.setStroke(STAR_COLOR.darker());
                rays.setFill(STAR_COLOR.darker());
                rays.setStrokeLineJoin(StrokeLineJoin.MITER);
                rays.setStrokeMiterLimit(Double.MAX_VALUE);

                super.getChildren().add(rays);

                ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(RANDOM.nextInt(6)), this);

                scaleTransition.setFromX(1);
                scaleTransition.setFromY(1);

                scaleTransition.setToX(0.1);
                scaleTransition.setToY(0.1);

                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(Animation.INDEFINITE);
                scaleTransition.setDelay(Duration.seconds(RANDOM.nextInt(6)));

                scaleTransition.play();
            }

            body.setFill(STAR_COLOR);
            super.getChildren().add(body);

            setEffect(GLOW);
        }

    }

    private static final Color UNIVERSE_COLOR = Color.BLACK;
    private static final int NUM_STARS = 50;
    private static final Random RANDOM = new Random();
    private final double width;
    private final double height;

    public Universe(double width, double height) {
        Rectangle background = new Rectangle(this.width = width, this.height = height);
        background.setFill(UNIVERSE_COLOR);
        super.getChildren().add(background);

        for (int i = 0; i < NUM_STARS; i++) {
            double posX = RANDOM.nextDouble() * width;
            double posY = RANDOM.nextDouble() * height;

            Star star = new Star(RANDOM.nextDouble() * 1.2 * Star.CRITICAL_STAR_SIZE);

            star.setTranslateX(posX);
            star.setTranslateY(posY);

            super.getChildren().add(star);
        }
    }

    @Override
    public void update() {
    }
    
    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

}
