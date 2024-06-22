package com.jhsfully.omokserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 오목 게임 방을 관리하는 Controller 입니다.
 */

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    /**
     * 현재 게임 대전 상대를 기다리있는 방 목록을 조회합니다. (PlayerList가 1인 경우)
     * @return
     */
    public ResponseEntity<?> getRoomList() {
        return null;
    }

    /**
     * 오목 게임방을 만들고 이에 대한 정보를 리턴합니다.
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createRoom() {
        return null;
    }

    /**
     * 이미 만들어진 오목 게임방에 입장합니다.
     * @param roomId
     * @return
     */
    @PatchMapping("/{roomId}")
    public ResponseEntity<?> enterRoom(@PathVariable String roomId) {
        return null;
    }

    /**
     * 오목 게임방을 삭제합니다.
     * @param roomId
     * @return
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        return null;
    }

}
