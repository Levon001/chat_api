package com.example.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.example.dto.*;
import com.example.entity.Message;
import com.example.service.AuthService;
import java.util.List;

@Path("/api/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    public Uni<Response> register(UserDTO userDTO) {
        return authService.register(userDTO)
                .map(token -> Response.ok(new TokenResponse(token)).build());
    }

    @POST
    @Path("/login")
    public Uni<Response> login(UserDTO userDTO) {
        return authService.login(userDTO)
                .map(token -> {
                    if (token == null) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                    return Response.ok(new TokenResponse(token)).build();
                });
    }

    @POST
    @Path("/broadcast")
    @RolesAllowed("user")
    public Uni<Response> broadcast(MessageDTO messageDTO) {
        Message message = new Message();
        message.sender = messageDTO.sender();
        message.content = messageDTO.content();
        message.timestamp = System.currentTimeMillis();

        return Panache.withTransaction(() ->
                message.persist()
                        .map(v -> Response.ok().build())
        );
    }

    @POST
    @Path("/direct")
    @RolesAllowed("user")
    public Uni<Response> sendDirect(MessageDTO messageDTO) {
        Message message = new Message();
        message.sender = messageDTO.sender();
        message.recipient = messageDTO.recipient();
        message.content = messageDTO.content();
        message.timestamp = System.currentTimeMillis();

        return Panache.withTransaction(() ->
                message.persist()
                        .map(v -> Response.ok().build())
        );
    }

    @POST
    @Path("/group")
    @RolesAllowed("user")
    public Uni<Response> sendGroup(MessageDTO messageDTO) {
        Message message = new Message();
        message.sender = messageDTO.sender();
        message.groupId = messageDTO.groupId();
        message.content = messageDTO.content();
        message.timestamp = System.currentTimeMillis();

        return Panache.withTransaction(() ->
                message.persist()
                        .map(v -> Response.ok().build())
        );
    }

    @GET
    @Path("/messages")
    @RolesAllowed("user")
    public Uni<List<Message>> getMessages() {
        return Panache.withTransaction(() ->
                Message.listAll()
        );
    }

    record TokenResponse(String token) {}
}