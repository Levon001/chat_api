package com.example.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "message")
public class Message extends PanacheEntity {
    public String sender;
    public String recipient;
    public String groupId;
    public String content;
    public long timestamp;
}