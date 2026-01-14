package com.vticket.identity.web;

import com.google.gson.Gson;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.identity.app.dto.req.UpdateProfileRequest;
import com.vticket.identity.app.dto.res.UserResponse;
import com.vticket.identity.app.usercase.ProfileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
public class UserController {

    private final ProfileUseCase profileUseCase;
    private final Gson gson;

    @GetMapping("/profile")
    public String userProfile(@RequestHeader("X-USER-ID") String userId) {
        String prefix = "[userProfile]|userId=" + userId;
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
            log.error("{}|Exception={}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_INTERNAL, e.getMessage());
        }
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestHeader("X-USER-ID") String userId,
                                @RequestBody UpdateProfileRequest updateRequest) {
        String prefix = "[updateProfile]|userId=" + userId;
        long start = System.currentTimeMillis();
        log.info("{}|Req={}", prefix, gson.toJson(updateRequest));
        try {
            UserResponse userResponse = profileUseCase.updateUserByUId(userId, updateRequest);
            if (userResponse == null) {
                log.error("{}|Update profile failed with userId={}", prefix, userId);
                return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, "User not found");
            }
            log.info("{}|Update profile user={} success|Expire time: {}ms", prefix, userId, System.currentTimeMillis() - start);
            return ResponseJson.success("Update profile user success", userResponse);
        } catch (Exception e) {
            log.error("{}|Exception={}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_INTERNAL, e.getMessage());
        }
    }
}
