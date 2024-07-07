package com.jhsfully.omokserver.dto;

import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.type.Piece;
import com.jhsfully.omokserver.type.State;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDetailDto {
    private String roomId;
    private String roomTitle;
    private List<String> playerIdList;
    private String blackPlayerId;
    private String whitePlayerId;
    private String otherPlayerName;
    private State nowState;
    private LocalDateTime turnedAt;
    private String winnerPlayerId;
    private Piece[] board;

    public static RoomDetailDto of(Room room, String otherPlayerName) {
        return RoomDetailDto.builder()
            .roomId(room.getRoomId())
            .roomTitle(room.getRoomTitle())
            .playerIdList(room.getPlayerIdList())
            .blackPlayerId(room.getBlackPlayerId())
            .otherPlayerName(otherPlayerName)
            .whitePlayerId(room.getWhitePlayerId())
            .nowState(room.getNowState())
            .turnedAt(room.getTurnedAt())
            .winnerPlayerId(room.getWinnerPlayerId())
            .board(room.getBoard())
            .build();
    }
}
