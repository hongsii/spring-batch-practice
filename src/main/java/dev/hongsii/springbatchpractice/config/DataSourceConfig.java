package dev.hongsii.springbatchpractice.config;

import com.zaxxer.hikari.HikariDataSource;
import dev.hongsii.springbatchpractice.infra.datasource.RoutingDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

/**
 * Should exlucde {@link DataSourceAutoConfiguration}
 *
 * <pre class="code">
 *     @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
 * </pre>
 */
@Configuration
public class DataSourceConfig {

    public static final String READ_DATA_SOURCE = "readDataSource";
    public static final String WRITE_DATA_SOURCE = "writeDataSource";


    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.hikari.read")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "custom.datasource.hikari.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier(READ_DATA_SOURCE) DataSource readDataSource,
                                 @Qualifier(WRITE_DATA_SOURCE) DataSource writeDataSource) {
        RoutingDataSource routingDataSource = RoutingDataSource.builder()
                .read(readDataSource)
                .write(writeDataSource)
                .build();
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
