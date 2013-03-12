package org.obiba.opal.reporting.service;

import java.util.Map;

import org.obiba.opal.core.runtime.Service;

public interface ReportService extends Service {

  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException;

}
