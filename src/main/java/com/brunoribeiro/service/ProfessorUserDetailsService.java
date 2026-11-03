package com.brunoribeiro.service;

import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.repositories.ProfessorRepository;
import com.brunoribeiro.security.ProfessorUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProfessorUserDetailsService implements UserDetailsService {

    private final ProfessorRepository professorRepository;

    public ProfessorUserDetailsService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return professorRepository.findByEmail(email)
                .map(ProfessorUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Professor não encontrado: " + email));
    }

}
