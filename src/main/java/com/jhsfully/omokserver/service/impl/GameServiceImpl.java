package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.RoomRepository;
import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final RoomRepository roomRepository;

    @Override
    public Room getGameData(String roomId) {
        return roomRepository.findById(roomId).orElseThrow();
    }
}
