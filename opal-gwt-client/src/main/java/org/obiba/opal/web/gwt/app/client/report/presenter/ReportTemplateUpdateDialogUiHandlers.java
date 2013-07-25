package org.obiba.opal.web.gwt.app.client.report.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ReportTemplateUpdateDialogUiHandlers extends UiHandlers {
  void updateReportTemplate();
  void onDialogHide();
  void enableSchedule();
  void disableSchedule();
  void onDialogHidden();
}
