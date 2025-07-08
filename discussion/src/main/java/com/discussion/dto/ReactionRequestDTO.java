package com.discussion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionRequestDTO {
    private Long id;
    @Positive
    private Long articleId;
    @NotBlank
    @Size(min = 2, max = 2048)
    private String content;
    private RequestMethod requestMethod;
}
