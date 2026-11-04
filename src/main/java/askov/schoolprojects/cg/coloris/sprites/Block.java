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

/**
 *
 * @author Danijel Askov
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Block extends Sprite {

    public enum ReorderDirection { UP, DOWN }

    public static final int DEFAULT_NUM_SQUARES = 3;
    public static final ReorderDirection DEFAULT_REORDER_DIRECTION = ReorderDirection.UP;

    private final Square[] squares;
    private final int numSquares;
    private final double width;
    private final double height;

    public Block(double width, double squareHeight, int numSquares) {
        squares = new Square[this.numSquares = numSquares];

        this.width = width;
        this.height = numSquares * squareHeight;

        for (int i = 0; i < numSquares; i++) {
            squares[i] = new Square(width, squareHeight, Square.SquareColor.randomVisibleSquareColor());

            super.getChildren().add(squares[i]);
        }
    }

    public Block(double blockWidth, double blockHeight) {
        this(blockWidth, blockHeight, DEFAULT_NUM_SQUARES);
    }

    public Square getSquare(int index) {
        if (index > -1 && index < numSquares) {
            return squares[index];
        } else {
            return null;
        }
    }

    @Override
    public void update() {
        for (int i = 0; i < numSquares; i++) {
            squares[i].setTranslateY(i * height / numSquares);
            squares[i].update();
        }
    }
    
    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    public int getNumSquares() {
        return numSquares;
    }

    public Square getBottomSquare() {
        return squares[numSquares - 1];
    }

    public void reorderSquares(ReorderDirection reorderDirection) {
        Square temp;
        switch (reorderDirection) {
            case UP -> {
                temp = squares[0];
                if (numSquares - 1 >= 0) System.arraycopy(squares, 1, squares, 0, numSquares - 1);
                squares[numSquares - 1] = temp;
            }
            case DOWN -> {
                temp = squares[numSquares - 1];
                System.arraycopy(squares, 0, squares, 1, numSquares - 1);
                squares[0] = temp;
            }
        }
    }
    
    public void absorbColorsFrom(Block block) {
        for (int i = 0; i < block.numSquares; i++) {
            squares[i].absorbColorFrom(block.squares[i]);
        }
    }
    
    public void setRandomSquareColors() {
        for (int i = 0; i < numSquares; i++) {
            squares[i].setSquareColor(Square.SquareColor.randomVisibleSquareColor());
        }
    }

    public void reorderSquares() {
        reorderSquares(DEFAULT_REORDER_DIRECTION);
    }

}
