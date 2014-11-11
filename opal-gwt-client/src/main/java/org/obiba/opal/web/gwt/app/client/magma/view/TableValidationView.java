/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TableValidationPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TableValidationUiHandlers;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.ValidationResultDto;

import java.util.*;

@SuppressWarnings("OverlyCoupledClass")
public class TableValidationView
    extends ViewWithUiHandlers<TableValidationUiHandlers>
        implements TableValidationPresenter.Display {

  interface TableValidationViewUiBinder extends UiBinder<Widget, TableValidationView> {}

  private static final TableValidationViewUiBinder uiBinder = GWT.create(TableValidationViewUiBinder.class);

  private final Widget widget;

    @UiField
    FlowPanel alertsPanel;

    @UiField
    Alert alert;

    @UiField
    Label errorMessage;

    private TableDto table;

    //@todo is there a better way than hardcoding the styles?
    private static final String TABLE_TAG = "<TABLE class=\"table table-striped table-condensed table-bordered bottom-margin\">";

    @UiField
    HTMLPanel validationResultsPanel;

    @Inject
  public TableValidationView(EventBus eventBus) {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setTable(TableDto table) {

      clearErrorMessages();

      if (needsValidationViewReset(table)) {
          //only clear validation results if needed.
          //this way we can keep the results if they are still on the same datasource/table
          setValidationResult(null);
      } else {
          validationResultsPanel.setVisible(true);
      }

      this.table = table;
  }

    @UiHandler("validate")
    public void onValidate(ClickEvent event) {
        getUiHandlers().onValidate();
    }

    @Override
    public void setValidationResult(ValidationResultDto dto) {

        if (dto != null) {
            SafeHtml html = buildValidationResultsHtml(dto);
            validationResultsPanel.getElement().setInnerSafeHtml(html);
        } else {
            validationResultsPanel.clear();
            validationResultsPanel.getElement().setInnerHTML("");
        }
    }

    @Override
    public void clearErrorMessages() {
        alertsPanel.clear();
    }

    @Override
    public void setErrorMessage(String title, String message) {
        alert.setHeading(title);
        errorMessage.getElement().setInnerHTML(message);
        alertsPanel.add(alert);
    }

    private static final Map<String, Set<String>> getVariableRuleMap(JSONObject rules) {
        List<String> vars = new ArrayList<>(rules.keySet());
        Map<String, Set<String>> map = new LinkedHashMap<>();
        for (String var: vars) {
            JSONArray array = (JSONArray)rules.get(var);
            Set<String> set = new LinkedHashSet<>();
            for (int i=0; i<array.size(); i++) {
                JSONString str = (JSONString)array.get(i);
                set.add(str.stringValue());
            }
            map.put(var, set);
        }
        return map;
    }

    private static Set<String> flatten(Collection<Set<String>> sets) {
        Set<String> result = new HashSet<>();
        for (Set<String> set: sets) {
            result.addAll(set);
        }
        return result;
    }

    private static Map<List<String>, List<String>> getVariableRuleFailedValuesMap(JSONObject failures) {
        Map<List<String>, List<String>> result = new HashMap<>();

        for (String key: failures.keySet()) {
            //@todo improve the tokenizing code (use regex)
            String str = key.replace("[","").replace("]", "").replace(",", " ");
            String[] parts = str.split("\\s+");
            List<String> pair = Arrays.asList(parts);
            result.put(pair, toList((JSONArray) failures.get(key)));
        }

        return result;
    }

    private static List<String> toList(JSONArray array) {
        List<String> result = new ArrayList<>();
        for (int i=0; i<array.size(); i++) {
            JSONString str = (JSONString)array.get(i);
            result.add(str.stringValue());
        }
        return result;
    }

    private SafeHtml buildValidationResultsHtml(ValidationResultDto dto) {
        JSONObject rules = (JSONObject) JSONParser.parseStrict(dto.getRules());
        JSONObject failures = (JSONObject)JSONParser.parseStrict(dto.getFailures());

        SafeHtmlBuilder builder = new SafeHtmlBuilder();

        if (rules.size() > 0) {
            Map<String, Set<String>> variableRuleMap = getVariableRuleMap(rules);
            Map<List<String>, List<String>> failedValuesMap = getVariableRuleFailedValuesMap(failures);
            builder.appendHtmlConstant("<h4>").appendEscaped("Overview").appendHtmlConstant("</h4>");
            builder.append(buildValidationSummaryTable(variableRuleMap, failedValuesMap));
            builder.appendHtmlConstant("<h4>").appendEscaped("Detail").appendHtmlConstant("</h4>");
            addValidationFailureTable(builder, failedValuesMap);
        } else {
            builder.appendEscaped("No validation configured");
        }
        return builder.toSafeHtml();
    }


    private SafeHtml buildValidationSummaryTable(Map<String, Set<String>> variableRuleMap, Map<List<String>, List<String>> failedValuesMap) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        List<String> constraints = new ArrayList<>(flatten(variableRuleMap.values()));

        builder.appendHtmlConstant(TABLE_TAG);
        builder.appendHtmlConstant("<TR>");
        addTableHeader(builder, "Variable");

        for (String constraint: constraints) {
            addTableHeader(builder, constraint);
        }

        builder.appendHtmlConstant("</TR>");

        for (String var: variableRuleMap.keySet()) {
            builder.appendHtmlConstant("<TR>");

            builder.appendHtmlConstant("<TD>").appendEscaped(var).appendHtmlConstant("</TD>");
            Set<String> set = variableRuleMap.get(var);
            for (String cons: constraints) {
                Boolean failure = null;
                if (set.contains(cons)) {
                    //constraint/variable is configured
                    List<String> key = Arrays.asList(var, cons);
                    failure = failedValuesMap.containsKey(key);
                }
                builder.appendHtmlConstant("<TD>");
                if (failure != null) {
                    addValidationCell(builder, failure.booleanValue());
                }
                builder.appendHtmlConstant("</TD>");
            }
            builder.appendHtmlConstant("</TR>");
        }

        builder.appendHtmlConstant("</TABLE>");

        return builder.toSafeHtml();
    }

    private void addValidationCell(SafeHtmlBuilder builder, boolean failure) {
        String color = "green";
        String text = "OK";
        if (failure) {
            color = "red";
            text = "FAILURE";
        }

        builder.appendHtmlConstant("<font color=\"" + color + "\">");
        builder.appendEscaped(text);
        builder.appendHtmlConstant("</font>");
    }

    private void addValidationFailureTable(SafeHtmlBuilder builder, Map<List<String>, List<String>> failedValuesMap) {
        builder.appendHtmlConstant(TABLE_TAG);
        builder.appendHtmlConstant("<TR>");
        addTableHeader(builder, "Variable");
        addTableHeader(builder, "Constraint");
        addTableHeader(builder, "Values");
        builder.appendHtmlConstant("</TR>");

        for (List<String> key: failedValuesMap.keySet()) {

            builder.appendHtmlConstant("<TR>");
            builder.appendHtmlConstant("<TD>").appendEscaped(key.get(0)).appendHtmlConstant("</TD>");
            builder.appendHtmlConstant("<TD>").appendEscaped(key.get(1)).appendHtmlConstant("</TD>");
            builder.appendHtmlConstant("<TD>");
            List<String> values = failedValuesMap.get(key);
            for (String value: values) {
                builder.appendEscaped(value).appendHtmlConstant("</BR>");
            }
            builder.appendHtmlConstant("</TD>");
            builder.appendHtmlConstant("</TR>");
        }

        builder.appendHtmlConstant("</TABLE>");
    }

    private static void addTableHeader(SafeHtmlBuilder builder, String header) {
        builder.appendHtmlConstant("<TH>").appendEscaped(header).appendHtmlConstant("</TH>");
    }

    private boolean needsValidationViewReset(TableDto newTable) {

        if (newTable == null || this.table == null) {
            return true;
        }

        if (!newTable.getName().equals(this.table.getName())) {
            return true; //different table
        }

        if (!newTable.getDatasourceName().equals(this.table.getDatasourceName())) {
            return true; //different datasource
        }

        return false;
    }

}
