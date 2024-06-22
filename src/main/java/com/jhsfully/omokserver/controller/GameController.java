package com.jhsfully.omokserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 오목게임을 수행하고, 검증하고, 관리하는 Controller
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    /**
     * 오목방 게임 데이터를 주기적으로 호출되어 응답을 전달하는 메서드입니다.
     * (해당 메서드의 응답을 통해, 게임의 진행 상황을 감시하고 결정합니다.)
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getGameData() {
        return null;
    }

    /**
     * 플레이어가 현재 해당하는 방에 게임돌을 두는 작업을 수행합니다.
     * @param roomId  => 현재 게임 방번호.
     * @return
     */
    @PatchMapping("/{roomId}")
    public ResponseEntity<?> putPiece(@PathVariable String roomId) {
        return null;
    }

}
