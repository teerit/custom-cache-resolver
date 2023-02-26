package com.example.spring.cache.demo.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Country implements Serializable {

    private final String code;
}
