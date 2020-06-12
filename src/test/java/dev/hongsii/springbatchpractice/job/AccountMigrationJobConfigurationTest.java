package dev.hongsii.springbatchpractice.job;

import dev.hongsii.springbatchpractice.BatchTestConfig;
import dev.hongsii.springbatchpractice.domain.Account;
import dev.hongsii.springbatchpractice.domain.AccountHistoryRepository;
import dev.hongsii.springbatchpractice.domain.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BatchTestConfig.class, AccountMigrationJobConfiguration.class})
@SpringBatchTest
class AccountMigrationJobConfigurationTest {

    /**
     * {@link SpringBatchTest} creates a bean of {@link JobLauncherTestUtils}
     * idea shows "Could not autowire. No beans of 'JobLauncherTestUtils' type found." but, it can ignore
     * If you want to remove it, you should declare a bean for {@link JobLauncherTestUtils}
     * <p>
     * Or disable inspection using the below code
     * <pre class="code">
     *     @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
     *     @Autowired
     *     private JobLauncherTestUtils jobLauncherTestUtils;
     * </pre>
     *
     * @see org.springframework.batch.test.context.BatchTestContextCustomizerFactory
     */
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountHistoryRepository accountHistoryRepository;

    @Test
    @DisplayName("Account의 History를 생성할 수 있다")
    void jobTest() throws Exception {
        // given
        int totalCount = 10;
        for (int i = 0; i < totalCount; i++) {
            accountRepository.save(new Account("email" + i, "password" + i));
        }

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(accountHistoryRepository.findAll()).hasSize(totalCount);
    }
}