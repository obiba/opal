package org.obiba.opal.web.gwt.app.client.administration.fs.view;

import org.obiba.opal.web.gwt.app.client.administration.fs.presenter.FilesAdministrationPresenter;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class FilesAdministrationView extends ViewImpl implements FilesAdministrationPresenter.Display{

  interface ViewUiBinder extends UiBinder<Widget, FilesAdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  Panel body;

  @UiField
  Breadcrumbs breadcrumbs;


  public FilesAdministrationView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    body.clear();
    body.add(content);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

}
