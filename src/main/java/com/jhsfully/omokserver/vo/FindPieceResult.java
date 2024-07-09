package com.jhsfully.omokserver.vo;

import com.jhsfully.omokserver.type.Piece;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPieceResult {
    private int count;
    private int distance;
    private int endRow;
    private int endCol;
    private Piece lastPiece;

    public FindPieceResult(int count, int endRow, int endCol) {
        this.count = count;
        this.endRow = endRow;
        this.endCol = endCol;
    }

    public FindPieceResult(int count, int distance, int endRow, int endCol, Piece lastPiece) {
        this.count = count;
        this.distance = distance;
        this.endRow = endRow;
        this.endCol = endCol;
        this.lastPiece = lastPiece;
    }
}
