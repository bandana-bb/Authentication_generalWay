package dev.bandana.user_auth_service.controllers;

import dev.bandana.user_auth_service.dtos.*;
import dev.bandana.user_auth_service.models.Token;
import dev.bandana.user_auth_service.models.User;
import dev.bandana.user_auth_service.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dev.bandana.user_auth_service.dtos.LoginRequestDto.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponseDto login( @RequestBody LoginRequestDto loginRequestDto) {

        Token token=userService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        LoginResponseDto loginResponseDto=new LoginResponseDto();
        loginResponseDto.setTokenValue(token.getValue());
        System.out.println("cfvgtbygcrfvjhfhf");
        System.out.println(loginRequestDto);
        return loginResponseDto;

    }

    @PostMapping("/signup")
    public UserDto signup(@RequestBody  SignupRequestDto signupRequestDto){
        User user=userService.signup(signupRequestDto.getName(),
                signupRequestDto.getEmail(),signupRequestDto.getPassword());

        //user to userdto

        return  UserDto.fromUser(user);

    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody  LogoutRequestDto logoutRequestDto){
        String tokenValue= logoutRequestDto.getTokenValue();

        if(tokenValue==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        userService.logout(tokenValue);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<UserDto> validateToken(@PathVariable("token")  String tokenValue){
        User user=userService.validateToken(tokenValue);
        ResponseEntity<UserDto> responseEntity=null;

        if(user==null){
            responseEntity=new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        else{
            responseEntity=new ResponseEntity<>(UserDto.fromUser(user), HttpStatus.OK);
        }
        return responseEntity;
    }

}
