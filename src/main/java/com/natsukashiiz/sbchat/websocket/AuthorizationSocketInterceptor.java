package com.natsukashiiz.sbchat.websocket;

import com.natsukashiiz.sbchat.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Objects;

@Log4j2
@RequiredArgsConstructor
public class AuthorizationSocketInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (Objects.nonNull(accessor) && StompCommand.CONNECT.equals(accessor.getCommand())) {
            var authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
                throw new AuthenticationCredentialsNotFoundException("Missing or invalid token");
            }

            var token = authHeader.substring(7);
            Jwt jwt;

            try {
                jwt = tokenService.decode(token);
            } catch (Exception e) {
                log.warn(e.getMessage());
                throw new AuthenticationCredentialsNotFoundException("Invalid token");
            }

            JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
            accessor.setUser(authentication);
        }
        return message;
    }
}
