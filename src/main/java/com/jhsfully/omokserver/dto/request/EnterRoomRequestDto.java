package com.jhsfully.omokserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EnterRoomRequestDto {

    @NotBlank(message = "방 ID는 필수 값입니다.")
    private String roomId;

    @NotBlank(message = "플레이어명을 입력해주세요.")
    @Size(min = 2, max = 15, message = "플레이어명은 2~7 자로 입력해주세요.")
    private String playerName;

}
