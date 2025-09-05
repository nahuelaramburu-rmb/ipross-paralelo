package com.capacidad.validationapi.prescription.galbop.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Profile({"!test"})
@Configuration
@PropertySource("classpath:galbop-${PROFILE}.properties")
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "galbopEntityManagerFactory",
        transactionManagerRef = "galbopTransactionManager",
        basePackages = "com.capacidad.validationapi.prescription.galbop")
public class GalbopDatasourceConfig {


    @Bean(name = "galbopDataSource")
    public DataSource dataSource() {
        Map<String, String> dataSourceProperties = getGalbopDataSourceProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceProperties.get("jdbcUrl"));
        config.setDriverClassName(dataSourceProperties.get("driverClassName"));
        config.setUsername(dataSourceProperties.get("username"));
        config.setPassword(dataSourceProperties.get("password"));
        config.setConnectionTimeout(Long.parseLong(dataSourceProperties.get("hikari.connection-timeout")));
        config.setMaxLifetime(Long.parseLong(dataSourceProperties.get("hikari.max-lifetime")));
        config.setMaximumPoolSize(Integer.parseInt(dataSourceProperties.get("hikari.maximum-pool-size")));
        config.setMinimumIdle(Integer.parseInt(dataSourceProperties.get("hikari.minimum-idle")));
        config.setPoolName("HikariPool-Galbop");
        return new HikariDataSource(config);
    }

    @Bean(name = "galbopDataSourceProperties")
    @ConfigurationProperties(prefix = "galbop.datasource")
    public Map<String, String> getGalbopDataSourceProperties() {
        return new HashMap<>();
    }

    @Bean(name = "galbopEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("galbopDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.capacidad.validationapi.prescription.galbop")
                .properties(getJpaProperties())
                .build();
    }

    @Bean(name = "galbopTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("galbopEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean("galbopJpaProperties")
    @ConfigurationProperties(prefix = "galbop.jpa")
    public Map<String, String> getJpaProperties() {
        return new HashMap<>();
    }

}
