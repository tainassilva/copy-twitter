package br.com.taina.copy_twitter.dto;

//Recebe o token de acesso e a data de expiração...
public record LoginResponseDto(String acessToken, Long expiresIn) {
}
