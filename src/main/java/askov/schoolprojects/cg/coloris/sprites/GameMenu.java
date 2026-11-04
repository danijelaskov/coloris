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

/**
 * Framed panel (same look as the score panel) holding a vertical list of selectable options.
 * The inner area is split into equal rows: the first {@code numContentRows} rows are left
 * for subclasses, the options occupy the remaining ones.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class GameMenu extends Frame {

    private static final Color BACKGROUND_COLOR = Color.web("0x111100");
    private static final int NUM_VERTICAL_SEGMENTS = 18;

    private final MenuOption[] options;
    private final double rowHeight;
    private int selectedIndex;

    protected GameMenu(double width, double height, int numContentRows, MenuOption... options) {
        super(width, height, NUM_VERTICAL_SEGMENTS, BACKGROUND_COLOR);
        this.options = options;

        rowHeight = height / (numContentRows + options.length);

        for (int i = 0; i < options.length; i++) {
            MenuOption option = options[i];
            option.setTranslateX(super.borderSize + width / 2);
            option.setTranslateY(getRowCenterY(numContentRows + i));

            // Mouse moved (not entered), so that switching screens under a still cursor doesn't change the selection
            int optionIndex = i;
            option.setOnMouseMoved(event -> {
                if (optionIndex != selectedIndex) {
                    select(optionIndex);
                }
            });

            super.getChildren().add(option);
        }

        select(0);
    }

    public GameMenu(double width, double height, MenuOption... options) {
        this(width, height, 0, options);
    }

    protected final double getRowCenterY(int row) {
        return super.borderSize + (row + 0.5) * rowHeight;
    }

    public void select(int index) {
        options[selectedIndex].setSelected(false);
        selectedIndex = Math.floorMod(index, options.length);
        options[selectedIndex].setSelected(true);
    }

    public void selectPrevious() {
        select(selectedIndex - 1);
    }

    public void selectNext() {
        select(selectedIndex + 1);
    }

    public void chooseSelected() {
        options[selectedIndex].choose();
    }

}
