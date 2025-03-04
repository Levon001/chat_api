package com.example.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import com.example.entity.ChatUser;
import com.example.dto.UserDTO;

@ApplicationScoped
public class AuthService {
    private static final Logger LOG = Logger.getLogger(AuthService.class);

    public Uni<String> register(UserDTO userDTO) {
        return Panache.withTransaction(() ->
                ChatUser.findByUsername(userDTO.username())
                        .flatMap(existingUser -> {
                            if (existingUser != null) {
                                throw new WebApplicationException("Username already exists", Response.Status.CONFLICT);
                            }
                            ChatUser user = new ChatUser();
                            user.username = userDTO.username();
                            user.password = BcryptUtil.bcryptHash(userDTO.password());
                            return user.persist()
                                    .onItem().invoke(() -> LOG.info("User persisted: " + user.username))
                                    .onFailure().invoke(throwable -> LOG.error("Failed to persist user: " + throwable.getMessage()));
                        })
        ).map(v -> Jwt.issuer("chat-api")
                .subject(userDTO.username())
                .groups("user")
                .sign());
    }

    public Uni<String> login(UserDTO userDTO) {
        return Panache.withTransaction(() ->
                ChatUser.findByUsername(userDTO.username())
                        .map(user -> {
                            if (user != null && BcryptUtil.matches(userDTO.password(), user.password)) {
                                return Jwt.issuer("chat-api")
                                        .subject(userDTO.username())
                                        .groups("user")
                                        .sign();
                            }
                            return null;
                        })
        );
    }
}