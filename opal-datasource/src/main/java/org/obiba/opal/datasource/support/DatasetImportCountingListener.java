package org.obiba.opal.datasource.support;

import java.util.List;

import org.obiba.opal.core.domain.data.Dataset;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class DatasetImportCountingListener implements ItemWriteListener<Dataset>, StepExecutionListener {

  public static final String DATA_ITEM_COUNT = "dataItem.count";

  private StepExecution stepExecution;

  public void afterWrite(List<? extends Dataset> items) {
    int dataItemCount = 0;
    for(Dataset dataset : items) {
      dataItemCount += dataset.getDataItems().size();
    }
    doIncrementCount(stepExecution, dataItemCount);
  }

  public void beforeWrite(List<? extends Dataset> items) {
  }

  public void onWriteError(Exception exception, List<? extends Dataset> items) {
  }

  public ExitStatus afterStep(StepExecution stepExecution) {
    this.stepExecution = null;
    return null;
  }

  public void beforeStep(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
    doSetCount(stepExecution, 0);
  }

  protected int doGetCount(StepExecution stepExecution) {
    return stepExecution.getExecutionContext().getInt(DATA_ITEM_COUNT);
  }

  protected int doSetCount(StepExecution stepExecution, int count) {
    stepExecution.getExecutionContext().putInt(DATA_ITEM_COUNT, count);
    return doGetCount(stepExecution);
  }

  protected int doIncrementCount(StepExecution stepExecution, int increment) {
    return doSetCount(stepExecution, doGetCount(stepExecution) + increment);
  }
}
