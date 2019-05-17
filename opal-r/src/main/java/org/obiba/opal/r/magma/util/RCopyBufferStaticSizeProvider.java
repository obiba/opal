package org.obiba.opal.r.magma.util;

import org.obiba.magma.ValueTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RCopyBufferStaticSizeProvider implements RCopyBufferSizeProvider {

  private static final Logger log = LoggerFactory.getLogger(RCopyBufferStaticSizeProvider.class);

  private static final double MEMORY_RATIO = 0.5d;

  private double memoryRatio;

  public RCopyBufferStaticSizeProvider() {
    this(MEMORY_RATIO);
  }

  public RCopyBufferStaticSizeProvider(double memoryRatio) {
    setMemoryRatio(memoryRatio);
  }

  public void setMemoryRatio(double memoryRatio) {
    this.memoryRatio = memoryRatio <= 0 || memoryRatio >= 0.9 ? MEMORY_RATIO : memoryRatio;
  }

  @Override
  public int getOptimizedDataPointsCount(ValueTable table, long availableMemory,  long bufferMemory) {
    if (bufferMemory > availableMemory * memoryRatio) {
      int entityCount = table.getVariableEntityCount();
      int variableCount = table.getVariableCount();
      int dataPointsCount = entityCount * variableCount;

      if (dataPointsCount > 0) {
        log.info("Using {}% of the available memory at copy init, for {} data points", 100 * bufferMemory / availableMemory, dataPointsCount);
        return dataPointsCount;
      }
    }

    return 0;
  }
}
