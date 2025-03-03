package github.tourism.web.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.tourism.config.util.JwtAuthorization;
import github.tourism.service.user.UserService;
import github.tourism.web.advice.ApiResponse;
import github.tourism.web.advice.ErrorCode;
import github.tourism.web.dto.user.UserInfoDTO;
import github.tourism.web.dto.user.sign.*;
import github.tourism.web.exception.BadRequestException;
import github.tourism.web.exception.CustomValidationException;
import github.tourism.web.exception.NotAcceptException;
import github.tourism.web.exception.NotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService authService;

    @PostMapping(value = "/email")
    public ResponseEntity<ApiResponse<CheckedEmailResponse>> checkEmail(
            @Valid @RequestBody CheckedEmailRequest emailCheckRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new CustomValidationException(ErrorCode.REGISTER_FAILURE);
        }

        try {
            boolean isEmailAvailable = authService.checkedEmail(emailCheckRequest);
            CheckedEmailResponse response = new CheckedEmailResponse(
                    isEmailAvailable,
                    "사용 가능한 이메일입니다."
            );
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (BadRequestException ex) {
            CheckedEmailResponse response = new CheckedEmailResponse(
                    false,
                    "이미 존재하는 이메일입니다."
            );
            return ResponseEntity.ok
                    (ApiResponse.onFailure(
                            HttpStatus.OK.value(),
                            ex.getMessage(),
                            response
                    ));
        }
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<ApiResponse<SignResponse>> register(@Valid @RequestBody String signUpRequestJson) {
        SignRequest signUpRequest;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            signUpRequest = objectMapper.readValue(signUpRequestJson, SignRequest.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(ApiResponse.onFailure(400, "잘못된 요청 형식입니다.", null));
        }

        boolean isSuccess = authService.signUp(signUpRequest);
        SignResponse response = new SignResponse(isSuccess, isSuccess ? "회원가입 성공하였습니다." : "회원가입 실패하였습니다.");
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request,HttpServletResponse httpServletResponse, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new CustomValidationException(ErrorCode.REGISTER_FAILURE);
        }

        Map<String, String> tokens = authService.login(loginRequest);
        httpServletResponse.setHeader("Authorization", "Bearer " + tokens.get("access"));

        Cookie refreshCookie = createCookie("refresh", tokens.get("refresh"),request);
        httpServletResponse.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.onSuccess(tokens));
    }

    @GetMapping(value = "/delete")
    public ResponseEntity<ApiResponse<DeletedUserResponse>> secession(@JwtAuthorization UserInfoDTO userInfo) {
        try {
            String email = userInfo.getEmail();

            boolean successSecession = authService.secession(email);
            DeletedUserResponse response = new DeletedUserResponse(successSecession, "회원 탈퇴 과정이 완료되었습니다.");
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (NotAcceptException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.onFailure(ErrorCode.USER_SECESSION_FAILURE.getStatusCode(), ErrorCode.USER_SECESSION_FAILURE.getErrorMessage(), null));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.onFailure(ErrorCode.USER_NOT_FOUNDED.getStatusCode(), ErrorCode.USER_NOT_FOUNDED.getErrorMessage(), null));
        }
    }

    @GetMapping("/myinfo")
    public ResponseEntity<ApiResponse<MyPageDTO>> getUserInformation(@JwtAuthorization UserInfoDTO userInfo){
        try{
            String email = userInfo.getEmail();

            MyPageDTO approvalSuccessful = authService.approvalToInformation(email);
            return ResponseEntity.ok(ApiResponse.onSuccess(approvalSuccessful));
        }catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.onFailure(ErrorCode.FAILURE_MYPAGE2.getStatusCode(),ErrorCode.FAILURE_MYPAGE2.getErrorMessage(),null));
        }
    }

    @PutMapping("/modify")
    public ResponseEntity<ApiResponse<MyPageDTO>> modifyUserInformation(@JwtAuthorization UserInfoDTO userInfo, @RequestBody MyPageDTO myPageDTO){
        try {
            String email = userInfo.getEmail();

            MyPageDTO updateInfo = authService.modifyUserInformation(email,myPageDTO);
            return ResponseEntity.ok(ApiResponse.onSuccess(updateInfo));
        }catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.onFailure(ErrorCode.FAILURE_MYPAGE4.getStatusCode(),ErrorCode.FAILURE_MYPAGE4.getErrorMessage(),null));
        }
    }

    private Cookie createCookie(String key, String value, HttpServletRequest request) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(3 * 60 * 60); // 쿠키의 유효 기간을 설정
        cookie.setPath("/"); // 쿠키가 유효한 경로를 설정
        cookie.setHttpOnly(true); // 쿠키를 HTTP 전용으로 설정
//        String origin = request.getHeader("Origin");
//        if (origin != null && origin.contains("localhost")) {
//            cookie.setSecure(false); // 로컬에서는 Secure 속성 제거
//            cookie.setAttribute("SameSite", "Lax");
//        } else {
//            cookie.setSecure(true); // 배포 환경에서는 Secure 적용
//            cookie.setAttribute("SameSite", "None");
//            cookie.setDomain("seoultourismweb.vercel.app");
//        }
        cookie.setAttribute("SameSite", "None");
        String domain = "seoultourism.store"; //"seoultourismweb.vercel.app";
        if (request.getServerName().equals("localhost")) {
            domain = "localhost";
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        cookie.setSecure(!"localhost".equals(domain));

        return cookie;
    }
}
