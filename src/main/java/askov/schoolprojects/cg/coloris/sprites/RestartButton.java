package askov.schoolprojects.cg.coloris.sprites;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RestartButton extends Group {

    private final double width;
    private final double height;

    public RestartButton(double width, double height, Runnable onClick) {
        this.width = width;
        this.height = height;

        Rectangle background = new Rectangle(width, height);
        background.setArcWidth(6);
        background.setArcHeight(6);
        background.setFill(Color.web("0x111100"));
        background.setStroke(Color.web("0xEEEE00"));
        background.setStrokeWidth(2);

        Text label = new Text("RESTART");
        label.setFont(new GameFont(GameFont.FontType.MAIN_GAME_FONT, 18).getFont());
        label.setFill(Color.web("0xEEEE00"));
        label.setTranslateX((width - label.getBoundsInLocal().getWidth()) / 2 - label.getBoundsInLocal().getMinX());
        label.setTranslateY((height - label.getBoundsInLocal().getHeight()) / 2 - label.getBoundsInLocal().getMinY());

        getChildren().addAll(background, label);
        setCursor(Cursor.HAND);
        setOnMouseClicked(event -> onClick.run());
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}