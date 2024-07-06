package com.jhsfully.omokserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRoomRequestDto {

    @NotBlank(message = "방 제목을 입력해주세요.")
    @Size(min = 2, max = 15, message = "방 제목은 2~15 자로 입력해주세요.")
    private String roomTitle;
}
