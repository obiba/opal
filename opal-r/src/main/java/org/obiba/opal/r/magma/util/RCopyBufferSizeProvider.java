package org.obiba.opal.r.magma.util;

import org.obiba.magma.ValueTable;

public interface RCopyBufferSizeProvider {

  int getOptimizedDataPointsCount(ValueTable table, long availableMemory, long bufferMemory);

}
