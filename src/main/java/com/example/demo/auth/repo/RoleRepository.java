package com.example.demo.auth.repo;

import com.example.demo.auth.entity.Role;
import com.example.demo.auth.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleName name);
}