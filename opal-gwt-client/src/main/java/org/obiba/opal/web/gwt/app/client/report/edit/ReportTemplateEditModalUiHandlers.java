package org.obiba.opal.web.gwt.app.client.report.edit;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface ReportTemplateEditModalUiHandlers extends ModalUiHandlers {
  void updateReportTemplate();

  void onDialogHide();

  void enableSchedule();

  void disableSchedule();

  void onDialogHidden();
}
