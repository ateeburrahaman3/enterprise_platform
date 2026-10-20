package com.ateeb.enterprise_platform.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("tenantGuard")
public class TenantSecurityGuard {

    public static final Long SYSTEM_PLATFORM_COMPANY_ID = 1L;

    public boolean isSameTenant(Long targetCompanyId) {
        if (targetCompanyId == null) {
            return false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails currentUser)) {
            return false;
        }

        if (currentUser.getCompanyId().equals(SYSTEM_PLATFORM_COMPANY_ID)) {
            return true;
        }

        return currentUser.getCompanyId().equals(targetCompanyId);
    }
}