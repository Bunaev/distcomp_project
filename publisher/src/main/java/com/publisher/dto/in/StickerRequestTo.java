package com.publisher.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StickerRequestTo implements Serializable {
    @Positive
    private Long id;
    @NotBlank
    @Size(min = 2, max = 32)
    private String name;
}
