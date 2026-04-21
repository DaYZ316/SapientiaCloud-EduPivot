package com.dayz.sapientiacloud_edupivot.live.common.clients;

import com.dayz.sapientiacloud_edupivot.live.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        contextId = "MinIOFileClient",
        value = "SapientiaCloud-EduPivot--minIO",
        path = "/file",
        configuration = FeignConfig.class
)
public interface MinIOFileClient {

    @DeleteMapping("/internal/delete/path")
    Result<Boolean> deleteFileByPath(@RequestParam("filePath") String filePath);
}
