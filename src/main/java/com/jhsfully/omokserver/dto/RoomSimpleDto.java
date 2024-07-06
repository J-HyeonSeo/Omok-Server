package com.jhsfully.omokserver.dto;

import com.jhsfully.omokserver.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomSimpleDto {
    private String roomId;
    private String roomTitle;

    public static RoomSimpleDto of(Room room) {
        return new RoomSimpleDto(room.getRoomId(), room.getRoomTitle());
    }
}
