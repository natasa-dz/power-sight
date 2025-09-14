package com.example.epsnwtbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheablePage<T> implements Serializable {
    private ArrayList<T> content;
    private int totalPages;
    private long totalElements;
}
