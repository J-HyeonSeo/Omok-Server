package com.jhsfully.omokserver.service;

import com.jhsfully.omokserver.dto.RoomCreateAndEnterDto;
import com.jhsfully.omokserver.dto.RoomSimpleDto;
import com.jhsfully.omokserver.dto.request.CreateRoomRequestDto;
import com.jhsfully.omokserver.dto.request.EnterRoomRequestDto;
import java.util.List;

public interface RoomService {

    List<RoomSimpleDto> getRoomWaitingRoomList();
    RoomCreateAndEnterDto createRoomAndPlayer(CreateRoomRequestDto roomRequestDto);
    RoomCreateAndEnterDto enterRoomAndCreatePlayer(EnterRoomRequestDto roomRequestDto);
    void exitRoom(String roomId, String playerId);

}
