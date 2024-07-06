package com.jhsfully.omokserver.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@Builder
@RedisHash(value = "Player") //TODO 오목게임의 최대시간을 계산해서 TTL을 계산해서 기입!
public class Player {

    @Id
    private String playerId;
    private String playerName;
    private LocalDateTime lastConnectedAt;

    public void updateLastConnectedAt(LocalDateTime lastConnectedAt) {
        this.lastConnectedAt = lastConnectedAt;
    }

}
