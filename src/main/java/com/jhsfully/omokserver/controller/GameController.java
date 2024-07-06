package com.jhsfully.omokserver.controller;

import com.jhsfully.omokserver.dto.AuthDto;
import com.jhsfully.omokserver.dto.RoomDetailDto;
import com.jhsfully.omokserver.dto.request.PutPieceRequestDto;
import com.jhsfully.omokserver.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 오목게임을 수행하고, 검증하고, 관리하는 Controller
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;

    /**
     * 오목방 게임 데이터를 주기적으로 호출되어 응답을 전달하는 메서드입니다.
     * (해당 메서드의 응답을 통해, 게임의 진행 상황을 감시하고 결정합니다.)
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailDto> getGameData(@PathVariable String roomId, @AuthenticationPrincipal
        AuthDto authDto) {
        return ResponseEntity.ok(gameService.getGameData(roomId, authDto.getPlayerId()));
    }

    /**
     * 플레이어가 현재 해당하는 방에 게임돌을 두는 작업을 수행합니다.
     */
    @PatchMapping
    public ResponseEntity<RoomDetailDto> putPiece(@RequestBody @Valid PutPieceRequestDto pieceRequestDto,
        @AuthenticationPrincipal AuthDto authDto) {
        return ResponseEntity.ok(gameService.putPiece(pieceRequestDto, authDto.getPlayerId()));
    }

}
