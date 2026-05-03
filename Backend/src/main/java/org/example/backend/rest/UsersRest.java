package org.example.backend.rest;

import java.util.Map;
import java.util.Optional;
import org.example.backend.entity.User;
import org.example.backend.service.RolesService;
import org.example.backend.service.SecurityService;
import org.example.backend.service.UsersService;
import org.example.backend.validators.SignUpFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth")
public class UsersRest {

    @Autowired
    private UsersService usersService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SignUpFormValidator signUpFormValidator;

    @Autowired
    private RolesService rolesService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        try {
            securityService.autoLogin(email, password);
            User user = usersService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return ResponseEntity.ok(Map.of(
                    "message", "Login exitoso",
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio"));
        }
        if (usersService.getUserByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya está registrado"));
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
        }
        if (!user.getPassword().equals(user.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));
        }

        user.setRole(rolesService.getDefaultRole());
        usersService.addUser(user);

        return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@RequestParam String email) {
        User user = usersService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of(
                "name", user.getName(),
                "lastName", user.getLastName(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "gender", user.getGender() != null ? user.getGender() : "",
                "birthDate", user.getBirthDate() != null ? user.getBirthDate() : "",
                "role", user.getRole()
        ));
    }
}