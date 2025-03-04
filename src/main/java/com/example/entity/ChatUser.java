package com.example.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import io.smallrye.mutiny.Uni;

@Entity
@Table(name = "chat_user")
public class ChatUser extends PanacheEntity {
    public String username;
    public String password;

    public static Uni<ChatUser> findByUsername(String username) {
        return find("username", username).firstResult();
    }
}