package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.RoomRepository;
import com.jhsfully.omokserver.dto.RoomSimpleDataDto;
import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.security.TokenProvider;
import com.jhsfully.omokserver.service.RoomService;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final TokenProvider tokenProvider;

    @Override
    public RoomSimpleDataDto createRoom() {

        String playerId = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();

        Room room = Room.builder()
            .roomId(roomId)
            .playerIdList(Collections.singletonList(playerId))
            .build();

        Room createdRoom = roomRepository.save(room);

        return new RoomSimpleDataDto(createdRoom.getRoomId(),
            tokenProvider.generateAccessToken(playerId));
    }
}
