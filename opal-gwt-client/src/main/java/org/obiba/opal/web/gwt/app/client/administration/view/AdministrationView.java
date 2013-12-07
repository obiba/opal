package org.obiba.opal.web.gwt.app.client.administration.view;

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class AdministrationView extends ViewImpl implements AdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, AdministrationView> {}

  @UiField
  OpalNavLink usersGroupsPlace;

  @UiField
  OpalNavLink unitsPlace;

  @UiField
  OpalNavLink databasesPlace;

  @UiField
  OpalNavLink searchPlace;

  @UiField
  OpalNavLink rPlace;

  @UiField
  OpalNavLink dataShieldPlace;

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

  @UiField
  OpalNavLink taxonomiesPlace;

  @Inject
  public AdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
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
  public void setIndexHistoryToken(String historyToken) {
    searchPlace.setHistoryToken(historyToken);
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
    javaPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setServerHistoryToken(String historyToken) {
    serverPlace.setHistoryToken(historyToken);
  }

  @Override
  public void setTaxonomiesHistoryToken(String historyToken) {
    taxonomiesPlace.setHistoryToken(historyToken);
  }

}
