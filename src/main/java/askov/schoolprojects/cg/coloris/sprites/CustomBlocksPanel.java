/*
 * This file is part of Coloris.
 *
 * Coloris is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package askov.schoolprojects.cg.coloris.sprites;

import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;

/** Color palette editor with five unique block colors and a save option. */
public class CustomBlocksPanel extends GameMenu {

    private static final int NUM_COLOR_SLOTS = 5;
    private static final double SWATCH_SIZE = 32;
    private static final double SWATCH_GAP = 12;
    private static final Font INSTRUCTION_FONT = new GameFont(GameFont.FontType.MAIN_GAME_FONT, 14).getFont();
    private static final Font STATUS_FONT = new GameFont(GameFont.FontType.MAIN_GAME_FONT, 16).getFont();

    private final Square.SquareColor[] selectedColors;
    private final Group[] swatches = new Group[NUM_COLOR_SLOTS];
    private final Rectangle[] selectionBorders = new Rectangle[NUM_COLOR_SLOTS];
    private final Text selectedColorText = new Text();
    private final double panelWidth;
    private int selectedSlot;

    public CustomBlocksPanel(double width, double height, List<Square.SquareColor> initialColors, Runnable saveAction) {
        super(width, height, 3, new MenuOption("SAVE", saveAction));
        if (initialColors.size() != NUM_COLOR_SLOTS) {
            throw new IllegalArgumentException("The color editor requires exactly five colors.");
        }

        panelWidth = width;
        selectedColors = initialColors.toArray(new Square.SquareColor[0]);

        Text instruction = new Text("CLICK A SQUARE TO CYCLE COLOR");
        instruction.setFont(INSTRUCTION_FONT);
        instruction.setFill(LabeledValue.LABEL_FILL_GRADIENT);
        instruction.setTextOrigin(VPos.CENTER);
        centerText(instruction, getRowCenterY(0));
        getChildren().add(instruction);

        double totalSwatchWidth = NUM_COLOR_SLOTS * SWATCH_SIZE + (NUM_COLOR_SLOTS - 1) * SWATCH_GAP;
        double startX = borderSize + (width - totalSwatchWidth) / 2;
        for (int i = 0; i < NUM_COLOR_SLOTS; i++) {
            final int slot = i;
            Square square = new Square(SWATCH_SIZE, SWATCH_SIZE, selectedColors[i]);
            square.update();

            Rectangle selectionBorder = new Rectangle(SWATCH_SIZE, SWATCH_SIZE);
            selectionBorder.setFill(Color.TRANSPARENT);
            selectionBorder.setStroke(Color.web("0xEEEE00"));
            selectionBorder.setStrokeWidth(3);
            selectionBorder.setMouseTransparent(true);

            Group swatch = new Group(square, selectionBorder);
            swatch.setTranslateX(startX + i * (SWATCH_SIZE + SWATCH_GAP));
            swatch.setTranslateY(getRowCenterY(1) - SWATCH_SIZE / 2);
            swatch.setCursor(Cursor.HAND);
            swatch.setOnMouseClicked(event -> {
                cycleColor(slot);
                event.consume();
            });

            swatches[i] = swatch;
            selectionBorders[i] = selectionBorder;
            getChildren().add(swatch);
        }

        selectedColorText.setFont(STATUS_FONT);
        selectedColorText.setFill(Color.web("0xEEEE00"));
        selectedColorText.setTextOrigin(VPos.CENTER);
        getChildren().add(selectedColorText);
        updateSwatches();
    }

    public Square.SquareColor[] getSelectedColors() {
        return selectedColors.clone();
    }

    private void cycleColor(int slot) {
        selectedSlot = slot;
        List<Square.SquareColor> availableColors = Square.getAvailableSquareColors();
        int currentIndex = availableColors.indexOf(selectedColors[slot]);

        for (int offset = 1; offset < availableColors.size(); offset++) {
            Square.SquareColor candidate = availableColors.get(Math.floorMod(currentIndex + offset, availableColors.size()));
            if (!isUsedByAnotherSlot(candidate, slot)) {
                selectedColors[slot] = candidate;
                break;
            }
        }
        updateSwatches();
    }

    private boolean isUsedByAnotherSlot(Square.SquareColor color, int slot) {
        for (int i = 0; i < selectedColors.length; i++) {
            if (i != slot && selectedColors[i] == color) {
                return true;
            }
        }
        return false;
    }

    private void updateSwatches() {
        for (int i = 0; i < NUM_COLOR_SLOTS; i++) {
            Square square = (Square) swatches[i].getChildren().get(0);
            square.setSquareColor(selectedColors[i]);
            square.update();
            selectionBorders[i].setVisible(i == selectedSlot);
            Tooltip.install(swatches[i], new Tooltip(selectedColors[i].getDisplayName()));
        }

        selectedColorText.setText("BLOCK " + (selectedSlot + 1) + ": " + selectedColors[selectedSlot].getDisplayName());
        centerText(selectedColorText, getRowCenterY(2));
    }

    private void centerText(Text text, double y) {
        text.setTranslateX(borderSize + (panelWidth - text.getLayoutBounds().getWidth()) / 2 - text.getLayoutBounds().getMinX());
        text.setTranslateY(y);
    }
}
