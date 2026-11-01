package com.brunoribeiro.controller;

import com.brunoribeiro.config.TokenService;
import com.brunoribeiro.dto.AuthenticationDTO;
import com.brunoribeiro.dto.LoginResponseDTO;
import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.repositories.ProfessorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(
                data.login(),
                data.password()
        );
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.gerarToken((Professor) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    //Se for criar ROLES será necessário criar um registerDTO adicionando role como parâmetro.
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid AuthenticationDTO data) {
        if(this.professorRepository.findByEmail(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        Professor newProfessor = new Professor(data.nome(), data.login(), encryptedPassword);

        this.professorRepository.save(newProfessor);

        return ResponseEntity.ok().build();
    }
}
