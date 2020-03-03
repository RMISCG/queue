package com.rss.queue;

import com.rss.queue.job.EchoJob;
import io.kungfury.coworker.CoworkerManager;
import io.kungfury.coworker.WorkInserter;
import io.kungfury.coworker.dbs.postgres.PgConnectionManager;
import java.time.Instant;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = {"com.rss.queue"})
@PropertySource("classpath:application.yml")
public class QueueApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueueApplication.class);
	private static final int NUMBER_OF_JOBS = 50;
	private static final String STRAND = "echo";
	private static final Integer DEFAULT_COWORKER_JOB_PRIORITY = 100;
	private static final char OLD_CHAR = ' ';
	private static final char NEW_CHAR = '0';
	private static final String CREATED_WORK_MESSAGE = "Created %s with state %s and result %s";

	@Value("${server.default-time-zone}")
	private String defaultTimeZone;

	private final PgConnectionManager pgConnectionManager;

	private final CoworkerManager coworkerManager;

	public QueueApplication(
			final PgConnectionManager pgConnectionManager,
			final CoworkerManager coworkerManager) {

		this.pgConnectionManager = pgConnectionManager;
		this.coworkerManager = coworkerManager;
	}

	@PostConstruct
	void started() {

		TimeZone.setDefault(TimeZone.getTimeZone(defaultTimeZone));

		final Instant runtAt = Instant.now();
		final int maxUniqueIdentifierLength = String.valueOf(NUMBER_OF_JOBS).length();

		for (int i=1; i<= NUMBER_OF_JOBS; i++) {

			final String workState = String.format("%1$" + maxUniqueIdentifierLength + "s", i).replace(OLD_CHAR, NEW_CHAR);

			long result = WorkInserter.INSTANCE.InsertWork(
					pgConnectionManager,
					EchoJob.class.getName(),
					workState,
					STRAND,
					runtAt,
					DEFAULT_COWORKER_JOB_PRIORITY
			);

			final String formattedResult = String.format("%1$" + maxUniqueIdentifierLength + "s", result).replace(OLD_CHAR, NEW_CHAR);

			LOGGER.info(String.format(CREATED_WORK_MESSAGE, EchoJob.class.getName(), workState, formattedResult));
		}

		coworkerManager.Start();
	}

	public static void main(String[] args) {

		new SpringApplicationBuilder(QueueApplication.class).run(args);
	}
}
