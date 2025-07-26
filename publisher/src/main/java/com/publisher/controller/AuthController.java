package com.publisher.controller;

import com.publisher.config.JwtTokenUtil;
import com.publisher.config.LoginAlreadyExistException;
import com.publisher.dto.in.CreatorRequestTo;
import com.publisher.dto.in.LoginRequestDTO;
import com.publisher.service.CreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v2.0")
@AllArgsConstructor
public class AuthController {
    private final CreatorService creatorService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;


    @PostMapping("/creators")
    public ResponseEntity<Object> registerCreator(@Valid @RequestBody CreatorRequestTo creator) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(creatorService.create(creator));
        } catch (LoginAlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Метод для выдачи пропуска (вход в систему).
     * Как администратор, который проверяет твой ID и выдаёт браслет.
     */
    @PostMapping("/login") // Срабатывает на POST /api/v2.0/login
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            // 1. Проверяем логин/пароль через AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(), // Твой логин
                            request.getPassword() // Твой пароль
                    )
            );

            // 2. Записываем в журнал, что ты проверен
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Ищем тебя в телефонной книге
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());

            // 4. Делаем для тебя новый браслет (токен)
            String token = jwtTokenUtil.generateToken(userDetails);

            // 5. Смотрим, какие комнаты тебе доступны (роли)
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList(); // Собираем в список
            log.debug("Авторизация успешна. User: {}, token: {}, roles: {}.", userDetails.getUsername(), token, roles);
            // 6. Отдаём тебе браслет и информацию о тебе
            return ResponseEntity.ok(Map.of("access_token", token, "user", userDetails.getUsername(), "roles", roles));
        } catch (AccessDeniedException e) {
            // Если ошибка (неправильный логин/пароль), скажем об этом (401 Unauthorized)
            log.debug("В доступе отказано. User: {}, token: {}.", request.getLogin(), request.getPassword());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный логин или пароль");
        }
    }
}
