package org.noses.homedefense.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;

@RestController
@RequestMapping("/users")
public class AccountResource {
    @Autowired
    AccountService accountService;

    @GET
    @RequestMapping("/user")
    public AccountDTO getCurrentUser(String token) {

        return accountService.getAccountByToken(token);
    }

    @POST
    @RequestMapping("/register/")
    public AccountDTO register(@RequestParam RegisterDTO registerDTO) {
        return null;
    }

}
