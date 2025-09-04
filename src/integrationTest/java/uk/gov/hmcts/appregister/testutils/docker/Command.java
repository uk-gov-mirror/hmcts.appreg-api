package uk.gov.hmcts.appregister.testutils.docker;

import java.io.IOException;
import org.springframework.test.context.DynamicPropertyRegistry;

public interface Command {
  void cleanupResources();

  void start(DynamicPropertyRegistry dynamicPropertyRegistry) throws IOException;

  boolean isSuccess();

  Integer getPortForContainer();

  boolean isRunning();
}
