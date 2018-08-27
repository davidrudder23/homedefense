package org.noses.homedefense.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    SecureRandom secureRandom = null;

    public AccountDTO getAccountByToken(String token) {
        Account account = accountRepository.getAccountBySessionToken(token);
        AccountDTO accountDTO = AccountDTO.get(account);

        accountDTO.setToken(token);
        return accountDTO;
    }

    public AccountDTO register(RegisterDTO registerDTO) throws RegistrationException {
        if (registerDTO == null) {
            throw new RegistrationException(400, "No registration provided");
        }

        if (registerDTO.getUsername() == null) {
            throw new RegistrationException(400, "No username provided");
        }

        if (registerDTO.getUsername().length() < 4) {
            throw new RegistrationException(400, "Username is too short");
        }

        Account account = accountRepository.getAccountByUsername(registerDTO.getUsername());
        if (account != null) {
            // TODO: try login
            throw new RegistrationException(400, "Username already taken");
        }

        account = new Account();
        account.getAccountPrimaryKey().setUsername(registerDTO.getUsername());
        account.getAccountPrimaryKey().setEmail(registerDTO.getEmail());
        account.setHashedPassword(hashPassword(registerDTO.getPassword()));

        accountRepository.save(account);

        return registerNewSession(account);
    }

    public AccountDTO registerNewSession(Account account) {
        String sessionToken = generateSessionToken();
        AccountSession accountSession = new AccountSession();
        accountSession.setAccountPartitionId(account.getAccountPrimaryKey().getPartitionId());
        accountSession.getToken().setPartitionId(AccountSession.getPartitionIdFromToken(sessionToken));
        accountSession.getToken().setToken(sessionToken);
        accountSession.setUsername(account.getAccountPrimaryKey().getUsername());
        accountSession.setEmail(account.getAccountPrimaryKey().getEmail());

        accountRepository.save(accountSession);

        AccountDTO accountDTO = AccountDTO.get(account);
        accountDTO.setToken(sessionToken);

        return accountDTO;
    }

    private String generateSessionToken() {
        if (secureRandom == null) {
            try {
                secureRandom = SecureRandom.getInstanceStrong();
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }

        byte[] tokenBytes = new byte[20];

        secureRandom.nextBytes(tokenBytes);
        return new String(Hex.encode(tokenBytes));
    }

    private String hashPassword (String password) {
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(20);
        return bCrypt.encode(password);
    }
}