package org.noses.homedefense.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RESTException extends Exception {

    private int statusCode;
    private String message;
}
