package com.example.hibernateinclausememoryleak.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Book {

    @Id
    private Integer id;
}
