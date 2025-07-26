package com.publisher.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;

/**
 * Этот класс работает как охранник на входе в каждый endpoint.
 * Он стоит перед дверью и проверяет у каждого:
 * 1. Есть ли у тебя браслет (токен)
 * 2. Не поддельный ли он
 * 3. Не просрочен ли он
 * Только после проверки он пропускает тебя внутрь.
 */
@Component
@RequiredArgsConstructor// Spring создаст одного охранника на всё приложение
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    // Охранник знает, что пропуск должен быть в конверте с надписью "Authorization"
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // И что перед самим пропуском должно быть слово "Bearer "
    public static final String BEARER_PREFIX = "Bearer ";

    // Охранник пользуется услугами фабрики пропусков (JwtTokenUtil)
    private final JwtTokenUtil jwtTokenUtil;
    // И телефонной книгой, где записаны все гости (UserDetailsService)
    private final UserDetailsService userDetailsService;

    /**
     * Главный метод охранника, который вызывается для каждого гостя (запроса)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException, java.io.IOException {

        // Логирование всех заголовков входящего запроса для отладки
        // Перебираем все имена заголовков и выводим их значения вместе с путем и методом запроса
        Collections.list(request.getHeaderNames()).forEach(header ->
                log.debug("В фильтр поступил запрос.'\n'{}: {}; ссылка: {}; метод: {}.",
                        header, request.getHeader(header),request.getServletPath(), request.getMethod()));

        // 1. Получаем заголовок Authorization из запроса
        // AUTHORIZATION_HEADER = "Authorization" (стандартное название заголовка для JWT)
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 2. Проверяем наличие и формат заголовка Authorization
        // Если заголовок отсутствует или не начинается с "Bearer " (BEARER_PREFIX),
        // пропускаем запрос дальше по цепочке фильтров без аутентификации
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response); // Передаем запрос следующему фильтру
            return; // Завершаем выполнение текущего фильтра
        }

        // 3. Извлекаем чистый JWT токен (удаляем префикс "Bearer ")
        // Например: из "Bearer eyJhbGciOi..." получаем "eyJhbGciOi..."
        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 4. Извлекаем имя пользователя из токена.
            // Используется JwtTokenUtil для парсинга JWT и получения subject (username)
            String username = jwtTokenUtil.getUsernameFromToken(token);

            // 5. Загружаем данные пользователя из базы/сервиса
            // UserDetailsService ищет пользователя по username и возвращает его данные,
            // включая пароль (хэш) и список ролей/прав
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 6. Проверяем валидность токена
            // JwtTokenUtil проверяет:
            // - Соответствие username в токене и в UserDetails
            // - Не истек ли срок действия токена
            // - Совпадает ли подпись токена с ожидаемой
            if (jwtTokenUtil.validateToken(token, userDetails)) {
                // 7. Создаем объект аутентификации
                // UsernamePasswordAuthenticationToken - стандартная реализация Authentication,
                // содержащая:
                // - Principal (UserDetails)
                // - Credentials (null, так как пароль уже проверен)
                // - Authorities (список ролей/прав)
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 8. Добавляем детали запроса к объекту аутентификации
                // WebAuthenticationDetails содержит дополнительную информацию:
                // - IP-адрес клиента
                // - Идентификатор сессии
                // - Другие параметры запроса
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. Сохраняем объект аутентификации в SecurityContext
                // SecurityContextHolder - хранилище контекста безопасности для текущего потока
                // После этой точки пользователь считается аутентифицированным
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (SignatureException e) {
            // Ошибка: подпись токена не совпадает с ожидаемой
            // Возможные причины:
            // - Изменение токена на клиенте
            // - Несоответствие секретных ключей
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
            return; // Прерываем цепочку фильтров
        } catch (ExpiredJwtException e) {
            // Ошибка: срок действия токена истек
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (Exception e) {
            // Любая другая ошибка при проверке токена
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }
        // 10. Если все проверки пройдены, передаем запрос дальше по цепочке фильтров
        // На этом этапе:
        // - Пользователь аутентифицирован
        // - SecurityContext содержит данные пользователя
        // - Следующие фильтры/контроллеры могут использовать эту информацию
        chain.doFilter(request, response);
    }
}
