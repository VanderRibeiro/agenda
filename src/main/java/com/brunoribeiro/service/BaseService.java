package com.brunoribeiro.service;

import com.brunoribeiro.security.ProfessorUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public abstract class BaseService {

    protected UUID getCurrentProfessorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof ProfessorUserDetails userDetails)) {
            return null;
        }
        return userDetails.getProfessor().getId();
    }

    protected UUID requireCurrentProfessorId() {
        UUID professorId = getCurrentProfessorId();
        if (professorId == null) {
            throw new AccessDeniedException("Professor não autenticado");
        }
        return professorId;
    }
}
