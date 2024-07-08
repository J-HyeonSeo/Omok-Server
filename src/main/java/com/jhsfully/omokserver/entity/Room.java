package com.jhsfully.omokserver.entity;

import com.jhsfully.omokserver.type.Piece;
import com.jhsfully.omokserver.type.State;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "Room", timeToLive = 4500)
public class Room {

    @Id
    private String roomId;
    private String roomTitle;
    @Builder.Default
    private List<String> playerIdList = new ArrayList<>();
    private String blackPlayerId;
    private String whitePlayerId;
    @Builder.Default
    @Indexed
    private State nowState = State.WAIT;
    private LocalDateTime turnedAt;
    private String winnerPlayerId;
    @Builder.Default
    private Piece[] board = initializeBoard();

    private final static int MAX_INDEX = 15;

    public static Piece[] initializeBoard() {
        Piece[] newBoard = new Piece[15 * 15];
        Arrays.fill(newBoard, Piece.NONE);
        return newBoard;
    }

    public static int IX(int x, int y) {
        return x * 15 + y;
    }

}
