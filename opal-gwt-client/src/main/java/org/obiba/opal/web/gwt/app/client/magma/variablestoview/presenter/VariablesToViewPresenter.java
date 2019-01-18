/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.CategoricalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.*;

import javax.annotation.Nullable;
import java.util.*;

import static org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter.Display.FormField;

public class VariablesToViewPresenter extends ModalPresenterWidget<VariablesToViewPresenter.Display>
    implements VariablesToViewUiHandlers {

  private static final Translations translations = GWT.create(Translations.class);

  private final PlaceManager placeManager;

  private List<String> tableReferences;

  private Map<String, List<String>> variableTableReferences;

  private List<VariableDto> variables = new LinkedList<VariableDto>();

  private JsArray<DatasourceDto> datasources;

  @Inject
  public VariablesToViewPresenter(Display display, EventBus eventBus, PlaceManager placeManager) {
    super(eventBus, display);
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    refreshDatasources();
  }

  @Override
  public void saveVariable() {
    getView().clearErrors();

    VariableCopyValidationHandler variableCopyValidationHandler = new VariableCopyValidationHandler();

    variableCopyValidationHandler.getValidators().add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired", FormField.NAME.name()));
    variableCopyValidationHandler.getValidators().add(new DisallowedCharactersValidator(getView().getViewName(), new char[] { '.', ':' }, "ViewNameDisallowedChars", FormField.NAME.name()));

    if (variableCopyValidationHandler.validate()) {
      createOrUpdateViewWithVariables();
    }
  }

  @Override
  public void renameCategories() {
    updateDerivedVariables();
  }

  @Override
  public void perOccurrence() {
    updateDerivedVariables();
  }

  private void updateDerivedVariables() {
    boolean perOccurrence = getView().isPerOccurrence();
    int max = getView().getPerOccurrenceCount();
    boolean renameCategories = getView().isRenameCategoriesSelected();
    List<VariableDto> derivedVariables = Lists.newArrayList();
    for (VariableDto variable : variables) {
      // whether the context provides variables with same name from different tables
      boolean namedOccurrences = variableTableReferences.get(variable.getName()).size()>1;
      if (perOccurrence && (variable.getIsRepeatable() || namedOccurrences)) {
        if (namedOccurrences) splitNamedOccurrencesVariable(variable, derivedVariables, renameCategories);
        else splitRepeatableVariable(variable, derivedVariables, max, renameCategories);
      } else if (renameCategories) {
        // Keep new name if changed
        VariableDto derived = getDerivedVariable(variable);
        derived.setIsRepeatable(namedOccurrences);
        appendDerivedVariable(derivedVariables, derived);
      } else {
        VariableDto derived = new VariableDuplicationHelper(variable).getDerivedVariable();
        derived.setIsRepeatable(namedOccurrences);
        appendDerivedVariable(derivedVariables, derived);
      }
    }

    getView().renderVariables(variables, derivedVariables, false);
  }

  /**
   * Original variable is repeatable and can be split by occurrence index (with a maximum count).
   *
   * @param variable
   * @param derivedVariables
   * @param max
   * @param renameCategories
   */
  private void splitRepeatableVariable(VariableDto variable, List<VariableDto> derivedVariables, int max, boolean renameCategories) {
    for (int i = 0; i < max; i++) {
      if (renameCategories)
        appendDerivedVariable(derivedVariables, getDerivedVariable(null, variable, i));
      else
        appendDerivedVariable(derivedVariables, new VariableDuplicationHelper(variable, i).getDerivedVariable());
    }
  }

  /**
   * Several original variables have the same name, and then can be derived distinctly.
   *
   * @param variable
   * @param derivedVariables
   * @param renameCategories
   */
  private void splitNamedOccurrencesVariable(VariableDto variable, List<VariableDto> derivedVariables, boolean renameCategories) {
    int valueAt = 0;
    for (String ref : variableTableReferences.get(variable.getName())) {
      if (renameCategories)
        appendDerivedVariable(derivedVariables, getDerivedVariable(ref, variable, valueAt));
      else
        appendDerivedVariable(derivedVariables, new VariableDuplicationHelper(ref, variable, valueAt).getDerivedVariable());
      valueAt++;
    }
  }

  /**
   * Append a derived variable to a list, making sure variable names are unique.
   *
   * @param derivedVariables
   * @param derived
   */
  private void appendDerivedVariable(List<VariableDto> derivedVariables, VariableDto derived) {
    // make sure the derived variables name are unique
    for (VariableDto var : derivedVariables) {
      if (var.getName().equals(derived.getName())) return;
    }
    derivedVariables.add(derived);
  }

  public void show(TableDto table, List<VariableDto> variables) {
    this.tableReferences = Lists.newArrayList(table.getDatasourceName() + "." + table.getName());
    this.variableTableReferences = Maps.newHashMap();
    this.variables = variables;

    //refreshDatasources();

    // Prepare the array of variableDto
    List<VariableDto> derivedVariables = Lists.newArrayList();
    for (VariableDto variable : variables) {
      List<String> refs = Lists.newArrayList(table.getDatasourceName() + "." + table.getName());
      variableTableReferences.put(variable.getName(), refs);
      derivedVariables.add(new VariableDuplicationHelper(variable).getDerivedVariable());
    }

    getView().renderVariables(variables, derivedVariables, true);
    getView().showDialog();
  }

  public void show(List<String> variableFullNames) {
    show(variableFullNames, "");
  }

  public void show(List<String> variableFullNames, String entityFilter) {
    getView().renderEntityFilter(entityFilter);
    this.tableReferences = Lists.newArrayList();
    this.variableTableReferences = Maps.newHashMap();
    final int expectedCount = variableFullNames.size();
    final Map<String, VariableDto> derivedVariablesByName = Maps.newHashMap();
    final List<String> notFoundVariables = Lists.newArrayList();
    for (final String varFullName : variableFullNames) {
      final MagmaPath.Parser parser = MagmaPath.Parser.parse(varFullName);
      // build a unique list of table references
      String tableRef = parser.getTableReference();
      if (!tableReferences.contains(tableRef)) tableReferences.add(tableRef);
      //if (tableReferences.size() == 1) refreshDatasources();
      // build a unique list of derived variables
      // when several original variables have the same name, the unique derived + repeatable variable is added
      ResourceRequestBuilderFactory.<VariableDto>newBuilder() //
          .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
              .build(parser.getDatasource(), parser.getTable(), parser.getVariable())) //
          .get() //
          .withCallback(new ResourceCallback<VariableDto>() {
            @Override
            public void onResource(Response response, VariableDto resource) {
              appendVariable(resource);
              if ((variables.size() + notFoundVariables.size()) == expectedCount) {
                renderVariables();
              }
            }

            private void appendVariable(VariableDto resource) {
              variables.add(resource);
              if (variableTableReferences.containsKey(parser.getVariable()))
                variableTableReferences.get(parser.getVariable()).add(parser.getDatasource() + "." + parser.getTable());
              else{
                List<String> refs = Lists.newArrayList(parser.getDatasource() + "." + parser.getTable());
                variableTableReferences.put(parser.getVariable(), refs);
              }
              if (derivedVariablesByName.containsKey(resource.getName())) derivedVariablesByName.get(resource.getName()).setIsRepeatable(true);
              else {
                VariableDto derived = new VariableDuplicationHelper(resource).getDerivedVariable();
                derived.setIndex(derivedVariablesByName.size());
                derivedVariablesByName.put(resource.getName(), derived);
              }
            }

            private void renderVariables() {
              getView().renderVariables(variables, Lists.newArrayList(derivedVariablesByName.values()), true);
              getView().showDialog();
            }
          }).withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              notFoundVariables.add(varFullName);
            }
          }, Response.SC_NOT_FOUND, Response.SC_FORBIDDEN).send();
    }
  }

  private void createOrUpdateViewWithVariables() {
    ViewDto view = ViewDtoBuilder.newBuilder().setName(getView().getViewName().getText()).fromTableReferences(tableReferences)
        .defaultVariableListView().build();
    VariableListViewDto derivedVariables = (VariableListViewDto) view
        .getExtension(VariableListViewDto.ViewDtoExtensions.view);

    JsArray<VariableDto> variablesDto = JsArrays.create();
    for (VariableDto v : getView().getVariables(true)) {
      // Push with the right name if it was changed and initialize index
      v.setIndex(0);
      variablesDto.push(v);
    }
    derivedVariables.setVariablesArray(variablesDto);

    if (getView().hasEntityFilter() && !Strings.isNullOrEmpty(getView().getEntityFilter())) {
      view.setWhere(getView().getEntityFilter());
    }

    ResponseCodeCallback createCodingViewCallback = new CreateViewCallBack(view);
    ResourceCallback<ViewDto> alreadyExistCodingViewCallback = new UpdateExistViewCallBack(variablesDto);
    UriBuilder uriBuilder = UriBuilder.create();
    uriBuilder.segment("datasource", getView().getDatasourceName(), "view", getView().getViewName().getText());

    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(alreadyExistCodingViewCallback)//
        .withCallback(Response.SC_NOT_FOUND, createCodingViewCallback)//
        .send();
  }


  private VariableDto getDerivedVariable(VariableDto variable) {
    return getDerivedVariable(null, variable, -1);
  }

  private VariableDto getDerivedVariable(String tableRef, VariableDto variable, int valueAt) {
    if (VariableDtos.hasCategories(variable) && ("text".equals(variable.getValueType()) ||
        "integer".equals(variable.getValueType()) && !VariableDtos.allCategoriesMissing(variable))) {
      CategoricalVariableDerivationHelper derivationHelper = new CategoricalVariableDerivationHelper(tableRef, variable, valueAt);
      derivationHelper.initializeValueMapEntries();
      return derivationHelper.getDerivedVariable();
    }
    return new VariableDuplicationHelper(tableRef, variable, valueAt).getDerivedVariable();
  }

  private class CreateViewCallBack implements ResponseCodeCallback {
    ViewDto view;

    private CreateViewCallBack(ViewDto view) {
      this.view = view;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      ResponseCodeCallback callbackHandler = new CreatedViewCallBack(view, translations.addViewSuccess());
      UriBuilder uriBuilder = UriBuilder.create();
      uriBuilder.segment("datasource", getView().getDatasourceName(), "views");
      ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).post()
          .withResourceBody(ViewDto.stringify(view))//
          .withCallback(callbackHandler, Response.SC_OK, Response.SC_CREATED)//
          .withCallback(new ErrorResponseCallback(getView().asWidget()), Response.SC_FORBIDDEN,
              Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_REQUEST)//
          .send();
    }
  }

  private class UpdateExistViewCallBack implements ResourceCallback<ViewDto> {
    JsArray<VariableDto> variables;

    private UpdateExistViewCallBack(JsArray<VariableDto> variables) {
      this.variables = variables;
    }

    @Override
    public void onResource(Response response, ViewDto viewDto) {
      // Proceed with view creation
      VariableListViewDto derivedVariables = (VariableListViewDto) viewDto
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      updateFromTables(viewDto);
      updateWhere(viewDto);

      JsArray<VariableDto> existingVariables = JsArrays.toSafeArray(derivedVariables.getVariablesArray());
      for (int i = 0; i < variables.length(); i++) {
        VariableDto var = variables.get(i);
        int pos = getVariablePosition(existingVariables, var.getName());
        var.setIndex(getVariableIndex(existingVariables, var.getName(), pos));
        if (pos == -1) {
          existingVariables.push(var);
        } else {
          existingVariables.set(pos, var);
        }
      }
      derivedVariables.setVariablesArray(existingVariables);

      ResponseCodeCallback callbackHandler = new CreatedViewCallBack(viewDto, translations.updateViewSuccess());
      UriBuilder builder = UriBuilder.create();
      builder.segment("datasource", getView().getDatasourceName(), "view", viewDto.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(builder.build()).put()
          .withResourceBody(ViewDto.stringify(viewDto))//
          .withCallback(callbackHandler, Response.SC_OK, Response.SC_CREATED, Response.SC_FORBIDDEN,
              Response.SC_BAD_REQUEST)//
          .send();
    }

    private void updateFromTables(ViewDto viewDto) {
      // Update from tables
      JsArrayString fromTables = viewDto.getFromArray();
      for (String tableRef : tableReferences) {
        boolean addTable = true;
        for (int i = 0; i < fromTables.length(); i++) {
          if (fromTables.get(i).equals(tableRef)) {
            addTable = false;
            break;
          }
        }
        if (addTable) {
          fromTables.push(tableRef);
        }
      }
    }

    private void updateWhere(ViewDto viewDto) {
      if (getView().hasEntityFilter() && !Strings.isNullOrEmpty(getView().getEntityFilter())) {
        viewDto.setWhere(getView().getEntityFilter());
      }
    }

    private int getVariableIndex(JsArray<VariableDto> vars, String name, int pos) {
      if (pos == -1) {
        int max = 0;
        for (int i = 0; i < vars.length(); i++) {
          VariableDto var = vars.get(i);
          if (max<var.getIndex()) {
            max = var.getIndex();
          }
        }
        return max == 0 ? 0 : max + 1;
      } else {
        return vars.get(pos).getIndex();
      }
    }

    private int getVariablePosition(JsArray<VariableDto> vars, String name) {
      for (int i = 0; i < vars.length(); i++) {
        if (vars.get(i).getName().equals(name)) {
          return i;
        }
      }
      return -1;
    }
  }

  private class CreatedViewCallBack implements ResponseCodeCallback {

    ViewDto view;

    String message;

    private CreatedViewCallBack(ViewDto view, String message) {
      this.view = view;
      this.message = message;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      getEventBus().fireEvent(NotificationEvent.newBuilder().info(message).build());
      getEventBus().fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
      selectView();
    }

    private void selectView() {
      placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(getView().getDatasourceName(), view.getName()));
    }
  }

  private void refreshDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            datasources = JsArrays.toSafeArray(resource);
            for (int i = 0; i < datasources.length(); i++) {
              DatasourceDto d = datasources.get(i);
              d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
            }
            // pre select the first datasource
            getView().setDatasources(datasources, MagmaPath.Parser.parse(tableReferences.get(0)).getDatasource());
          }
        }).send();
  }

  public interface Display extends PopupView, HasUiHandlers<VariablesToViewUiHandlers> {

    enum FormField {
      NAME,
      VARIABLE,
      VARIABLES
    }

    void showDialog();

    void hideDialog();

    void setDatasources(JsArray<DatasourceDto> datasources, String name);

    void renderEntityFilter(String entityFilter);

    void renderVariables(List<VariableDto> originalVariables, List<VariableDto> rows, boolean clearNames);

    HasText getViewName();

    boolean hasEntityFilter();

    String getEntityFilter();

    List<VariableDto> getVariables(boolean withNewNames);

    String getDatasourceName();

    boolean isRenameCategoriesSelected();

    boolean isPerOccurrence();

    int getPerOccurrenceCount();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);
  }

  private class VariableCopyValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if (validators == null) {
        validators = new LinkedHashSet<FieldValidator>();

        validators.add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired", FormField.NAME.name()));
        validators.add(new ConditionValidator(
            viewNotCurrentCondition(getView().getDatasourceName(), getView().getViewName().getText()),
            "CopyVariableCurrentView", FormField.NAME.name()));

        List<VariableDto> list = getView().getVariables(true);
        String variableGroupName = list.size() == 1 ? FormField.VARIABLE.name() : FormField.VARIABLES.name();
        for (VariableDto v : list) {
          validators.add(new ConditionValidator(variableNameEmptyCondition(v.getName()), "CopyVariableNameRequired",
              variableGroupName));

          @SuppressWarnings("unchecked")
          List<String> args = new ArrayList();
          args.add(v.getName());
          validators.add(new ConditionValidator(variableNameColonCondition(v.getName()),
              TranslationsUtils.replaceArguments(translations.userMessageMap().get("CopyVariableNameColon"), args),
              variableGroupName));
        }

        validators.add(new VariableNameUniqueCondition(list));
      }
      return validators;
    }

    private HasValue<Boolean> viewNotCurrentCondition(final String datasourceName, final String viewName) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          String selfRef = datasourceName + "." + viewName;
          for (String tableRef : tableReferences) {
            if (selfRef.equals(tableRef)) return false;
          }
          return true;
        }
      };
    }

    private HasValue<Boolean> variableNameEmptyCondition(final String name) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !name.isEmpty();
        }
      };
    }

    private HasValue<Boolean> variableNameColonCondition(final String name) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !name.contains(":");
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(FormField.valueOf(id), message);
    }
  }

  private static class VariableNameUniqueCondition extends AbstractFieldValidator {

    List<VariableDto> variables;

    private VariableNameUniqueCondition(List<VariableDto> variables) {
      super("CopyVariableNameAlreadyExists");
      this.variables = variables;
    }

    @Override
    protected boolean hasError() {
      Collection<String> names = new HashSet<String>();

      for (VariableDto var : variables) {
        if (!names.add(var.getName())) {
          List<String> args = new ArrayList<String>();
          args.add(var.getName());
          setArgs(args);
          return true;
        }
      }
      return false;
    }
  }
}
