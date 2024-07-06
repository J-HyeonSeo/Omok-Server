package com.jhsfully.omokserver.service;

import com.jhsfully.omokserver.dto.RoomDetailDto;
import com.jhsfully.omokserver.dto.request.PutPieceRequestDto;

public interface GameService {
    RoomDetailDto getGameData(String roomId, String playerId);
    RoomDetailDto putPiece(PutPieceRequestDto pieceRequestDto, String playerId);
}
