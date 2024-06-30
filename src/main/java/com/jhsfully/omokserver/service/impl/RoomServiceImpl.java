package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.RoomRepository;
import com.jhsfully.omokserver.dto.RoomSimpleDataDto;
import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.service.RoomService;
import com.jhsfully.omokserver.type.Piece;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public RoomSimpleDataDto createRoom() {

        String playerId = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();

        Room room = Room.builder()
            .roomId(roomId)
            .playerIdList(Collections.singletonList(playerId))
            .build();

        room.getBoard().set(Room.IX(14, 14), Piece.BLACK);

        roomRepository.save(room);

        return new RoomSimpleDataDto(roomId, playerId);
    }
}
