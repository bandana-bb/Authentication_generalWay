package dev.bandana.user_auth_service.dtos;

public class LogoutRequestDto {
    private String tokenValue;

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
