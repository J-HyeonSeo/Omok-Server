package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.RoomRepository;
import com.jhsfully.omokserver.dto.RoomCreateAndEnterDto;
import com.jhsfully.omokserver.dto.RoomSimpleDto;
import com.jhsfully.omokserver.dto.request.CreateRoomRequestDto;
import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.security.TokenProvider;
import com.jhsfully.omokserver.service.RoomService;
import com.jhsfully.omokserver.type.State;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final TokenProvider tokenProvider;

    @Override
    public List<RoomSimpleDto> getRoomWaitingRoomList() {
        return roomRepository.findByNowState(State.WAIT)
            .stream()
            .map(RoomSimpleDto::of)
            .collect(Collectors.toList());
    }

    @Override
    public RoomCreateAndEnterDto createRoom(CreateRoomRequestDto roomRequestDto) {

        String playerId = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();

        Room room = Room.builder()
            .roomId(roomId)
            .roomTitle(roomRequestDto.getRoomTitle())
            .playerIdList(Collections.singletonList(playerId))
            .build();

        Room createdRoom = roomRepository.save(room);

        return new RoomCreateAndEnterDto(createdRoom.getRoomId(),
            tokenProvider.generateAccessToken(playerId));
    }
}
