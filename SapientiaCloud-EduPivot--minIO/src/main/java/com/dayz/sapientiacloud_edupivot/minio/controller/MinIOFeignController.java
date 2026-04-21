package com.dayz.sapientiacloud_edupivot.minio.controller;

import com.dayz.sapientiacloud_edupivot.minio.result.Result;
import com.dayz.sapientiacloud_edupivot.minio.utils.MinIOUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文件内部接口", description = "供其他服务调用的文件接口")
@RestController
@RequestMapping("/file/internal")
@RequiredArgsConstructor
public class MinIOFeignController {

    private final MinIOUtil minIOUtil;

    @DeleteMapping("/delete/path")
    public Result<Boolean> deleteFileByPath(@RequestParam("filePath") String filePath) {
        return Result.success(minIOUtil.removeObjectByPath(filePath, null));
    }
}
