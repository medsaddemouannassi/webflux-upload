package com.talan.test.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table
public class Product {
    @Id
    private Long id;
    @Column
    private String name;
    @Column
    private double price;
    @Column
    private String image;
}
