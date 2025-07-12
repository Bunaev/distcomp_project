package com.publisher.dto.out;

import com.publisher.entities.Role;
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
    private String role;
}
