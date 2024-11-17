package com.example.epsnwtbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumptionMessage {
    private String id;
    private Float consumption;
    private Date timestamp;
}
