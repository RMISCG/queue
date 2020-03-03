package com.rss.queue.config;

import com.zaxxer.hikari.HikariConfig;
import io.kungfury.coworker.CoworkerManager;
import io.kungfury.coworker.StaticCoworkerConfigurationInput;
import io.kungfury.coworker.consul.ServiceChecker;
import io.kungfury.coworker.dbs.postgres.PgConnectionManager;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoworkerConfiguration {

  private static final Long TIMEOUT = null;
  private static final MeterRegistry METER_REGISTRY = null;
  private static final Duration CHECK_WORK_EVERY = Duration.parse("PT1M");
  private static final Map<String, Integer> N_STRAND = new HashMap<>();
  private static final short FAILURE_LIMIT = 3;
  private static final int GARBAGE_HEAP_SIZE = 1000;
  private static final Duration CLEANUP_DURATION = Duration.ofSeconds(30);
  private static final int THREADS = 5;
  private static final ServiceChecker SERVICE_CHECKER = null;


  @Bean("coworkerConnectionManager")
  PgConnectionManager getConnectionManager(
      @Value("${spring.datasource.url}") final String jdbcUrl,
      @Value("${spring.datasource.username}") final String username,
      @Value("${spring.datasource.password}") final String password) {

    return new PgConnectionManager(
        (Function<HikariConfig, HikariConfig>) (hikariConfig -> {
          hikariConfig.setJdbcUrl(jdbcUrl);
          hikariConfig.setUsername(username);
          hikariConfig.setPassword(password);
          return hikariConfig;
        }),
        TIMEOUT,
        METER_REGISTRY
    );
  }

  @Bean("coworkerStaticConfiguration")
  StaticCoworkerConfigurationInput getStaticConfiguration() {

    return new StaticCoworkerConfigurationInput(
        CHECK_WORK_EVERY,
        N_STRAND,
        FAILURE_LIMIT,
        GARBAGE_HEAP_SIZE,
        CLEANUP_DURATION
    );
  }

  @Bean("coworkerManager")
  CoworkerManager getCoworkerInstance(
      @Qualifier("coworkerConnectionManager") final PgConnectionManager coworkerConnectionManager,
      @Qualifier("coworkerStaticConfiguration") final StaticCoworkerConfigurationInput coworkerStaticConfiguration) {

    return new CoworkerManager(
        coworkerConnectionManager,
        THREADS,
        SERVICE_CHECKER,
        METER_REGISTRY,
        coworkerStaticConfiguration
    );
  }
}
