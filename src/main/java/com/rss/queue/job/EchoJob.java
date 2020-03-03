package com.rss.queue.job;

import io.kungfury.coworker.BackgroundJavaWork;
import io.kungfury.coworker.WorkGarbage;
import io.kungfury.coworker.dbs.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoJob extends BackgroundJavaWork {

  private static final Logger LOGGER = LoggerFactory.getLogger(EchoJob.class);
  private static final String PROCESSING_WORK_MESSAGE = "Running %s with state %s";
  private static final String ERROR_PROCESSING_WORK_MESSAGE = "Error running %s with state %s. Error message: %s";
  private static final String ERROR_MARKING_FAIL_WORK_MESSAGE = "Error marking failed %s with state %s. Error message: %s";

  private final ConnectionManager connectionManager;

  public EchoJob(
      final ConnectionManager connectionManager,
      final WorkGarbage workGarbage,
      final long id,
      final int stage,
      final String strand,
      final int priority) {

    super(workGarbage, id, stage, strand, priority);

    this.connectionManager = connectionManager;
  }

  @Override
  public String getSerializedState() {

    return "";
  }

  @Override
  public void Work(final String state) {

    try {

      LOGGER.info(String.format(PROCESSING_WORK_MESSAGE, EchoJob.class.getName(), state));

      this.finishWork();

    } catch (Exception processingException) {
      try {
        LOGGER.error(String.format(ERROR_PROCESSING_WORK_MESSAGE, EchoJob.class.getName(), state, processingException.getMessage()));
        this.failWork(connectionManager, EchoJob.class.getName(), processingException.getMessage());
      } catch (Exception markingFailWorkException) {
        LOGGER.error(String.format(ERROR_MARKING_FAIL_WORK_MESSAGE, EchoJob.class.getName(), state, markingFailWorkException.getMessage()));
      }
    }
  }
}
