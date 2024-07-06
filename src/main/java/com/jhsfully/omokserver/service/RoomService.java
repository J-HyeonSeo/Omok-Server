package com.jhsfully.omokserver.service;

import com.jhsfully.omokserver.dto.RoomCreateAndEnterDto;
import com.jhsfully.omokserver.dto.RoomSimpleDto;
import com.jhsfully.omokserver.dto.request.CreateRoomRequestDto;
import java.util.List;

public interface RoomService {

    List<RoomSimpleDto> getRoomWaitingRoomList();
    RoomCreateAndEnterDto createRoom(CreateRoomRequestDto roomRequestDto);

}
