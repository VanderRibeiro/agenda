package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {
}
