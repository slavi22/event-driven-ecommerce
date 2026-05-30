package com.eventdriven.product.adapter.out.persistence.query.postgres.config;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.eventdriven.product.adapter.out.persistence.query.postgres",
        entityManagerFactoryRef = "queryEntityManagerFactory",
        transactionManagerRef = "queryTransactionManager"
)
class QueryDbConfiguration {

    @Bean(name = "queryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.query")
    public DataSource queryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.query.liquibase")
    public LiquibaseProperties queryLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public SpringLiquibase queryLiquibase(@Qualifier("queryLiquibaseProperties") LiquibaseProperties properties) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(queryDataSource());
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

    @Bean(name = "queryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean queryEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                            @Qualifier("queryDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.eventdriven.product.adapter.out.persistence.query.postgres")
                .persistenceUnit("query")
                .build();
    }

    @Bean(name = "queryTransactionManager")
    public PlatformTransactionManager queryTransactionManager(@Qualifier("queryEntityManagerFactory")
                                                              EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
