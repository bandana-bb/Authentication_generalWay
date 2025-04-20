package dev.bandana.user_auth_service.dtos;


import dev.bandana.user_auth_service.models.Token;



public class LoginResponseDto {
    private String tokenValue;

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
