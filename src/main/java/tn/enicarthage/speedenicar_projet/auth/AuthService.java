package tn.enicarthage.speedenicar_projet.auth;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.speedenicar_projet.auth.dto.AuthResponse;
import tn.enicarthage.speedenicar_projet.auth.dto.LoginRequest;
import tn.enicarthage.speedenicar_projet.auth.dto.RefreshTokenRequest;
import tn.enicarthage.speedenicar_projet.auth.dto.RegisterRequest;
import tn.enicarthage.speedenicar_projet.common.enums.Role;
import tn.enicarthage.speedenicar_projet.common.exception.BusinessException;
import tn.enicarthage.speedenicar_projet.psychologist.entity.PsychologistProfile;
import tn.enicarthage.speedenicar_projet.psychologist.repository.PsychologistProfileRepository;
import tn.enicarthage.speedenicar_projet.student.entity.StudentProfile;
import tn.enicarthage.speedenicar_projet.student.repository.StudentProfileRepository;
import tn.enicarthage.speedenicar_projet.user.entity.User;
import tn.enicarthage.speedenicar_projet.user.repository.UserRepository;
import tn.enicarthage.speedenicar_projet.security.jwt.JwtUtils;
import java.time.LocalDateTime;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepo;
    private final StudentProfileRepository studentProfileRepo;
    private final PsychologistProfileRepository psychologistProfileRepo;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;



    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(("Identifiants invalides")));
        if (!user.getEnabled()) {
            throw new BusinessException("Ce compte a été désactivé");
        }
        user.setLastLogin(LocalDateTime.now());
        userRepo.save(user);

        String accessToken = jwtUtils.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail()
        );

        log.info("Login réussi pour {}", user.getEmail());

        return AuthResponse.of(accessToken, refreshToken, user.getId(),
                user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte avec cet email existe déjà");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepo.save(user);

        // Create role-specific profile
        if (request.getRole() == Role.STUDENT) {
            if (request.getStudentId() == null || request.getStudentId().isBlank()) {
                throw new BusinessException("Le numéro étudiant est obligatoire");
            }
            StudentProfile profile = StudentProfile.builder()
                    .user(savedUser)
                    .studentId(request.getStudentId())
                    .department(request.getDepartment())
                    .level(request.getLevel())
                    .build();
            studentProfileRepo.save(profile);
        } else if (request.getRole() == Role.PSYCHOLOGIST) {
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new BusinessException("Le numéro de licence est obligatoire");
            }
            PsychologistProfile profile = PsychologistProfile.builder()
                    .user(savedUser)
                    .licenseNumber(request.getLicenseNumber())
                    .specialization(request.getSpecialization())
                    .build();
            psychologistProfileRepo.save(profile);
        }

        String accessToken = jwtUtils.generateAccessToken(
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getEmail());

        log.info("Inscription réussie pour {} ({})", savedUser.getEmail(), savedUser.getRole());

        return AuthResponse.of(accessToken, refreshToken, savedUser.getId(),
                savedUser.getEmail(), savedUser.getFirstName(),
                savedUser.getLastName(), savedUser.getRole());
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException("Refresh token invalide ou expiré");
        }

        String email = jwtUtils.extractEmail(token);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        String newAccessToken = jwtUtils.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        return AuthResponse.of(newAccessToken, newRefreshToken, user.getId(),
                user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole());
    }
}

