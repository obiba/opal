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

import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
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

  private ViewDto viewDto;

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
    addEventHandlers();
    refreshDisplay();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
    refreshLocales();
    refreshTableData();
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

    getDisplay().setTableData(localizables);
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addLocaleChangeHandler(new LocaleChangeHandler()));
    super.registerHandler(getDisplay().addButtonClickHandler(new AddButtonClickHandler()));

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
      GWT.log("<edit: " + localizable.getName() + ">");
      editLocalizable(localizable, getDisplay().getSelectedLocale());
    } else if(DELETE_ACTION.equals(actionName)) {
      GWT.log("<delete: " + localizable.getName() + ">");
      deleteLocalizable(localizable, getDisplay().getSelectedLocale());
    }
    refreshTableData();
  }

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

    HandlerRegistration addButtonClickHandler(ClickHandler handler);
  }

  public interface Localizable extends Comparable<Localizable> {

    public String getName();

    public String getLabel();
  }

  class LocaleChangeHandler implements ChangeHandler {

    @Override
    public void onChange(ChangeEvent event) {
      refreshTableData();
    }
  }

  class AddButtonClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      // TODO: Show the "add" dialog.
    }
  }
}