package dev.hongsii.springbatchpractice.infra.datasource;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    public static final String READ = "read";
    public static final String WRITE = "write";

    @Builder
    public RoutingDataSource(DataSource read, DataSource write) {
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(READ, read);
        dataSources.put(WRITE, write);
        super.setTargetDataSources(dataSources);
        super.setDefaultTargetDataSource(write);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? READ : WRITE;
        if (log.isDebugEnabled()) {
            log.debug("Current datasource: {}", dataSource);
        }
        return dataSource;
    }
}