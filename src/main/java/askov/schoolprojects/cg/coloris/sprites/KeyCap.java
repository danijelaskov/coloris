/*
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

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

/**
 * Arrow key drawn as a white square with an arrow on top of it.
 */
@SuppressWarnings("unused")
public class KeyCap extends Sprite {

    public enum Arrow {

        UP    (0),
        RIGHT (90),
        DOWN  (180),
        LEFT  (270);

        private final double angle;

        Arrow(double angle) {
            this.angle = angle;
        }

    }

    private static final Color ARROW_COLOR = Color.web("0x113366");

    private final double size;

    public KeyCap(double size, Arrow arrow) {
        this.size = size;

        Square cap = new Square(size, size, Square.SquareColor.WHITE);
        cap.update();

        // Upward pointing arrow, rotated around the center of the key
        Polygon arrowShape = new Polygon(
                0.50 * size, 0.22 * size,
                0.78 * size, 0.50 * size,
                0.60 * size, 0.50 * size,
                0.60 * size, 0.78 * size,
                0.40 * size, 0.78 * size,
                0.40 * size, 0.50 * size,
                0.22 * size, 0.50 * size
        );
        arrowShape.setStroke(null);
        arrowShape.setFill(ARROW_COLOR);
        arrowShape.getTransforms().add(new Rotate(arrow.angle, size / 2, size / 2));

        super.getChildren().addAll(cap, arrowShape);
    }

    public KeyCap(double size, String label) {
        this.size = size;

        Square cap = new Square(size, size, Square.SquareColor.WHITE);
        cap.update();

        Text labelText = new Text(label);
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.62));
        labelText.setFill(ARROW_COLOR);
        labelText.setTranslateX((size - labelText.getLayoutBounds().getWidth()) / 2 - labelText.getLayoutBounds().getMinX());
        labelText.setTranslateY((size - labelText.getLayoutBounds().getHeight()) / 2 - labelText.getLayoutBounds().getMinY());

        super.getChildren().addAll(cap, labelText);
    }

    @Override
    public void update() {
    }

    @Override
    public double getWidth() {
        return size;
    }

    @Override
    public double getHeight() {
        return size;
    }

}
