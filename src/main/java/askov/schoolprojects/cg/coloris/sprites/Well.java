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

import javafx.scene.paint.Color;

/**
 *
 * @author Danijel Askov
 */
public class Well extends Frame {
    
    private static final Color BACKGROUND_COLOR = Color.web("0x0000DD", 0.5);
    private static final int NUM_SEGMENTS = 55;

    private static final double RELATIVE_WELL_HEIGHT = 1.4;
    
    private final SquareMatrix squareMatrix;
    private final Block fallingBlock;
    
    public Well(double squareMatrixWidth, double squareMatrixHeight) {
        super(squareMatrixWidth, RELATIVE_WELL_HEIGHT * squareMatrixHeight, NUM_SEGMENTS, BACKGROUND_COLOR);
        
        double relativeMarkerHeight = (RELATIVE_WELL_HEIGHT - 1) * squareMatrixHeight;
        double markerPosition = (2.0 / 3.0) * Block.DEFAULT_NUM_SQUARES * (squareMatrixHeight / SquareMatrix.DEFAULT_NUM_ROWS);
        double relativeMarkerRatio = (relativeMarkerHeight + markerPosition) / (RELATIVE_WELL_HEIGHT * squareMatrixHeight);
        int markerIndex = (int)(relativeMarkerRatio * NUM_SEGMENTS);

        segmentsLeft[markerIndex].setFillType(FillType.YELLOW);
        segmentsRight[markerIndex].setFillType(FillType.YELLOW_INVERTED);
        
        squareMatrix = new SquareMatrix(squareMatrixWidth, squareMatrixHeight);
        fallingBlock = new Block(squareMatrixWidth / squareMatrix.getNumColumns(), squareMatrixWidth / squareMatrix.getNumColumns());
        
        squareMatrix.setTranslateX(super.getBorderSize());
        squareMatrix.setTranslateY(super.getBorderSize() + (RELATIVE_WELL_HEIGHT - 1) * squareMatrixHeight);
        
        super.getChildren().addAll(squareMatrix, fallingBlock);
    }
    
    public SquareMatrix getSquareMatrix() {
        return squareMatrix;
    }
    
    public Block getFallingBlock() {
        return fallingBlock;
    }
    
    @Override
    public void update() {
        squareMatrix.update();
    }

}
