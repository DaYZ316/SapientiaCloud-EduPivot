package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SelectIdentityDTO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.IdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
@Tag(name = "身份选择接口", description = "用户身份选择相关接口")
public class IdentityController {

    private final IdentityService identityService;

    @PostMapping("/select")
    @Operation(summary = "selectIdentity", description = "选择身份并创建对应的学生或教师记录")
    public Result<Boolean> selectIdentity(@Valid @RequestBody SelectIdentityDTO selectIdentityDTO) {
        Boolean result = identityService.selectIdentity(selectIdentityDTO);
        return Result.success(result);
    }
}

