package com.ateeb.enterprise_platform.domain.enums;

public enum AppPermission {
    //Platform-level (Developer) Permissions
    PLATFORM_COMPANY_CREATE,
    PLATFORM_COMPANY_READ,
    PLATFORM_COMPANY_UPDATE,

    //Tenant-level (Company ADMIN/User) Permissions
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,
    USER_ASSIGN_ROLE,

    ROLE_CREATE,
    ROLE_READ,
    ROLE_UPDATE,
    ROLE_DELETE,

    TASK_CREATE,
    TASK_READ,
    TASK_UPDATE,
    TASK_DELETE
}
