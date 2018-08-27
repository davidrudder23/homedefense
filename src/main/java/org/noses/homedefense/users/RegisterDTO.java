package org.noses.homedefense.users;

import lombok.Data;

@Data
public class RegisterDTO {

    private String username;

    private String email;

    private String password;
}
