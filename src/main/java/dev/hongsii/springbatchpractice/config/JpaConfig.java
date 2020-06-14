package dev.hongsii.springbatchpractice.config;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * @see org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration
 * @see org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration
 */
@Configuration
@ConditionalOnBean(DataSourceConfig.class)
@EnableTransactionManagement
@RequiredArgsConstructor
public class JpaConfig {

    public static final String DEFAULT_ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    public static final String READ_ENTITY_MANAGER_FACTORY = "readEntityManagerFactory";

    private static final String UNIT_DEFAULT = DefaultPersistenceUnitManager.ORIGINAL_DEFAULT_PERSISTENCE_UNIT_NAME;
    private static final String UNIT_READ = "read";

    private final EntityManagerFactoryBuilder factoryBuilder;
    private final ConfigurableListableBeanFactory beanFactory;
    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;

    @Primary
    @Bean(name = DEFAULT_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        return createEntityFactoryBean(dataSource, UNIT_DEFAULT);
    }

    @Bean(name = READ_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(@Qualifier(DataSourceConfig.READ_DATA_SOURCE) DataSource readDataSource) {
        LocalContainerEntityManagerFactoryBean entityFactoryBean = createEntityFactoryBean(readDataSource, UNIT_READ);
        return entityFactoryBean;
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private LocalContainerEntityManagerFactoryBean createEntityFactoryBean(DataSource dataSource, String unitName) {
        Map<String, String> jpaProperties = getProperties(unitName);
        return factoryBuilder
                .dataSource(dataSource)
                .packages(getPackageToScan(beanFactory))
                .persistenceUnit(unitName)
                .mappingResources(getMappingResources())
                .properties(hibernateProperties.determineHibernateProperties(jpaProperties, new HibernateSettings())) // 스프링부트 2.1.0 이상
                // .properties(jpaProperties.getHibernateProperties(new HibernateSettings())) // 스프링부트 2.1.0 미만
                .build();
    }

    private String[] getPackageToScan(ConfigurableListableBeanFactory beanFactory) {
        List<String> packages = EntityScanPackages.get(beanFactory).getPackageNames();
        if (packages.isEmpty() && AutoConfigurationPackages.has(beanFactory)) {
            packages = AutoConfigurationPackages.get(beanFactory);
        }
        return StringUtils.toStringArray(packages);
    }

    private Map<String, String> getProperties(String unitName) {
        Map<String, String> jpaProperties = this.jpaProperties.getProperties();
        if (!UNIT_DEFAULT.equals(unitName)) {
            jpaProperties.put(AvailableSettings.HBM2DDL_AUTO, "none");
        }
        return jpaProperties;
    }

    private String[] getMappingResources() {
        List<String> mappingResources = jpaProperties.getMappingResources();
        return !ObjectUtils.isEmpty(mappingResources) ? StringUtils.toStringArray(mappingResources) : null;
    }
}
