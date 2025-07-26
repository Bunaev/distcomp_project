package com.publisher.config;

import com.publisher.repository.CreatorRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Помечает класс как конфигурационный для Spring
@EnableWebSecurity // Включает механизмы безопасности Spring Security
@AllArgsConstructor // Автоматически создает конструктор с зависимостями
public class SecurityConfig {

    // Репозиторий для работы с пользователями в БД
    private final CreatorRepository creatorRepository;

    /**
     * Бин для кодирования паролей.
     * Используется алгоритм BCrypt с солью (salt).
     *
     * @return PasswordEncoder - кодировщик паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Рекомендуемый алгоритм хеширования
    }

    /**
     * Бин для загрузки данных пользователей.
     * Связывает систему аутентификации с вашей БД.
     *
     * @return UserDetailsService - сервис загрузки пользователей
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return login -> creatorRepository.findByLogin(login)
                .map(creator -> User.builder()
                        .username(creator.getLogin()) // Логин как идентификатор
                        .password(creator.getPassword()) // Уже закодированный пароль
                        .authorities(creator.getRole().name()) // Роли пользователя
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Основная конфигурация безопасности.
     * Определяет:
     * - Какие URL защищены
     * - Какие роли требуются
     * - Какие фильтры применяются
     *
     * @param http - объект конфигурации HttpSecurity
     * @param jwtRequestFilter - наш кастомный JWT-фильтр
     * @return SecurityFilterChain - цепочка фильтров безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtRequestFilter jwtRequestFilter) throws Exception {

        http
                // Отключаем CSRF защиту (не нужна для REST API с JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Настройка авторизации запросов
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем все запросы к API v1.0 без аутентификации
                        .requestMatchers("/api/v1.0/**").permitAll()

                        // DELETE-запросы к creators требуют роли ADMIN
                        .requestMatchers(HttpMethod.DELETE, "api/v2.0/creators/**").hasAuthority("ADMIN")

                        // Регистрация нового пользователя доступна всем
                        .requestMatchers(HttpMethod.POST, "/api/v2.0/creators").permitAll()

                        // Логин доступен всем
                        .requestMatchers(HttpMethod.POST, "/api/v2.0/login").permitAll()

                        // Получение списка creators требует аутентификации
                        .requestMatchers(HttpMethod.GET, "/api/v2.0/creators").authenticated()

                        // Все остальные запросы разрешены
                        .anyRequest().permitAll()
                )

                // Настраиваем управление сессией
                .sessionManagement(session -> session
                        // Без состояния (не используем сессии)
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Обработка ошибок аутентификации
                .exceptionHandling(exceptions -> exceptions
                        // Возвращаем 401 при неудачной аутентификации
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                );

        // Добавляем наш JWT-фильтр перед стандартным фильтром аутентификации
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Собираем конфигурацию
    }

    /**
     * Бин для аутентификации пользователей.
     * Использует наш UserDetailsService и PasswordEncoder.
     *
     * @param http - объект HttpSecurity
     * @return AuthenticationManager - менеджер аутентификации
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(userDetailsService()) // Наш сервис пользователей
                .passwordEncoder(passwordEncoder()); // Наш кодировщик паролей
        return builder.build();
    }
}
