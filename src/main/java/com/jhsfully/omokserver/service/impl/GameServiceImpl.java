package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.PlayerRepository;
import com.jhsfully.omokserver.dao.RoomRepository;
import com.jhsfully.omokserver.dto.RoomDetailDto;
import com.jhsfully.omokserver.entity.Player;
import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.service.GameService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    @Override
    public RoomDetailDto getGameData(String roomId, String playerId) {

        Room room = roomRepository.findById(roomId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();

        validateGameData(room, playerId);

        // 최근 연결된 시각을 업데이트 하여, 클라이언트의 연결 상태를 업데이트.
        player.updateLastConnectedAt(LocalDateTime.now());

        // 상대방 이름 가져오기.
        String otherPlayerName = getOtherPlayerName(room, playerId);

        return RoomDetailDto.of(roomRepository.findById(roomId).orElseThrow(), otherPlayerName);
    }

    private void validateGameData(Room room, String playerId) {
        if (room.getPlayerIdList().stream().noneMatch(it -> it.equals(playerId))) {
            throw new RuntimeException("현재 참가한 게임방이 아닙니다.");
        }
    }

    private String getOtherPlayerName(Room room, String playerId) {
        if (room.getPlayerIdList().size() <= 1) {
            return "";
        }

        String otherPlayerId = room.getPlayerIdList().stream()
            .filter(id -> !id.equals(playerId))
            .findFirst()
            .orElse("");

        if (!StringUtils.hasText(otherPlayerId)) return "";

        Player otherPlayer = playerRepository.findById(otherPlayerId).orElseThrow();

        return otherPlayer.getPlayerId();
    }
}
