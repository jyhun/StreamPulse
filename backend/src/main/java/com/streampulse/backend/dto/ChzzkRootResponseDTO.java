package com.streampulse.backend.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChzzkRootResponseDTO {
    private int code;
    private String message;
    private LiveListResponseDTO content;
}
