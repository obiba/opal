/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.DisallowedCharactersValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredOptionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.HibernateDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateViewStepPresenter extends WidgetPresenter<CreateViewStepPresenter.Display> {
  //
  // Instance Variables
  //

  @Inject
  private Provider<ApplicationPresenter> applicationPresenter;

  @Inject
  private Provider<DashboardPresenter> dashboardPresenter;

  @Inject
  private DatasourceSelectorPresenter datasourceSelectorPresenter;

  @Inject
  private TableListPresenter tableListPresenter;

  @Inject
  private ConclusionStepPresenter conclusionStepPresenter;

  //
  // Constructors
  //

  @Inject
  public CreateViewStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    datasourceSelectorPresenter.bind();
    getDisplay().setDatasourceSelector(datasourceSelectorPresenter.getDisplay());

    tableListPresenter.bind();
    getDisplay().setTableSelector(tableListPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    datasourceSelectorPresenter.unbind();
    tableListPresenter.unbind();
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addCreateClickHandler(new CreateClickHandler()));
    super.registerHandler(getDisplay().addSelectExistingDatasourceClickHandler(new SelectExistingDatasourceClickHandler()));
    super.registerHandler(getDisplay().addCreateNewDatasourceClickHandler(new CreateNewDatasourceClickHandler()));
  }

  @Override
  public void revealDisplay() {
    refreshDisplay();
  }

  @Override
  public void refreshDisplay() {
    datasourceSelectorPresenter.refreshDisplay();
    getDisplay().setDatasourceSelectorEnabled(false);
    getDisplay().setNewDatasourceInputEnabled(false);
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

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setDatasourceSelector(DatasourceSelectorPresenter.Display datasourceSelector);

    void setDatasourceSelectorEnabled(boolean enabled);

    void setNewDatasourceInputEnabled(boolean enabled);

    void setTableSelector(TableListPresenter.Display tableSelector);

    HasValue<Boolean> getAttachToExistingDatasourceOption();

    HasValue<Boolean> getAttachToNewDatasourceOption();

    HasText getExistingDatasourceName();

    HasText getNewDatasourceName();

    HasText getViewName();

    HasValue<Boolean> getApplyGlobalVariableFilterOption();

    HasValue<Boolean> getAddVariablesOneByOneOption();

    boolean isAttachingToExistingDatasource();

    boolean isAttachingToNewDatasource();

    boolean isApplyingGlobalVariableFilter();

    boolean isAddingVariablesOneByOne();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCreateClickHandler(ClickHandler handler);

    HandlerRegistration addSelectExistingDatasourceClickHandler(ClickHandler handler);

    HandlerRegistration addCreateNewDatasourceClickHandler(ClickHandler handler);
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(dashboardPresenter.get()));
      ApplicationPresenter.Display appDisplay = applicationPresenter.get().getDisplay();
      appDisplay.setCurrentSelection(appDisplay.getDashboardItem());
    }
  }

  class CreateClickHandler implements ClickHandler {

    private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

    @SuppressWarnings("unchecked")
    public CreateClickHandler() {
      validators.add(new RequiredOptionValidator(RequiredOptionValidator.asSet(getDisplay().getAttachToExistingDatasourceOption(), getDisplay().getAttachToNewDatasourceOption()), "ViewMustBeAttachedToExistingOrNewDatasource"));
      validators.add(new ConditionalValidator(getDisplay().getAttachToNewDatasourceOption(), new RequiredTextValidator(getDisplay().getNewDatasourceName(), "DatasourceNameRequired")));
      validators.add(new ConditionalValidator(getDisplay().getAttachToNewDatasourceOption(), new DisallowedCharactersValidator(getDisplay().getNewDatasourceName(), new char[] { '.', ':' }, "DatasourceNameDisallowedChars")));
      validators.add(new RequiredTextValidator(getDisplay().getViewName(), "ViewNameRequired"));
      validators.add(new DisallowedCharactersValidator(getDisplay().getViewName(), new char[] { '.', ':' }, "ViewNameDisallowedChars"));

      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        public Collection<TableDto> getCollection() {
          return tableListPresenter.getTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<TableDto>(tablesField, 1, "TableSelectionRequired"));
      validators.add(new MatchingTableEntitiesValidator(tablesField));
      validators.add(new RequiredOptionValidator(RequiredOptionValidator.asSet(getDisplay().getApplyGlobalVariableFilterOption(), getDisplay().getAddVariablesOneByOneOption()), "VariableDefinitionMethodRequired"));
    }

    public void onClick(ClickEvent event) {
      String errorMessageKey = validate();
      if(errorMessageKey != null) {
        eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, errorMessageKey, null));
        return;
      }

      if(getDisplay().isAttachingToNewDatasource()) {
        createNewDatasourceAndView();
      } else {
        createView();
      }
    }

    String validate() {
      for(FieldValidator validator : validators) {
        String errorMessageKey = validator.validate();
        if(errorMessageKey != null) {
          return errorMessageKey;
        }
      }
      return null;
    }

    void createNewDatasourceAndView() {
      DatasourceFactoryDto dsFactoryDto = DatasourceFactoryDto.create();
      dsFactoryDto.setName(getDisplay().getNewDatasourceName().getText());
      HibernateDatasourceFactoryDto hibFactoryDto = HibernateDatasourceFactoryDto.create();
      dsFactoryDto.setExtension(HibernateDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, hibFactoryDto);

      ResponseCodeCallback responseCodeCallback = new ResponseCodeCallback() {

        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == 201) {
            createView();
          } else {
            String errorMessage = null;
            if(response.getText() != null && response.getText().length() != 0) {
              try {
                ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
                errorMessage = errorDto.getStatus();
              } catch(Exception ex) {
                // Should never get here!
                errorMessage = "InternalError";
              }
            } else {
              errorMessage = "UnknownError";
            }
            eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, errorMessage, null));
          }
        }
      };
      ResourceRequestBuilderFactory.newBuilder().put().forResource("/datasource/" + getDisplay().getNewDatasourceName()).accept("application/x-protobuf+json").withResourceBody(stringify(dsFactoryDto)).withCallback(201, responseCodeCallback).withCallback(400, responseCodeCallback).send();
    }

    void createView() {
      // Get the view name and datasource name.
      String viewName = getDisplay().getViewName().getText();
      String datasourceName = getDisplay().isAttachingToExistingDatasource() ? getDisplay().getExistingDatasourceName().getText() : getDisplay().getNewDatasourceName().getText();

      // Build the ViewDto for the request.
      ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder().fromTables(tableListPresenter.getTables());
      if(getDisplay().isApplyingGlobalVariableFilter()) {
        viewDtoBuilder.defaultJavaScriptView();
      } else if(getDisplay().isAddingVariablesOneByOne()) {
        viewDtoBuilder.defaultVariableListView();
      }
      ViewDto viewDto = viewDtoBuilder.build();

      // Create the resource request (the builder).
      ResourceRequestBuilder<JavaScriptObject> resourceRequestBuilder = ResourceRequestBuilderFactory.newBuilder().put().forResource("/datasource/" + datasourceName + "/view/" + viewName).accept("application/x-protobuf+json").withResourceBody(stringify(viewDto));

      // Update the Conclusion step then fire a WorkbenchChangeEvent to move to that step.
      conclusionStepPresenter.setResourceRequest(viewName, "/datasource/" + datasourceName + "/view/" + viewName, resourceRequestBuilder);
      conclusionStepPresenter.showConfigureViewWidgets(false);
      conclusionStepPresenter.sendResourceRequest();
      eventBus.fireEvent(new WorkbenchChangeEvent(conclusionStepPresenter));
    }
  }

  class SelectExistingDatasourceClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().setDatasourceSelectorEnabled(true);
      getDisplay().setNewDatasourceInputEnabled(false);
    }
  }

  class CreateNewDatasourceClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().setDatasourceSelectorEnabled(false);
      getDisplay().setNewDatasourceInputEnabled(true);
    }
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
  return $wnd.JSON.stringify(obj);
  }-*/;
}
