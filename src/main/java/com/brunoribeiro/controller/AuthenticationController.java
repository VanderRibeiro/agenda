package com.brunoribeiro.controller;

import com.brunoribeiro.config.TokenService;
import com.brunoribeiro.dto.LoginResponseDTO;
import com.brunoribeiro.dto.request.LoginRequestDTO;
import com.brunoribeiro.dto.request.RegisterRequestDTO;
import com.brunoribeiro.entities.Professor;
import com.brunoribeiro.repositories.ProfessorRepository;
import com.brunoribeiro.security.ProfessorUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(
                data.login(),
                data.password()
        );
        var auth = authenticationManager.authenticate(usernamePassword);
        var userDetails = (ProfessorUserDetails) auth.getPrincipal();
        var token = tokenService.gerarToken(userDetails.getProfessor());

        return ResponseEntity.ok(new LoginResponseDTO(token));

    }

    //Se for criar ROLES será necessário criar um registerDTO adicionando role como parâmetro.
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterRequestDTO data) {
        if (this.professorRepository.findByEmail(data.login()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        Professor newProfessor = new Professor(data.nome(), data.login(), encryptedPassword);

        this.professorRepository.save(newProfessor);

        return ResponseEntity.ok().build();
    }
}
