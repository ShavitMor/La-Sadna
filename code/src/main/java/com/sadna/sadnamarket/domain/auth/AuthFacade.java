package com.sadna.sadnamarket.domain.auth;
import com.sadna.sadnamarket.service.Error;
import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sadna.sadnamarket.domain.users.UserFacade;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;

public class AuthFacade {

    private TokenService tokenService;
    private IAuthRepository iAuthRepository;
    private UserFacade userFacade;
    
    private static final Logger logger = LogManager.getLogger(AuthFacade.class);

    public AuthFacade(IAuthRepository iAuthRepository, UserFacade userFacade) {
        this.iAuthRepository=iAuthRepository;
        tokenService=new TokenService();
        this.userFacade=userFacade;
    }
    
    public String login(String username, String password) {
        logger.info("start-Login. username: {} ", username);
        String token = auth(username,password);
        userFacade.login(username, password);
        logger.info("end-Login. returnedValue:"+ token);
        return token;
    
    }
    public String login(String username, String password, int guestId) {
        // If the user is authenticated, generate a JWT token for the user
        logger.info("start-Login. username: {} from guest {} ", username, guestId);
        String token = auth(username,password);
        userFacade.login(username, password,guestId);
        logger.info("end-Login. returnedValue:"+ token);
        return token;
    
    }
    private String auth(String username, String password){
        logger.info("start-auth. username: {} ", username);
        iAuthRepository.login(username,password); 
        // If the user is authenticated, generate a JWT token for the user
        String token = tokenService.generateToken(username);
        logger.info("end-auth. token:{}",token);
        return token;
    }
    public String login(String jwt) {
        if(!tokenService.validateToken(jwt)){
            throw new IllegalArgumentException(Error.makeAuthInvalidJWTError());
        }
        else 
            return tokenService.extractUsername(jwt);

    }

    public void register(String username, String password,String firstName, String lastName,String emailAddress,String phoneNumber,LocalDate birthDate){
        logger.info("start-register. username: {} first name={}, last name={}, email={} phone number={}, birthday={}", username,firstName,lastName,emailAddress,phoneNumber,birthDate);
        iAuthRepository.add(username, password);
        userFacade.register(username, firstName, lastName, emailAddress, phoneNumber,birthDate);
        logger.info("end-register.");

    }

    public void clear(){
        iAuthRepository.clear();
    }


}