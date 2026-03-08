package buloshnaya.authService.services;

import buloshnaya.authService.dto.AuthResponse;
import buloshnaya.authService.dto.LoginRequest;
import buloshnaya.authService.dto.SignUpRequest;
import buloshnaya.authService.entity.RefreshTokenEntity;
import buloshnaya.authService.entity.UserEntity;
import buloshnaya.authService.model.Role;
import buloshnaya.authService.repository.RefreshTokenRepository;
import buloshnaya.authService.repository.UserRepository;
import buloshnaya.authService.web.EmailAlreadyExistsException;
import buloshnaya.authService.web.InvalidCredentialsException;
import buloshnaya.authService.web.InvalidRefreshTokenException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final long refreshExpiration;


    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
       @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshExpiration = refreshExpiration;
    }

    @Transactional
    public AuthResponse register(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already taken");
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .surname(request.getSurname())
                .role(Role.CLIENT)
                .build();

        userRepository.save(user);

        return createTokenPair(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return createTokenPair(user);
        }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (tokenEntity.isExpired()) {
            refreshTokenRepository.delete(tokenEntity);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        UserEntity user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("User not found"));

        refreshTokenRepository.delete(tokenEntity);

        return createTokenPair(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }


    private AuthResponse createTokenPair(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = UUID.randomUUID().toString();
        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();

        refreshTokenRepository.save(tokenEntity);

        return new AuthResponse(accessToken, refreshToken, user.getRole());
    }
}
