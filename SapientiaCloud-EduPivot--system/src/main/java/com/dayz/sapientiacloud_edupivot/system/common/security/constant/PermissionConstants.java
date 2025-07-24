package com.dayz.sapientiacloud_edupivot.system.common.security.constant;

public class PermissionConstants {

    public static final String QUERY = "query";
    public static final String ADD = "add";
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";

    public static final String USER_QUERY = "system:user:query";
    public static final String USER_ADD = "system:user:add";
    public static final String USER_EDIT = "system:user:edit";
    public static final String USER_DELETE = "system:user:delete";

    public static final String ROLE_QUERY = "system:role:query";
    public static final String ROLE_ADD = "system:role:add";
    public static final String ROLE_EDIT = "system:role:edit";
    public static final String ROLE_DELETE = "system:role:delete";

    public static final String PERMISSION_QUERY = "system:permission:query";
    public static final String PERMISSION_ADD = "system:permission:add";
    public static final String PERMISSION_EDIT = "system:permission:edit";
    public static final String PERMISSION_DELETE = "system:permission:delete";
    
    private PermissionConstants() {
        // 禁止实例化
    }
} 