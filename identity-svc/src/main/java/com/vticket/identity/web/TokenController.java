package com.vticket.identity.web;

import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.identity.app.dto.req.IntrospectRequest;
import com.vticket.identity.app.dto.res.IntrospectResponse;
import com.vticket.identity.app.usercase.IntrospectTokenUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/identity/token")
@RequiredArgsConstructor
public class TokenController {

    private final IntrospectTokenUseCase introspectTokenUseCase;

    @PostMapping("/introspect")
    public ResponseEntity<String> introspect(@Valid @RequestBody IntrospectRequest request) {
        try {
            log.info("Token introspection request");
            IntrospectResponse response = introspectTokenUseCase.execute(request);
            return ResponseEntity.ok(ResponseJson.success("Token introspection", response));
        } catch (Exception e) {
            log.error("Token introspection failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseJson.success("Token introspection", 
                    IntrospectResponse.builder().valid(false).build()));
        }
    }
}

