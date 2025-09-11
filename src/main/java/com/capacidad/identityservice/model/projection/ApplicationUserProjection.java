package com.capacidad.identityservice.model.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ApplicationUserProjection {

    String getUsername();

    LocalDateTime getCreatedAt();

    StateProjection getState();

    String getEmail();

    ProfileProjection getProfile();

    Boolean getEmailVerified();

    UUID getSub();

    UUID getResourceId();

}
