package dev.bandana.user_auth_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bandana.user_auth_service.dtos.SendEmailDto;
import dev.bandana.user_auth_service.models.Token;
import dev.bandana.user_auth_service.models.User;
import dev.bandana.user_auth_service.repositores.TokenRepository;
import dev.bandana.user_auth_service.repositores.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    //1.key-->topic ,Value->event
    private KafkaTemplate<String,String> kafkaTemplate;

    public  UserServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, TokenRepository tokenRepository, ObjectMapper objectMapper,
                            KafkaTemplate<String,String> kafkaTemplate) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Token login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            System.out.println("User not found for email: " + email);

            return null;
        }

        User user = optionalUser.get();

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            System.out.println("Invalid password for user: " + email);
            return null;
        }


        Token token = new Token();
        System.out.println("==========================");
        token.setValue(RandomStringUtils.randomAlphanumeric(128));
        token.setUser(user);

        // Set token expiry (30 days)
        LocalDate localDate = LocalDate.now().plusDays(30);
        Date expiryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        token.setExpiryAt(expiryDate);

        try {
            Token savedToken = tokenRepository.save(token);
            System.out.println("Token saved successfully: " + savedToken.getValue());
            return savedToken;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error saving token.");
            return null;
        }
    }

    @Override
    public User signup(String name, String email, String password) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        //Pushing an event to kafka which userservice will read and send welcome email to
        //the user .
        SendEmailDto sendEmailDto=new SendEmailDto();
        sendEmailDto.setSubject("Greetings for the day");
        sendEmailDto.setBody("Welcome to Scaler classes");
        sendEmailDto.setEmail(user.getEmail());

        //push and event to kafka

        try {
            kafkaTemplate.send(
                    "sendEmail",objectMapper.writeValueAsString(sendEmailDto)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return userRepository.save(user);
    }

    @Override
    public User validateToken(String tokenvalue) {
       //token value should be present in db,deleted should be false,expiry_time>current_time

        Optional<Token> optionalToken= tokenRepository.findByValueAndDeletedAndExpiryAtGreaterThan(tokenvalue,false,new Date());

        if(optionalToken.isEmpty()) {
            return null;
        }
        return optionalToken.get().getUser();
    }

    @Override
    public void logout(String tokenvalue) {
     Optional<Token> optionalToken= tokenRepository.findByValueAndDeletedAndExpiryAtGreaterThan(tokenvalue,false,new Date());
     Token token = optionalToken.get();
     token.setDeleted(true);
     tokenRepository.save(token);

    }
}
