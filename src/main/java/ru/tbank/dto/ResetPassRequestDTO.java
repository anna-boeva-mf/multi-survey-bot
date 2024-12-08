package ru.tbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class ResetPassRequestDTO {
    private String token;
    private String newPassword;
    private String confirmPassword;

    public ResetPassRequestDTO() {
    }
}
