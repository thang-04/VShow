package com.vticket.identity.web;

import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.identity.app.dto.res.UserResponse;
import com.vticket.identity.app.usercase.ProfileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/identity/profile")
@RequiredArgsConstructor
public class UserController {

    private final ProfileUseCase profileUseCase;

    @GetMapping()
    public String userProfile(@RequestHeader("X-USER-ID") String userId) {
        String prefix = "[userProfile]";
        long start = System.currentTimeMillis();
        try {
            UserResponse userResponse = profileUseCase.getUserByUId(userId);
            if (userResponse == null) {
                log.error("{}|User not found with userId={}", prefix, userId);
                return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, "User not found");
            }
            log.info("{}|Get profile user={} success|Expire time: {}ms", prefix, userId, System.currentTimeMillis() - start);
            return ResponseJson.success("Get profile user success", userResponse);
        } catch (Exception e) {
            log.error("Exception={}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_INTERNAL, e.getMessage());
        }
    }
}
