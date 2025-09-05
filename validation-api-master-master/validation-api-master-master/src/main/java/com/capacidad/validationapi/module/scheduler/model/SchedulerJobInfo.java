package com.capacidad.validationapi.module.scheduler.model;

import com.capacidad.validationapi.module.base.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "scheduler_job_info")
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "scheduler_job_info_seq", allocationSize = 1)
public class SchedulerJobInfo extends BaseEntity<Long> {

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_group", nullable = false)
    private String jobGroup;

    @Column(name = "job_class", nullable = false)
    private String jobClass;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "repeat_time")
    private Long repeatTime;

    @Column(name = "cron_job", nullable = false)
    private Boolean cronJob;
}
