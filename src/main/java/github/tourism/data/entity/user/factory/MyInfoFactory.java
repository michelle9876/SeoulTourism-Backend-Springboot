package github.tourism.data.entity.user.factory;

import github.tourism.data.entity.user.User;
import github.tourism.web.dto.user.sign.MyPageDTO;
import org.springframework.stereotype.Component;

@Component
public class MyInfoFactory {
    public MyPageDTO createMyPageDTO(User user) {
        MyPageDTO dto = new MyPageDTO();
        dto.setEmail(user.getEmail());
        dto.setUserName(user.getUserName());
        dto.setCountry(user.getCountry());
        dto.setGender(user.getGender());
        return dto;
    }
}
