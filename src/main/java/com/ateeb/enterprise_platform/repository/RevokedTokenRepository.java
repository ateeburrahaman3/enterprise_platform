
package com.ateeb.enterprise_platform.repository;

import com.ateeb.enterprise_platform.domain.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, Long> {

    boolean existsByTokenId(String tokenId);

    void deleteAllByExpiresAtBefore(Instant currentTime);
}
