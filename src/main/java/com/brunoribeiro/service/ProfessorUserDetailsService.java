package com.brunoribeiro.service;

import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.repositories.ProfessorRepository;
import com.brunoribeiro.security.ProfessorUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfessorUserDetailsService implements UserDetailsService {

    @Autowired
    ProfessorRepository professorRepository;

    @Override
    public UserDetails loadUserByUsername(String email){
        return professorRepository.findByEmail(email)
                .map(ProfessorUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Professor não encontrado com o email: " + email));
    }
}
