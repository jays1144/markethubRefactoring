package org.hanghae.markethub.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hanghae.markethub.domain.user.dto.UserRequestDto;
import org.hanghae.markethub.domain.user.dto.UserResponseDto;
import org.hanghae.markethub.global.security.impl.UserDetailsImpl;
import org.hanghae.markethub.domain.user.service.UserService;
import org.hanghae.markethub.global.constant.SuccessMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId) {
        UserResponseDto userResponseDto = userService.getUser(userId);
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/user")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> userResponseDtoList = userService.getAllUsers();
        return ResponseEntity.ok(userResponseDtoList);
    }

    @GetMapping("/user/checkEmail")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean emailExists = userService.checkEmailExists(email);
        return ResponseEntity.ok().body(emailExists);
    }


    @PatchMapping("/user/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId, @RequestBody UserRequestDto requestDto) {
        UserResponseDto userResponseDto = userService.updateUser(userId, requestDto);
        return ResponseEntity.ok(userResponseDto);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(SuccessMessage.DELETE_SUCCESS_MESSAGE.getSuccessMessage(), HttpStatus.CREATED);
    }

    @GetMapping("/user/loginFormPage")
    public String loginPage(@RequestParam(required = false) String url, Model model) {
        if (url == null) {
            model.addAttribute("url", "/");
        }
        model.addAttribute("url", url);
        return "login";
    }


    @GetMapping("/user/signupPage")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/user/mypagePage")
    public String myPage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        UserResponseDto userResponseDto = userService.getUser(userDetails.getUser().getId());
        model.addAttribute("user", userResponseDto);
        return "myPage";
    }

    @GetMapping("/user/errorPage")
    public String error() {
        return "error";
    }

    @PostMapping("/user/signup")
    public String signup(@RequestBody UserRequestDto userRequestDto) {
        try {
            userService.createUser(userRequestDto);
        } catch (IllegalArgumentException e) {
            // 예외 처리 코드
            return "redirect:/api/user/errorPage"; // 예외 발생 시 리다이렉션
        }
        return "redirect:/api/user/loginFormPage"; // 정상적인 경우 리다이렉션
    }
}
