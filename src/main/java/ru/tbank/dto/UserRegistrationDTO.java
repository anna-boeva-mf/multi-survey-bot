package ru.tbank.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRegistrationDTO {
    private String username;
    private String password;
    private List<String> roleNames;
}
