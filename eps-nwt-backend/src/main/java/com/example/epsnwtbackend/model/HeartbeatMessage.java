package com.example.epsnwtbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatMessage {
    private String id;
    private String status;
    private String timestamp;
}
