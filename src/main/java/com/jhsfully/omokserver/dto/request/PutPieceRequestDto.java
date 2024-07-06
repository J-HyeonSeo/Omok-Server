package com.jhsfully.omokserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Getter
@NoArgsConstructor
public class PutPieceRequestDto {
    @NotBlank(message = "방 ID는 필수 값입니다.")
    private String roomId;

    @Range(min = 0, max = 14, message = "row값은 0 ~ 14 값으로 입력해주세요.")
    private int row;
    @Range(min = 0, max = 14, message = "col값은 0 ~ 14 값으로 입력해주세요.")
    private int col;
}
