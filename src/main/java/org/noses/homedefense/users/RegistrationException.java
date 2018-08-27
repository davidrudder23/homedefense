package org.noses.homedefense.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationException extends Exception {

    private int statusCode;
    private String message;
}
