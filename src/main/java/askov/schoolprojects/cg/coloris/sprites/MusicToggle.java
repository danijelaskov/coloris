/*
 * This file is part of Coloris.
 *
 * Coloris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package askov.schoolprojects.cg.coloris.sprites;

import java.util.function.Consumer;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/** A compact button that toggles the game's music on and off. */
public class MusicToggle extends Sprite {

    private static final double BUTTON_WIDTH = 142;
    private static final double BUTTON_HEIGHT = 38;
    private static final Color BORDER_COLOR = Color.web("0xEEEE00");
    private static final Color NORMAL_BACKGROUND = Color.rgb(17, 17, 0, 0.9);
    private static final Color HOVER_BACKGROUND = Color.rgb(48, 48, 0, 0.95);

    private final Rectangle background = new Rectangle(BUTTON_WIDTH, BUTTON_HEIGHT);
    private final Text label = new Text();
    private final Consumer<Boolean> onMuteChanged;
    private boolean muted;

    public MusicToggle(boolean muted, Consumer<Boolean> onMuteChanged) {
        this.muted = muted;
        this.onMuteChanged = onMuteChanged;

        background.setArcWidth(8);
        background.setArcHeight(8);
        background.setFill(NORMAL_BACKGROUND);
        background.setStroke(BORDER_COLOR);
        background.setStrokeWidth(1.5);

        label.setFont(new GameFont(GameFont.FontType.MAIN_GAME_FONT, 17).getFont());
        label.setTextOrigin(VPos.CENTER);
        label.setFill(BORDER_COLOR);
        updateLabel();

        getChildren().addAll(background, label);
        setAccessibleRole(AccessibleRole.BUTTON);
        setCursor(Cursor.HAND);
        setFocusTraversable(true);
        setOnMouseClicked(event -> toggle());
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                toggle();
                event.consume();
            }
        });
        setOnMouseEntered(event -> background.setFill(HOVER_BACKGROUND));
        setOnMouseExited(event -> background.setFill(NORMAL_BACKGROUND));
    }

    private void toggle() {
        muted = !muted;
        updateLabel();
        onMuteChanged.accept(muted);
    }

    private void updateLabel() {
        label.setText(muted ? "MUSIC: OFF" : "MUSIC: ON");
        setAccessibleText(muted ? "Music muted. Activate to turn music on."
                : "Music on. Activate to mute music.");

        Bounds bounds = label.getLayoutBounds();
        label.setTranslateX((BUTTON_WIDTH - bounds.getWidth()) / 2 - bounds.getMinX());
        label.setTranslateY((BUTTON_HEIGHT - bounds.getHeight()) / 2 - bounds.getMinY());
    }

    @Override
    public void update() {
        // The toggle has no animated elements.
    }

    @Override
    public double getWidth() {
        return BUTTON_WIDTH;
    }

    @Override
    public double getHeight() {
        return BUTTON_HEIGHT;
    }
}
