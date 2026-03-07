package buloshnaya.authService.web;

public record ErrorResponseDto(
        String message,
        String code
) {}
