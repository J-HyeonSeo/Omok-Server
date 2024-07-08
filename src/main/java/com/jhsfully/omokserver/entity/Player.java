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
@RedisHash(value = "Player", timeToLive = 3600)
public class Player {

    @Id
    private String playerId;
    private String playerName;
    private LocalDateTime lastConnectedAt;

    public void updateLastConnectedAt(LocalDateTime lastConnectedAt) {
        this.lastConnectedAt = lastConnectedAt;
    }

}
