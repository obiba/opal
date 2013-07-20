package org.obiba.opal.web.gwt.app.client.administration.fs.view;

import org.obiba.opal.web.gwt.app.client.administration.fs.presenter.FilesAdministrationPresenter;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class FilesAdministrationView extends ViewImpl implements FilesAdministrationPresenter.Display{

  private final FlowPanel panel;

  public FilesAdministrationView() {
    panel = new FlowPanel();
  }

  @Override
  public Widget asWidget() {
    return panel;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    panel.clear();
    panel.add(content);
  }
}
