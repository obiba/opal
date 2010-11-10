/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import java.util.Collections;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.DerivedVariableSelectionEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;

public abstract class LocalizablesPresenter extends WidgetPresenter<LocalizablesPresenter.Display> {
  //
  // Constants
  //

  public static final String EDIT_ACTION = "Edit";

  public static final String DELETE_ACTION = "Delete";

  //
  // Instance Variables
  //

  protected ViewDto viewDto;

  protected VariableDto variableDto;

  //
  // Constructors
  //

  public LocalizablesPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    bindDependencies();
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    unbindDependencies();
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
    if(viewDto != null) {
      refreshLocales();
      refreshTableData();
      refreshDependencies();
    }
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
  }

  public void setVariableDto(VariableDto variableDto) {
    this.variableDto = variableDto;
  }

  void refreshLocales() {
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>> newBuilder().forResource("/datasource/" + viewDto.getDatasourceName() + "/table/" + viewDto.getName() + "/locales" + "?locale=en").get().withCallback(new ResourceCallback<JsArray<LocaleDto>>() {

      @Override
      public void onResource(Response response, JsArray<LocaleDto> locales) {
        getDisplay().setLocales(locales);
      }
    }).send();
  }

  void refreshTableData() {
    List<Localizable> localizables = getLocalizables(getDisplay().getSelectedLocale());
    Collections.sort(localizables);

    GWT.log("localizables count = " + localizables.size());

    getDisplay().setTableData(localizables);
  }

  protected void addEventHandlers() {
    // Register common handlers
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    super.registerHandler(eventBus.addHandler(DerivedVariableSelectionEvent.getType(), new DerivedVariableSelectionEventHandler()));
    super.registerHandler(getDisplay().addLocaleChangeHandler(new LocaleChangeHandler()));
    super.registerHandler(getDisplay().addAddButtonClickHandler(getAddButtonClickHandler()));
    addActionHandler(); // for "Edit" and "Delete" links

    // Register additional handlers provided by subclasses
  }

  private void addActionHandler() {
    getDisplay().getActionsColumn().setActionHandler(new ActionHandler<Localizable>() {
      public void doAction(Localizable localizable, String actionName) {
        if(actionName != null) {
          doActionImpl(localizable, actionName);
        }
      }
    });
  }

  private void doActionImpl(Localizable localizable, String actionName) {
    if(EDIT_ACTION.equals(actionName)) {
      editLocalizable(localizable, getDisplay().getSelectedLocale());
    } else if(DELETE_ACTION.equals(actionName)) {
      deleteLocalizable(localizable, getDisplay().getSelectedLocale());
    }
    refreshTableData();
  }

  protected abstract void bindDependencies();

  protected abstract void unbindDependencies();

  protected abstract void refreshDependencies();

  protected abstract ClickHandler getAddButtonClickHandler();

  protected abstract List<Localizable> getLocalizables(String localeName);

  protected abstract void editLocalizable(Localizable localizable, String localeName);

  protected abstract void deleteLocalizable(Localizable localizable, String localeName);

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setAddButtonText(String addButtonText);

    void setLocales(JsArray<LocaleDto> locales);

    String getSelectedLocale();

    void setTableData(List<Localizable> localizables);

    HasActionHandler<Localizable> getActionsColumn();

    HandlerRegistration addLocaleChangeHandler(ChangeHandler handler);

    HandlerRegistration addAddButtonClickHandler(ClickHandler handler);
  }

  public abstract class Localizable implements Comparable<Localizable> {

    public abstract String getName();

    public abstract String getLabel();

    @Override
    public int compareTo(Localizable o) {
      return getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
      if(o instanceof Localizable) {
        return getName().equals(((Localizable) o).getName()) && getLabel().equals(((Localizable) o).getLabel());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return (getName() + ":" + getLabel()).hashCode();
    }
  }

  class LocaleChangeHandler implements ChangeHandler {

    @Override
    public void onChange(ChangeEvent event) {
      refreshTableData();
    }
  }

  class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      LocalizablesPresenter.this.setViewDto(event.getView());
    }
  }

  class DerivedVariableSelectionEventHandler implements DerivedVariableSelectionEvent.Handler {

    @Override
    public void onDerivedVariableSelection(DerivedVariableSelectionEvent event) {
      LocalizablesPresenter.this.setVariableDto(event.getVariable());
    }
  }
}