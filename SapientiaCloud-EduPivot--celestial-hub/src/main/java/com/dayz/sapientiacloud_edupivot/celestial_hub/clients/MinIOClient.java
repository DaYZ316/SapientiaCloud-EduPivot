package com.dayz.sapientiacloud_edupivot.celestial_hub.clients;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient(contextId = "MinIOClient", value = "SapientiaCloud-EduPivot--minIO", path = "/file", configuration = FeignConfig.class)
public interface MinIOClient {

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<Map<String, String>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "directory", required = false) String directory,
            @RequestParam(value = "bucketCode", required = false) String bucketCode
    );

    @GetMapping("/url")
    Result<String> getFileUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expiry", required = false) Integer expiry,
            @RequestParam(value = "bucketCode", required = false) String bucketCode
    );

    @DeleteMapping("/delete")
    Result<Boolean> deleteFile(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "bucketCode", required = false) String bucketCode
    );

    @GetMapping("/download/bytes")
    Result<byte[]> downloadFile(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "bucketCode", required = false) String bucketCode
    );
}

