package com.example.dto;

public record MessageDTO(
        String sender,
        String recipient,
        String groupId,
        String content
) {}
