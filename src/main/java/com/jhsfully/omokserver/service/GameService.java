package com.jhsfully.omokserver.service;

import com.jhsfully.omokserver.dto.RoomDetailDto;

public interface GameService {
    RoomDetailDto getGameData(String roomId, String playerId);
}
