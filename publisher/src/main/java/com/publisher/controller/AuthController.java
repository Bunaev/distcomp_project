package com.publisher.controller;

import com.publisher.config.LoginAlreadyExistException;
import com.publisher.dto.in.CreatorRequestTo;
import com.publisher.dto.in.LoginRequestDTO;
import com.publisher.service.CreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v2.0")
@AllArgsConstructor
public class AuthController {
    private final CreatorService creatorService;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/creators")
    public ResponseEntity<Object> registerCreator(@Valid @RequestBody CreatorRequestTo creator) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(creatorService.create(creator));
        } catch (LoginAlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.status(HttpStatus.OK).body(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(request);
        }
    }
}
