package com.publisher.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Этот класс - как фабрика по производству и проверке пропусков (JWT токенов)
 * для нашего приложения. Он умеет:
 * 1. Создавать новые пропуска (токены)
 * 2. Проверять, не поддельные ли пропуска
 * 3. Читать информацию с пропусков
 */
@Component // Помечает класс как компонент Spring (управляется контейнером IoC)
public class JwtTokenUtil {

    // Секретный ключ для подписи токенов.
    // Загружается из application.properties (jwt.secret=ваш_ключ)
    // Должен быть длиной минимум 256 бит (32 символа) для алгоритма HS256
    @Value("${jwt.secret}")
    private String secret;

    // Время жизни токена в миллисекундах
    // Например: 86400000 = 24 часа (1000*60*60*24)
    @Value("${jwt.lifetime}")
    private long jwtLifetime;

    /**
     * Преобразует строковый секрет в криптографический ключ
     * для алгоритма HMAC-SHA256.
     *
     * Используется стандартную библиотеку jjwt (Keys.hmacShaKeyFor)
     *
     * @return SecretKey - готовый ключ для подписи/верификации JWT
     */
    private SecretKey getSigningKey() {
        // Преобразуем строку в байты (используется кодировка платформы по умолчанию)
        // Для надежности можно указать кодировку явно: secret.getBytes(StandardCharsets.UTF_8)
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Извлекает все claims (утверждения) из JWT токена.
     * В процессе проверяет подпись токена.
     *
     * @param token - JWT токен в виде строки
     * @return Claims - объект с данными токена
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Устанавливаем ключ для проверки подписи
                .build()
                .parseClaimsJws(token) // Парсим и проверяем токен
                .getBody(); // Извлекаем тело токена (claims)
    }

    /**
     * Универсальный метод для извлечения конкретного claim из токена.
     *
     * @param token - JWT токен
     * @param claimsResolver - функция-извлекатель (например, Claims::getSubject)
     * @return T - значение claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token); // Получаем все claims
        return claimsResolver.apply(claims); // Применяем функцию извлечения
    }

    /**
     * Извлекает имя пользователя (subject) из токена.
     *
     * @param token - JWT токен
     * @return String - имя пользователя
     */
    public String getUsernameFromToken(String token) {
        // Используем метод-ссылку Claims::getSubject для извлечения subject
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения срока действия токена.
     *
     * @param token - JWT токен
     * @return Date - дата истечения
     */
    public Date getExpirationDateFromToken(String token) {
        // Используем метод-ссылку Claims::getExpiration
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param token - JWT токен
     * @return Boolean - true если токен просрочен
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        // Сравниваем дату истечения с текущей датой
        return expiration.before(new Date());
    }

    /**
     * Генерирует новый JWT токен для пользователя.
     *
     * @param userDetails - данные пользователя (Spring Security)
     * @return String - JWT токен в виде строки
     */
    public String generateToken(UserDetails userDetails) {
        // Создаем claims (утверждения) - данные, которые будут храниться в токене
        Map<String, Object> claims = new HashMap<>();
        // Добавляем роли пользователя (преобразуются в JSON-массив)
        claims.put("roles", userDetails.getAuthorities());

        // Строим токен с помощью билдера:
        return Jwts.builder()
                .setClaims(claims) // Устанавливаем claims
                .setSubject(userDetails.getUsername()) // Устанавливаем subject (имя пользователя)
                .setIssuedAt(new Date()) // Текущая дата как время создания
                .setExpiration(new Date(System.currentTimeMillis() + jwtLifetime)) // Дата истечения
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Подписываем ключом с алгоритмом HS256
                .compact(); // Преобразуем в компактную строку формата JWT
    }

    /**
     * Проверяет валидность токена для конкретного пользователя.
     *
     * @param token - JWT токен
     * @param userDetails - данные пользователя для проверки
     * @return Boolean - true если токен валиден
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        // Извлекаем имя пользователя из токена
        final String username = getUsernameFromToken(token);
        // Проверяем:
        // 1. Что имя пользователя в токене совпадает с текущим
        // 2. Что токен не просрочен
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
