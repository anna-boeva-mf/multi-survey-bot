package ru.tbank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCreateDTO {
    private String username;
    private String tgFirstname;
    private String tgLastname;
    private String tgUsername;
    private String password;

    public UserCreateDTO(String username, String tgFirstname, String tgLastname, String tgUsername) {
        this.username = username;
        this.tgFirstname = tgFirstname;
        this.tgLastname = tgLastname;
        this.tgUsername = tgUsername;
    }
}
