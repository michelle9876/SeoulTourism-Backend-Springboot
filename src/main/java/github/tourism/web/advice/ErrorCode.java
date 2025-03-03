package github.tourism.web.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 로그인
    REGISTER_FAILURE(400,"입력하신 데이터에 문제가 있습니다.",HttpStatus.BAD_REQUEST),
    SECESSION_NOT_FOUND(404,"탈퇴한 회원이 아닙니다.",HttpStatus.NOT_FOUND),
    SECESSION_DETAIL(410," 해당 날짜에 탈퇴를 하셨습니다.",HttpStatus.FORBIDDEN),
    USER_SECESSION_FAILURE(401,"지금 상태에서 탈퇴 진행을 하실 수 없습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILURE(406,"로그인 할 수 없습니다.", HttpStatus.NOT_ACCEPTABLE),
    LOGIN_FAILURE2(406,"탈퇴를 하셔서 로그인이 불가능합니다.", HttpStatus.NOT_ACCEPTABLE),
    LOGIN_FAILURE3(400,"비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(401,"발급된 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN2(401,"Access Token이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_TOKEN(401,"인증되지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_TOKEN2(401,"Access Token이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ENTRY_POINT_FAILURE(401,"인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(403,"접근할 수 있는 권한이 없습니다.", HttpStatus.FORBIDDEN),
    EMAIL_NOT_EXIST(404,"해당 이메일의 사용자를 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXIST(400,"다른 이메일을 사용해주시길 바랍니다.",HttpStatus.BAD_REQUEST),
    WEAK_PASSWORD(400,"비밀번호는 최소 8자 이상이어야 합니다.",HttpStatus.BAD_REQUEST),
    ROLE_FAILURE(400,"사용자 권한이 없습니다.",HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_REFRESH(400,"Refresh Token이 없습니다.",HttpStatus.BAD_REQUEST),
    EXPIRED_REFRESH(400,"Refresh Token이 만료되었습니다.",HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_REFRESH2(400,"Refresh Token이 일치하지 않습니다.",HttpStatus.BAD_REQUEST),
    FAILURE_USER_NAME(400,"50자 이내의 이름을 설정해주세요.",HttpStatus.BAD_REQUEST),
    FAILURE_GENDER(400,"성별은 [M,F,U]로 설정되어야 합니다.",HttpStatus.BAD_REQUEST),
    USER_NOT_FOUNDED(404,"해당 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_ERROR_FORBIDDEN(403,"로그인 정보와 접근 정보가 다릅니다.", HttpStatus.FORBIDDEN),

    // 예외 처리가 필요한 경우 위의 코드를 참고하여 작성
    GOODS_NOT_FOUNDED(404,"해당 굿즈를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CART_NOT_FOUNDED(404,"해당 장바구니 항목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CART_EMPTY(404,"사용자의 장바구니가 비어있습니다.", HttpStatus.NOT_FOUND),
    MAPS_NOT_FOUNDED(404,"해당 맵을 찾을 수 없습니다.",HttpStatus.NOT_FOUND),

    FAILURE_MYPAGE(404,"개인 정보를 찾을 수 없습니다.",HttpStatus.BAD_REQUEST),
    FAILURE_MYPAGE2(400,"개인 정보를 불러올 수 없습니다.",HttpStatus.BAD_REQUEST),
    FAILURE_MYPAGE3(400,"에러가 발생되어 개인 정보를 수정할 수 없습니다.",HttpStatus.BAD_REQUEST),
    FAILURE_MYPAGE4(400,"개인 정보 수정 중 문제가 발생했습니다.",HttpStatus.BAD_REQUEST),
    NOT_SAVED_PLACE(400,"존재하지 않는 장소입니다.",HttpStatus.BAD_REQUEST);

    private final int statusCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;
}
