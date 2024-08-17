package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.PlayerRepository;
import com.jhsfully.omokserver.dao.RoomRepository;
import com.jhsfully.omokserver.dto.RoomCreateAndEnterDto;
import com.jhsfully.omokserver.dto.RoomSimpleDto;
import com.jhsfully.omokserver.dto.request.CreateRoomRequestDto;
import com.jhsfully.omokserver.dto.request.EnterRoomRequestDto;
import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.security.TokenProvider;
import com.jhsfully.omokserver.service.PlayerService;
import com.jhsfully.omokserver.service.RoomService;
import com.jhsfully.omokserver.type.State;
import java.time.LocalDateTime;
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
    private final PlayerRepository playerRepository;
    private final TokenProvider tokenProvider;
    private final PlayerService playerService;

    @Override
    public List<RoomSimpleDto> getRoomWaitingRoomList() {
        return roomRepository.findByNowState(State.WAIT)
            .stream()
            .map(RoomSimpleDto::of)
            .collect(Collectors.toList());
    }

    @Override
    public RoomCreateAndEnterDto createRoomAndPlayer(CreateRoomRequestDto roomRequestDto) {

        String roomId = UUID.randomUUID().toString();
        String playerId = playerService.createPlayer(roomRequestDto.getPlayerName());

        Room room = Room.builder()
            .roomId(roomId)
            .roomTitle(roomRequestDto.getRoomTitle())
            .playerIdList(Collections.singletonList(playerId))
            .blackPlayerId(playerId)
            .build();

        Room createdRoom = roomRepository.save(room);

        return new RoomCreateAndEnterDto(createdRoom.getRoomId(),
            tokenProvider.generateAccessToken(playerId), playerId);
    }

    @Override
    public RoomCreateAndEnterDto enterRoomAndCreatePlayer(EnterRoomRequestDto roomRequestDto) {

        Room room = roomRepository.findById(roomRequestDto.getRoomId()).orElseThrow();

        if (room.getNowState() != State.WAIT || room.getPlayerIdList().size() > 1) {
            throw new RuntimeException("해당 방은 입장할 수 없는 상태입니다.");
        }

        String playerId = playerService.createPlayer(roomRequestDto.getPlayerName());

        room.getPlayerIdList().add(playerId);
        room.setNowState(State.BLACK);
        room.setWhitePlayerId(playerId);
        room.setTurnedAt(LocalDateTime.now());
        roomRepository.save(room);

        return new RoomCreateAndEnterDto(room.getRoomId(), tokenProvider.generateAccessToken(playerId), playerId);
    }

    @Override
    public void exitRoom(String roomId, String playerId) {
        Room room = roomRepository.findById(roomId).orElseThrow();

        if (room.getPlayerIdList().stream().noneMatch(x -> x.equals(playerId))) {
            throw new RuntimeException("권한이 없습니다.");
        }

        // Player 대상에서 제외합니다.
        room.getPlayerIdList().remove(playerId);

        // 플레이어가 없다면, 플레이어와 방을 모두 파기합니다.
        if (room.getPlayerIdList().isEmpty()) {
            playerRepository.deleteById(playerId);
            roomRepository.deleteById(roomId);
            return;
        }

        // 1명 남았다면, 남은 플레이어를 블랙으로 수정하고, 상태는 WAIT으로 변경합니다.
        room.setBlackPlayerId(playerId);
        room.setWhitePlayerId(null);
        room.setNowState(State.WAIT);
        room.setTurnedAt(null);
        room.setBoard(Room.initializeBoard());

        // 수정된 Room을 저장합니다.
        roomRepository.save(room);
    }
}
