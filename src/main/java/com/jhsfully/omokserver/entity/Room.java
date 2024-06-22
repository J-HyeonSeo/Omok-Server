package com.jhsfully.omokserver.entity;

import com.jhsfully.omokserver.type.Piece;
import com.jhsfully.omokserver.type.State;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@RedisHash(value = "Room")
public class Room {

    @Id
    private String roomId;
    private List<String> playerIdList;
    private String blackPlayerId;
    private String whitePlayerId;
    private State nowState;
    private LocalDateTime turnedAt;
    private String winnerPlayerId;
    private Piece[][] board = new Piece[15][15];

}
