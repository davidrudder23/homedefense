package org.noses.homedefense.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;

@RestController
@RequestMapping("/users")
public class AccountResource {
    @Autowired
    AccountService accountService;

    @GET
    @RequestMapping(value="/user", method=RequestMethod.GET)
    public ResponseEntity<AccountDTO> getCurrentUser(@RequestHeader("X-Authorization-Token") String authorizationToken) {
        if (StringUtils.isEmpty(authorizationToken)) {
            new ResponseEntity<String>("unauthorized", HttpStatus.UNAUTHORIZED);
        }
        AccountDTO accountDTO = accountService.getAccountByToken(authorizationToken);
        if (accountDTO == null) {
            return new ResponseEntity<AccountDTO>(new AccountDTO(), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<AccountDTO>(accountDTO, HttpStatus.OK);
    }

    @POST
    @RequestMapping(value="/user", method=RequestMethod.POST)
    public ResponseEntity<AccountDTO> setUserInfo(@RequestHeader("X-Authorization-Token") String authorizationToken,
                                                  @RequestBody AccountDTO accountDTO) {
        if (StringUtils.isEmpty(authorizationToken)) {
            new ResponseEntity<String>("unauthorized", HttpStatus.UNAUTHORIZED);
        }
        AccountDTO existingDTO = accountService.getAccountByToken(authorizationToken);
        if (existingDTO == null) {
            return new ResponseEntity<AccountDTO>(new AccountDTO(), HttpStatus.UNAUTHORIZED);
        }

        accountService.updateAccount(accountDTO);

        return new ResponseEntity<AccountDTO>(accountDTO, HttpStatus.OK);
    }

    @POST
    @RequestMapping(value="/register", method=RequestMethod.POST)
    public ResponseEntity<AccountDTO> register(@RequestBody RegisterDTO registerDTO) {

        try {
            AccountDTO accountDTO = accountService.register(registerDTO);
            return new ResponseEntity<AccountDTO>(accountDTO, HttpStatus.OK);
        } catch (RESTException e) {
            return new ResponseEntity<AccountDTO>(new AccountDTO(), HttpStatus.valueOf(e.getStatusCode()));
        }
    }

    @POST
    @RequestMapping(value="/login", method=RequestMethod.POST)
    public ResponseEntity<AccountDTO> login(@RequestBody LoginDTO loginDTO) {

        try {
            AccountDTO accountDTO = accountService.login(loginDTO);
            return new ResponseEntity<AccountDTO>(accountDTO, HttpStatus.OK);
        } catch (RESTException e) {
            return new ResponseEntity<AccountDTO>(new AccountDTO(), HttpStatus.valueOf(e.getStatusCode()));
        }
    }

}
