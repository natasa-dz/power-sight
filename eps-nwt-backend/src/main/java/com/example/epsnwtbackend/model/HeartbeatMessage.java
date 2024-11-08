package com.example.epsnwtbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeartbeatMessage {
    private String id;
    private String status;
    private Date timestamp;
}
