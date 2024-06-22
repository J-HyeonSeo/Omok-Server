package com.jhsfully.omokserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "Player") //TODO 오목게임의 최대시간을 계산해서 TTL을 계산해서 기입!
public class Player {

    @Id
    private String playerId;
    private String playerName;
    private long heartBeat;

}
