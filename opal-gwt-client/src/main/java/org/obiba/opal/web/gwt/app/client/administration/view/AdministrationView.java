package org.obiba.opal.web.gwt.app.client.administration.view;

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.breadcrumbs.OpalNavLink;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class AdministrationView extends ViewImpl implements AdministrationPresenter.Display {


  @UiTemplate("AdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, AdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  OpalNavLink usersGroupsPlace;

  @UiField
  OpalNavLink unitsPlace;

  @UiField
  OpalNavLink databasesPlace;

  @UiField
  OpalNavLink mongoDbPlace;

  @UiField
  OpalNavLink esPlace;

  @UiField
  OpalNavLink indexPlace;

  @UiField
  OpalNavLink rPlace;

  @UiField
  OpalNavLink dataShieldPlace;

  @UiField
  OpalNavLink pluginsPlace;

  @UiField
  OpalNavLink reportsPlace;

  @UiField
  OpalNavLink filesPlace;

  @UiField
  OpalNavLink tasksPlace;

  @UiField
  OpalNavLink javaPlace;

  @UiField
  OpalNavLink serverPlace;

  public AdministrationView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setUsersGroupsHistoryToken(String historyToken) {
    usersGroupsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setUnitsHistoryToken(String historyToken) {
    unitsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setDatabasesHistoryToken(String historyToken) {
    databasesPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setMongoDbHistoryToken(String historyToken) {
    mongoDbPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setEsHistoryToken(String historyToken) {
    esPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setIndexHistoryToken(String historyToken) {
    indexPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setRHistoryToken(String historyToken) {
    rPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setDataShieldHistoryToken(String historyToken) {
    dataShieldPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setPluginsHistoryToken(String historyToken) {
    pluginsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setReportsHistoryToken(String historyToken) {
    reportsPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setFilesHistoryToken(String historyToken) {
    filesPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setTasksHistoryToken(String historyToken) {
    tasksPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setJavaHistoryToken(String historyToken) {
    javaPlace.setHistoryToken(historyToken);;
  }

  @Override
  public void setServerHistoryToken(String historyToken) {
    serverPlace.setHistoryToken(historyToken);;
  }

}
