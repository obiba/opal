package org.obiba.opal.spi.analysis.support.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public final class IdGeneratorFactory {

  public static IdGenerator createDateIdGenerator() {
    return () -> new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "-" + new Random().nextInt();
  }

  public static IdGenerator createUUIDGenerator() {
    return () -> UUID.randomUUID().toString();
  }

}
