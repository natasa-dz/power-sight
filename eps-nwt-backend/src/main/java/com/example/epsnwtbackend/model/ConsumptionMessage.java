package com.example.epsnwtbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumptionMessage {
    private String id;
    private String consumption;
    private String timestamp;
}
