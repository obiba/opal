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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.DerivedVariableConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.LocalizableDeleteEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

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

  private Runnable actionRequiringConfirmation;

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

    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));

    afterViewDtoSet();
  }

  public void setVariableDto(VariableDto variableDto) {
    this.variableDto = variableDto;

    variableDto.setAttributesArray(JsArrays.toSafeArray(variableDto.getAttributesArray()));
    variableDto.setCategoriesArray(JsArrays.toSafeArray(variableDto.getCategoriesArray()));
  }

  public VariableDto getVariableDto() {
    return this.variableDto;
  }

  void refreshLocales() {
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>> newBuilder().forResource("/datasource/" + viewDto.getDatasourceName() + "/table/" + viewDto.getName() + "/locales" + "?locale=en").get().withCallback(new ResourceCallback<JsArray<LocaleDto>>() {

      @Override
      public void onResource(Response response, JsArray<LocaleDto> locales) {
        getDisplay().setLocales(JsArrays.toSafeArray(locales));
      }
    }).send();
  }

  void refreshTableData() {
    List<Localizable> localizables = getLocalizables(getDisplay().getSelectedLocale());
    Collections.sort(localizables);

    getDisplay().setTableData(localizables);
  }

  protected void addEventHandlers() {
    // Register common handlers
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    super.registerHandler(eventBus.addHandler(DerivedVariableConfigurationRequiredEvent.getType(), new DerivedVariableConfigurationRequiredEventHandler()));
    super.registerHandler(getDisplay().addLocaleChangeHandler(new LocaleChangeHandler()));
    super.registerHandler(getDisplay().addAddButtonClickHandler(getAddButtonClickHandler()));
    addActionHandler(); // for "Edit" and "Delete" links
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));

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

  private void doActionImpl(final Localizable localizable, String actionName) {
    if(EDIT_ACTION.equals(actionName)) {
      getEditActionHandler().onEdit(localizable);
    } else if(DELETE_ACTION.equals(actionName)) {
      actionRequiringConfirmation = new Runnable() {

        @Override
        public void run() {
          getDeleteActionHandler().onDelete(localizable);
          refreshTableData();
          eventBus.fireEvent(new LocalizableDeleteEvent());
        }
      };

      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, getDeleteConfirmationTitle(), getDeleteConfirmationMessage()));
    }
  }

  protected void afterViewDtoSet() {
    // do nothing by default
  }

  protected abstract void bindDependencies();

  protected abstract void unbindDependencies();

  protected abstract void refreshDependencies();

  protected abstract List<Localizable> getLocalizables(String localeName);

  protected abstract ClickHandler getAddButtonClickHandler();

  protected abstract EditActionHandler getEditActionHandler();

  protected abstract DeleteActionHandler getDeleteActionHandler();

  protected abstract String getDeleteConfirmationTitle();

  protected abstract String getDeleteConfirmationMessage();

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

  interface EditActionHandler {

    public void onEdit(Localizable localizable);
  }

  interface DeleteActionHandler {

    public void onDelete(Localizable localizable);
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

  class DerivedVariableConfigurationRequiredEventHandler implements DerivedVariableConfigurationRequiredEvent.Handler {

    @Override
    public void onDerivedVariableConfigurationRequired(DerivedVariableConfigurationRequiredEvent event) {
      LocalizablesPresenter.this.setVariableDto(event.getVariable());
      refreshTableData();
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }
}