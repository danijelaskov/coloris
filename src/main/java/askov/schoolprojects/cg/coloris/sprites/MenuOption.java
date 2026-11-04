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

import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Menu option whose selected state is marked by two small blocks, one on each side of the label.
 * The option is centered on its own origin.
 */
@SuppressWarnings("unused")
public class MenuOption extends Sprite {

    private static final Font LABEL_FONT = new GameFont(GameFont.FontType.MAIN_GAME_FONT, 34).getFont();
    private static final Color SELECTED_LABEL_FILL = Color.web("0xEEEE00");
    private static final double CURSOR_SQUARE_SIZE = 8;
    private static final double CURSOR_GAP = 14;

    private final Text labelText = new Text();
    private final Block leftCursor = new Block(CURSOR_SQUARE_SIZE, CURSOR_SQUARE_SIZE);
    private final Block rightCursor = new Block(CURSOR_SQUARE_SIZE, CURSOR_SQUARE_SIZE);
    private final double width;
    private final double height;
    private final Runnable action;

    public MenuOption(String label, Runnable action) {
        this.action = action;

        labelText.setText(label);
        labelText.setFont(LABEL_FONT);
        labelText.setTextOrigin(VPos.CENTER);

        double labelWidth = labelText.getLayoutBounds().getWidth();
        labelText.setTranslateX(-labelWidth / 2);

        leftCursor.setTranslateX(-labelWidth / 2 - CURSOR_GAP - leftCursor.getWidth());
        leftCursor.setTranslateY(-leftCursor.getHeight() / 2);
        rightCursor.setTranslateX(labelWidth / 2 + CURSOR_GAP);
        rightCursor.setTranslateY(-rightCursor.getHeight() / 2);

        width = labelWidth + 2 * (CURSOR_GAP + leftCursor.getWidth());
        height = LABEL_FONT.getSize();

        // Transparent area that makes the whole option (not only the glyphs) react to the mouse
        Rectangle hitArea = new Rectangle(-width / 2, -height / 2, width, height);
        hitArea.setFill(Color.TRANSPARENT);

        super.getChildren().addAll(hitArea, leftCursor, labelText, rightCursor);

        setCursor(Cursor.HAND);
        setOnMouseClicked(event -> choose());
        setSelected(false);
    }

    public void setSelected(boolean selected) {
        labelText.setFill(selected ? SELECTED_LABEL_FILL : LabeledValue.LABEL_FILL_GRADIENT);
        leftCursor.setVisible(selected);
        rightCursor.setVisible(selected);

        if (selected) {
            leftCursor.setRandomSquareColors();
            rightCursor.absorbColorsFrom(leftCursor);
            update();
        }
    }

    public void choose() {
        action.run();
    }

    @Override
    public void update() {
        leftCursor.update();
        rightCursor.update();
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
