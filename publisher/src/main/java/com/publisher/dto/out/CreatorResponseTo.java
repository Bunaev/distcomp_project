package com.publisher.dto.out;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatorResponseTo implements Serializable {
    private Long id;
    private String login;
    private String password;
    private String firstname;
    private String lastname;
}
