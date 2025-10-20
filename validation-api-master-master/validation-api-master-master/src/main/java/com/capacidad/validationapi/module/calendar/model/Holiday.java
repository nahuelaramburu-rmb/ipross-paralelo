package com.capacidad.validationapi.module.calendar.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "deleted", "deletion_token", "tenant_id"}),
})
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "holiday_seq", allocationSize = 1)
@Relation(collectionRelation = "holidays")
public class Holiday extends BaseTenantEntity<Long> {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_event_id", nullable = false)
    private CalendarEvent event;

}
