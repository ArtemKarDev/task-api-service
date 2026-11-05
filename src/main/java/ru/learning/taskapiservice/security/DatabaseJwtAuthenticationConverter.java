package ru.learning.taskapiservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.learning.taskapiservice.entity.UserAccount;
import ru.learning.taskapiservice.repository.UserAccountRepository;

import java.util.List;

/**
 * После проверки подписи и срока токена сверяется с базой: пользователь должен существовать
 * и быть включённым, а роль берётся из базы (а не из токена), поэтому изменения действуют сразу.
 */
@Component
@RequiredArgsConstructor
public class DatabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserAccountRepository repository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UserAccount user = repository.findByUsername(jwt.getSubject())
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new InvalidBearerTokenException("User is disabled or no longer exists"));

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new JwtAuthenticationToken(jwt, authorities, user.getUsername());
    }
}