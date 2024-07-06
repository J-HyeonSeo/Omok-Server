package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.PlayerRepository;
import com.jhsfully.omokserver.entity.Player;
import com.jhsfully.omokserver.service.PlayerService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    public String createPlayer(String playerName) {

        String playerId = UUID.randomUUID().toString();

        Player player = Player.builder()
            .playerId(playerId)
            .playerName(playerName)
            .lastConnectedAt(LocalDateTime.now())
            .build();

        Player createdPlayer = playerRepository.save(player);

        return createdPlayer.getPlayerId();
    }
}
