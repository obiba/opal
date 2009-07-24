package org.obiba.opal.datasource.support;

import org.obiba.opal.core.domain.data.Dataset;
import org.springframework.batch.core.ItemReadListener;

public class DatasetReadListener implements ItemReadListener<Dataset> {

  public void afterRead(Dataset item) {
  }

  public void beforeRead() {
  }

  public void onReadError(Exception ex) {
  }

}
