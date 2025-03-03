package github.tourism.web.dto.user.sign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyPageDTO {
    private String email;
    private String userName;
    private String country;
    private String gender;
}
