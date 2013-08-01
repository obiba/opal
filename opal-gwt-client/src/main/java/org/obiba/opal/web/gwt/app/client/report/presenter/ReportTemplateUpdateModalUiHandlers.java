package org.obiba.opal.web.gwt.app.client.report.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface ReportTemplateUpdateModalUiHandlers extends ModalUiHandlers {
  void updateReportTemplate();
  void onDialogHide();
  void enableSchedule();
  void disableSchedule();
  void onDialogHidden();
}
