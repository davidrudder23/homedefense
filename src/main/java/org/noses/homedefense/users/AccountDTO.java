package org.noses.homedefense.users;

import lombok.Data;

@Data
public class AccountDTO {

    private String email;
    private String username;
    private String token;

    public static AccountDTO get(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setEmail(account.getEmail());
        accountDTO.setUsername(account.getAccountPrimaryKey().getUsername());

        return accountDTO;
    }
}
