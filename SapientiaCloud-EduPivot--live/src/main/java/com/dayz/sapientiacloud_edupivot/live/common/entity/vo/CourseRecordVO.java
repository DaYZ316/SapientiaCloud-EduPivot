package com.dayz.sapientiacloud_edupivot.live.common.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CourseRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6373015138476717831L;

    private UUID id;

    private UUID courseId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime overTime;
}
