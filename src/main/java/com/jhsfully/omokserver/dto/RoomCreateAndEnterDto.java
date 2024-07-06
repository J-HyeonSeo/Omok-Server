package com.jhsfully.omokserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomCreateAndEnterDto {

    private String roomId;
    private String accessToken;

}
