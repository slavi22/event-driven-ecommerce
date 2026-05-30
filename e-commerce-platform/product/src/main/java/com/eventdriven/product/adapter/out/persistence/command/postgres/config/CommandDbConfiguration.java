package com.eventdriven.product.adapter.out.persistence.command.postgres.config;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.eventdriven.product.adapter.out.persistence.command.postgres",
        entityManagerFactoryRef = "commandEntityManagerFactory",
        transactionManagerRef = "commandTransactionManager"
)
class CommandDbConfiguration {

    @Primary
    @Bean(name = "commandDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.command")
    public DataSource commandDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.command.liquibase")
    public LiquibaseProperties commandLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public SpringLiquibase commandLiquibase(@Qualifier("commandLiquibaseProperties") LiquibaseProperties properties) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(commandDataSource());
        springLiquibase.setChangeLog(properties.getChangeLog());
        springLiquibase.setContexts(StringUtils.collectionToCommaDelimitedString(properties.getContexts()));
        springLiquibase.setDefaultSchema(properties.getDefaultSchema());
        springLiquibase.setDropFirst(properties.isDropFirst());
        springLiquibase.setShouldRun(properties.isEnabled());
        springLiquibase.setLabelFilter(StringUtils.collectionToCommaDelimitedString(properties.getLabelFilter()));
        springLiquibase.setChangeLogParameters(properties.getParameters());
        springLiquibase.setRollbackFile(properties.getRollbackFile());

        return springLiquibase;
    }

    @Primary
    @Bean(name = "commandEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean commandEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                              @Qualifier("commandDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.eventdriven.product.adapter.out.persistence.command.postgres")
                .persistenceUnit("command")
                .build();
    }

    @Primary
    @Bean(name = "commandTransactionManager")
    public PlatformTransactionManager commandTransactionManager(
            @Qualifier("commandEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
