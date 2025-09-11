package com.capacidad.identityservice.repository;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.projection.ApplicationUserProjection;
import com.capacidad.identityservice.repository.base.ExtendedRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ApplicationUserRepository extends ExtendedRepository<ApplicationUser, Long> {

    Optional<ApplicationUser> findByUsername(String username);

    Optional<ApplicationUserProjection> findProjectedByUsername(String username);

    Optional<ApplicationUser> findBySub(UUID sub);

    @Query(value = "select u from ApplicationUser u " +
            "join fetch u.profile " +
            "join fetch u.state " +
            "where u.email = ?1")
    Optional<ApplicationUser> findByEmail(String email);

    Optional<ApplicationUser> findByUsernameOrEmail(String username, String email);

    boolean existsByResourceId(UUID resourceId);

    default Optional<ApplicationUser> findByIdAndOrResourceId(long id, UUID resourceId) {
        if (resourceId == null)
            return findByIdAndResourceIdIsNull(id);
        return findByIdAndResourceId(id, resourceId);
    }

    @Query(value = "select u from ApplicationUser u " +
            "join fetch u.profile " +
            "join fetch u.state " +
            "where u.id = ?1 and u.resourceId = ?2")
    Optional<ApplicationUser> findByIdAndResourceId(long id, UUID resourceId);

    @Query(value = "select u from ApplicationUser u " +
            "join fetch u.profile " +
            "join fetch u.state " +
            "where u.id = ?1 and u.resourceId is null")
    Optional<ApplicationUser> findByIdAndResourceIdIsNull(long id);

    @Query(value = "select refresh_token from oauth_access_token where user_name like ?1", nativeQuery = true)
    Set<String> getUserRefreshToken(String username);

    @Modifying
    @Query(value = "delete from oauth_access_token where user_name like ?1", nativeQuery = true)
    void clearUserAccessToken(String username);

    @Modifying
    @Query(value = "delete from oauth_refresh_token where token_id like ?1", nativeQuery = true)
    void clearUserRefreshToken(String refreshToken);

}
