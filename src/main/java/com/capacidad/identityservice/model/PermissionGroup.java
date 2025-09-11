package com.capacidad.identityservice.model;

import com.capacidad.identityservice.model.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COMA;

@Entity
@Table(name = "application_permission_group")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "application_permission_group_seq")
public class PermissionGroup extends BaseEntity<Long> implements Serializable {

    @Column(nullable = false, unique = true, updatable = false)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "resource")
    @BatchSize(size = 50)
    private Map<String, String> resourceOperations = new HashMap<>();

    @ManyToMany(mappedBy = "permissionGroups")
    private Set<ApplicationUserContext> userContexts = new HashSet<>();

    @ManyToMany(mappedBy = "permissionGroups")
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "permissionGroups")
    private Set<PermissionSuggestion> permissionSuggestions = new HashSet<>();

    public Map<String, List<Operation>> getResourceOperations() {
        return this.resourceOperations.entrySet()
                .stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), Arrays.stream(e.getValue().split(COMA))
                        .map(string -> Operation.valueOf(StringUtils.upperCase(string)))
                        .collect(Collectors.toList())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void setResourceOperations(Map<String, List<Operation>> resourceOperations) {
        this.resourceOperations = resourceOperations.entrySet()
                .stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().stream()
                        .map(operation -> StringUtils.lowerCase(operation.toString()))
                        .collect(Collectors.joining(COMA))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void setResourceOperationsAttrib(Map<String, String> resourceOperations) {
        this.resourceOperations = resourceOperations;
    }

}