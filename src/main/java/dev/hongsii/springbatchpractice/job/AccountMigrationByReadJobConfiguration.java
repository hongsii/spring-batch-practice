package dev.hongsii.springbatchpractice.job;

import dev.hongsii.springbatchpractice.domain.Account;
import dev.hongsii.springbatchpractice.domain.AccountHistory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

import static dev.hongsii.springbatchpractice.config.JpaConfig.DEFAULT_ENTITY_MANAGER_FACTORY;
import static dev.hongsii.springbatchpractice.config.JpaConfig.READ_ENTITY_MANAGER_FACTORY;

@Configuration
public class AccountMigrationByReadJobConfiguration {

    private static final String JOB_NAME = "AccountMigrationByReadJob";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final EntityManagerFactory readEntityManagerFactory;

    private int chunkSize;

    public AccountMigrationByReadJobConfiguration(JobBuilderFactory jobBuilderFactory,
                                                  StepBuilderFactory stepBuilderFactory,
                                                  @Qualifier(DEFAULT_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory,
                                                  @Qualifier(READ_ENTITY_MANAGER_FACTORY) EntityManagerFactory readEntityManagerFactory,
                                                  @Value("${chunkSize:1000}") int chunkSize) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.readEntityManagerFactory = readEntityManagerFactory;
        this.chunkSize = chunkSize;
    }

    @Bean(JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean(JOB_NAME + "Step")
    public Step step() {
        return stepBuilderFactory.get(JOB_NAME + "Step")
                .<Account, AccountHistory>chunk(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean(JOB_NAME + "Reader")
    public JpaPagingItemReader<Account> reader() {
        return new JpaPagingItemReaderBuilder<Account>()
                .name(JOB_NAME + "Reader")
                .entityManagerFactory(readEntityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT a FROM Account a")
                .build();
    }

    private ItemProcessor<Account, AccountHistory> processor() {
        return AccountHistory::new;
    }

    @Bean(JOB_NAME + "Writer")
    public JpaItemWriter<AccountHistory> writer() {
        return new JpaItemWriterBuilder<AccountHistory>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
