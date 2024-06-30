package com.jhsfully.omokserver.service;

import com.jhsfully.omokserver.entity.Room;

public interface GameService {
    Room getGameData(String roomId);
}
