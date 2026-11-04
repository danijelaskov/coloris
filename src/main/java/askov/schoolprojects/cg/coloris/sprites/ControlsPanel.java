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
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Menu panel listing the game controls, with a single BACK option below them.
 */
@SuppressWarnings("unused")
public class ControlsPanel extends GameMenu {

    private static final Font DESCRIPTION_FONT = new GameFont(GameFont.FontType.MAIN_GAME_FONT, 24).getFont();
    private static final double KEY_CAP_SIZE = 28;
    private static final double KEY_CAP_GAP = 4;
    private static final double KEYS_COLUMN_WIDTH = 2 * KEY_CAP_SIZE + KEY_CAP_GAP;
    private static final double COLUMN_GAP = 16;

    public ControlsPanel(double width, double height, Runnable backAction) {
        super(width, height, 3, new MenuOption("BACK", backAction));

        Group[] rows = {
            createRow("MOVE", KeyCap.Arrow.LEFT, KeyCap.Arrow.RIGHT),
            createRow("SHIFT COLORS", KeyCap.Arrow.UP),
            createRow("SPEED UP", KeyCap.Arrow.DOWN)
        };

        double rowsWidth = 0;
        for (Group row : rows) {
            rowsWidth = Math.max(rowsWidth, row.getLayoutBounds().getWidth());
        }

        for (int i = 0; i < rows.length; i++) {
            rows[i].setTranslateX(super.borderSize + (width - rowsWidth) / 2);
            rows[i].setTranslateY(getRowCenterY(i));
            super.getChildren().add(rows[i]);
        }
    }

    private static Group createRow(String description, KeyCap.Arrow... arrows) {
        Group row = new Group();

        double keysWidth = arrows.length * KEY_CAP_SIZE + (arrows.length - 1) * KEY_CAP_GAP;
        for (int i = 0; i < arrows.length; i++) {
            KeyCap keyCap = new KeyCap(KEY_CAP_SIZE, arrows[i]);
            keyCap.setTranslateX((KEYS_COLUMN_WIDTH - keysWidth) / 2 + i * (KEY_CAP_SIZE + KEY_CAP_GAP));
            keyCap.setTranslateY(-KEY_CAP_SIZE / 2);
            row.getChildren().add(keyCap);
        }

        Text descriptionText = new Text(description);
        descriptionText.setFont(DESCRIPTION_FONT);
        descriptionText.setFill(LabeledValue.LABEL_FILL_GRADIENT);
        descriptionText.setTextOrigin(VPos.CENTER);
        descriptionText.setTranslateX(KEYS_COLUMN_WIDTH + COLUMN_GAP);
        row.getChildren().add(descriptionText);

        return row;
    }

}
