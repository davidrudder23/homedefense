package org.noses.homedefense.users;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
@Slf4j
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    SecureRandom secureRandom = null;

    public AccountDTO getAccountByToken(String token) {
        Account account = accountRepository.getAccountBySessionToken(token);
        if (account == null) {
            return null;
        }
        AccountDTO accountDTO = AccountDTO.get(account);

        accountDTO.setToken(token);
        return accountDTO;
    }

    public AccountDTO login(LoginDTO loginDTO) throws RESTException {
        if (loginDTO == null) {
            throw new RESTException(400, "No login provided");
        }

        if (loginDTO.getUsername() == null) {
            throw new RESTException(400, "No username provided");
        }

        Account account = accountRepository.getAccountByUsername(loginDTO.getUsername());

        if ((account == null) || (!checkPassword(loginDTO.getPassword(), account.getHashedPassword()))) {
            // TODO: try login
            throw new RESTException(401, "Password doesn't match");
        }
        
        return registerNewSession(account);
    }

    public void updateAccount(AccountDTO accountDTO) {
        Account account = accountRepository.getAccountByUsername(accountDTO.getUsername());

        account.setHomeLatitude(accountDTO.getHomeLatitude());
        account.setHomeLongitude(accountDTO.getHomeLongitude());

        if (!StringUtils.isEmpty(accountDTO.getEmail())) {
            account.setEmail(accountDTO.getEmail());
        }

        accountRepository.save(account);
    }

    public AccountDTO register(RegisterDTO registerDTO) throws RESTException {
        if (registerDTO == null) {
            throw new RESTException(400, "No registration provided");
        }

        if (registerDTO.getUsername() == null) {
            throw new RESTException(400, "No username provided");
        }

        if (registerDTO.getUsername().length() < 4) {
            throw new RESTException(400, "Username is too short");
        }

        Account account = accountRepository.getAccountByUsername(registerDTO.getUsername());
        if (account != null) {
            // TODO: try login
            throw new RESTException(400, "Username already taken");
        }

        account = new Account();
        account.getAccountPrimaryKey().setUsername(registerDTO.getUsername());
        account.setEmail(registerDTO.getEmail());
        account.setHashedPassword(hashPassword(registerDTO.getPassword()));

        accountRepository.save(account);

        return registerNewSession(account);
    }

    public AccountDTO registerNewSession(Account account) {
        String sessionToken = generateSessionToken(account);

        AccountDTO accountDTO = AccountDTO.get(account);
        accountDTO.setToken(sessionToken);

        return accountDTO;
    }

    private String generateSessionToken(Account account) {
        byte[] tokenBytes = new byte[20];

        getSecureRandom().nextBytes(tokenBytes);
        String sessionToken = new String(HexUtils.toHexString(tokenBytes));

        AccountSession accountSession = new AccountSession();
        accountSession.setAccountPartitionId(account.getAccountPrimaryKey().getPartitionId());
        accountSession.getAuthenticationToken().setPartitionId(AccountSession.getPartitionIdFromToken(sessionToken));
        accountSession.getAuthenticationToken().setAuthenticationToken(sessionToken);
        accountSession.setUsername(account.getAccountPrimaryKey().getUsername());
        accountSession.setEmail(account.getEmail());

        accountRepository.save(accountSession);
        return sessionToken;
    }

    private SecureRandom getSecureRandom() {
        if (secureRandom == null) {
            log.info("Generating new secure random");
            secureRandom = new SecureRandom();
        }

        return secureRandom;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(5, getSecureRandom()));
    }

    private boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}