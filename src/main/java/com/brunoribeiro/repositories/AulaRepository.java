package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
}
