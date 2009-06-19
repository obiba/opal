package org.obiba.opal.mart.writer.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

public class LoggingItemWriter implements ItemWriter<Object> {

  private static final Logger log = LoggerFactory.getLogger(LoggingItemWriter.class);

  public void write(List<? extends Object> items) throws Exception {
    log.info("items.size()={}", items.size());
    for (Object item : items) {
      log.info("item={}", item);
    }
  }

}
