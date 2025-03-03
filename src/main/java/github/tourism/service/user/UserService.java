package github.tourism.service.user;

import github.tourism.config.security.JwtTokenProvider;
import github.tourism.data.entity.user.RefreshToken;
import github.tourism.data.entity.user.User;
import github.tourism.data.entity.user.factory.MyInfoFactory;
import github.tourism.data.repository.user.RefreshRepository;
import github.tourism.data.repository.user.RoleRepository;
import github.tourism.data.repository.user.UserRepository;
import github.tourism.service.user.security.CustomUserDetails;
import github.tourism.web.advice.ErrorCode;
import github.tourism.web.dto.user.sign.CheckedEmailRequest;
import github.tourism.web.dto.user.sign.LoginRequest;
import github.tourism.web.dto.user.sign.MyPageDTO;
import github.tourism.web.dto.user.sign.SignRequest;
import github.tourism.web.exception.BadRequestException;
import github.tourism.web.exception.DeletedUserException;
import github.tourism.web.exception.NotAcceptException;
import github.tourism.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final RefreshRepository refreshRepository;
    private final MyInfoFactory myInfoFactory;

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8;
    }
    private boolean isGenderFailure(String gender) {
        return gender.equals("M") || gender.equals("F");
    }

    private void checkUserDeleted(User user) {
        if (user.getDeletedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = user.getDeletedAt().format(formatter);
            throw new DeletedUserException(formattedDate, ErrorCode.SECESSION_DETAIL);
        }
    }

    public boolean checkedEmail(CheckedEmailRequest checkedEmailRequest){
        String email = checkedEmailRequest.getEmail();
        if (userRepository.existsByEmail(email)) {
            log.warn("이메일이 이미 존재합니다: {}", email);
            throw new BadRequestException(ErrorCode.EMAIL_ALREADY_EXIST);
        }
        return true;
    }

    private void addRefreshEntity(String email, String refresh, Long expiredMs) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiredMs);

        RefreshToken refreshEntity = new RefreshToken();
        refreshEntity.setEmail(email);
        refreshEntity.setRefresh(refresh);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        String formattedExpirationDate = sdf.format(expirationDate);
        refreshEntity.setExpiration(formattedExpirationDate);

        refreshRepository.save(refreshEntity);
    }

    @Transactional(transactionManager = "tmJpa1")
    public boolean signUp(SignRequest signUpRequest) {
        String email = signUpRequest.getEmail();
        String password = signUpRequest.getPassword();
        String username = signUpRequest.getUserName();
        String country = signUpRequest.getCountry();
        String gender = signUpRequest.getGender();

        if (userRepository.existsByEmail(email)) {
            log.warn("이메일이 이미 존재합니다: {}", email);
            throw new BadRequestException(ErrorCode.EMAIL_ALREADY_EXIST);
        }

        if (!isPasswordStrong(password)) {
            log.warn("비밀번호가 너무 약합니다.");
            throw new BadRequestException(ErrorCode.WEAK_PASSWORD);
        }

        if (!isGenderFailure(gender)) {
            log.warn("성별을 확인해주세요");
            throw new BadRequestException(ErrorCode.FAILURE_GENDER);
        }

        User userPrincipal = User.builder()
                .email(email)
                .userName(username)
                .password(passwordEncoder.encode(password))
                .country(country)
                .gender(gender)
                .role(roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_FAILURE)))
                .build();

        userRepository.save(userPrincipal);
        return true;
    }

    public Map<String, String> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmailFetchJoin(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EMAIL_NOT_EXIST));

        checkUserDeleted(user);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

            String username = userPrincipal.getUserName();

            Integer userId = userPrincipal.getUserId();

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String access = jwtTokenProvider.createToken("access", userId,email, username, roles);
            String refresh = jwtTokenProvider.createRefreshToken("refresh", email, roles);

            Long expiredMs = jwtTokenProvider.getExpirationTime(refresh);
            addRefreshEntity(email, refresh, expiredMs);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access", access);
            tokens.put("refresh", refresh);

            return tokens;

        } catch (BadCredentialsException e) {
            log.warn("로그인 실패: 잘못된 비밀번호");
            throw new BadRequestException(ErrorCode.LOGIN_FAILURE3);
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: {}", e.getMessage());
            throw new NotAcceptException(ErrorCode.LOGIN_FAILURE);
        }
    }

    @Transactional(transactionManager = "tmJpa1")
    public boolean secession(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUNDED));

        if (user.getDeletedAt() != null) {
            throw new NotAcceptException(ErrorCode.USER_SECESSION_FAILURE);
        }

        user.deleteUser();
        userRepository.save(user);

        return true;
    }

    public MyPageDTO approvalToInformation(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new NotFoundException(ErrorCode.FAILURE_MYPAGE));
        return myInfoFactory.createMyPageDTO(user);
    }

    @Transactional(transactionManager = "tmJpa1")
    public MyPageDTO modifyUserInformation(String email, MyPageDTO myPageDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FAILURE_MYPAGE));
        try {
            user.setEmail(myPageDTO.getEmail());
            user.setUserName(myPageDTO.getUserName());
            user.setCountry(myPageDTO.getCountry());
            user.setGender(myPageDTO.getGender());

            userRepository.save(user);

            return myInfoFactory.createMyPageDTO(user);
        }catch (BadRequestException e){
            throw new BadRequestException(ErrorCode.FAILURE_MYPAGE3);
        }

    }
}
