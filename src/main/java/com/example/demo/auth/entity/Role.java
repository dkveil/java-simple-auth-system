package com.example.demo.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // SERIAL
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, unique = true, nullable = false)
    private RoleName name;
}