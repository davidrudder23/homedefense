package org.noses.homedefense.users;

import lombok.Data;

@Data
public class AccountDTO {

    private String email;
    private String username;
    private String token;
    private double homeLongitude;
    private double homeLatitude;

    public static AccountDTO get(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setEmail(account.getEmail());
        accountDTO.setUsername(account.getAccountPrimaryKey().getUsername());
        accountDTO.setHomeLatitude(account.getHomeLatitude());
        accountDTO.setHomeLongitude(account.getHomeLongitude());

        return accountDTO;
    }
}
