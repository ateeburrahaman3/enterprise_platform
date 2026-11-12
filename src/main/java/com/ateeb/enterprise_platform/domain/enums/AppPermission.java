package com.ateeb.enterprise_platform.domain.enums;

public enum AppPermission {
    //Platform level permissions
    PLATFORM_COMPANY_CREATE,
    PLATFORM_COMPANY_READ,
    PLATFORM_COMPANY_UPDATE,

    //Tenant level permissions
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,
    USER_ASSIGN_ROLE,

    ROLE_CREATE,
    ROLE_READ,
    ROLE_UPDATE,
    ROLE_DELETE,

    // Task Management Business Permissions
    TASK_CREATE,
    TASK_READ,
    TASK_UPDATE,
    TASK_DELETE

}
