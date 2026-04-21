package com.eventdriven.product.adapter.out.persistance.postgres;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product")
public class ProductEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    //TODO [Reverse Engineering] generate columns from DB
}