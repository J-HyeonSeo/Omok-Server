package com.jhsfully.omokserver.controller;

import com.jhsfully.omokserver.dto.RoomCreateAndEnterDto;
import com.jhsfully.omokserver.dto.RoomSimpleDto;
import com.jhsfully.omokserver.dto.request.CreateRoomRequestDto;
import com.jhsfully.omokserver.dto.request.EnterRoomRequestDto;
import com.jhsfully.omokserver.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 오목 게임 방을 관리하는 Controller 입니다.
 */

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 현재 게임 대전 상대를 기다리있는 방 목록을 조회합니다. (PlayerList가 1인 경우)
     */
    @GetMapping("/public")
    public ResponseEntity<List<RoomSimpleDto>> getRoomList() {
        return ResponseEntity.ok(roomService.getRoomWaitingRoomList());
    }

    /**
     * 오목 게임방을 만들고 이에 대한 정보를 리턴합니다.
     */
    @PostMapping("/public")
    public ResponseEntity<RoomCreateAndEnterDto> createRoom(@RequestBody @Valid CreateRoomRequestDto roomRequestDto) {
        return ResponseEntity.ok(roomService.createRoomAndPlayer(roomRequestDto));
    }

    /**
     * 이미 만들어진 오목 게임방에 입장합니다.
     */
    @PatchMapping("/public/enter")
    public ResponseEntity<RoomCreateAndEnterDto> enterRoom(@RequestBody @Valid EnterRoomRequestDto enterRoomRequestDto) {
        return ResponseEntity.ok(roomService.enterRoomAndCreatePlayer(enterRoomRequestDto));
    }

}
