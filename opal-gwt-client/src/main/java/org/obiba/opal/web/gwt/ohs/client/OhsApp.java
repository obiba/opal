/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.ohs.client;

import static com.google.gwt.query.client.GQuery.$;
import static gwtquery.plugins.ui.Ui.Ui;
import gwtquery.plugins.ui.widgets.Autocomplete;
import gwtquery.plugins.ui.widgets.Button;
import gwtquery.plugins.ui.widgets.Dialog;
import gwtquery.plugins.ui.widgets.Autocomplete.Source;
import gwtquery.plugins.ui.widgets.Button.Icons;

import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Selector;
import com.google.gwt.query.client.Selectors;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class OhsApp implements EntryPoint {

  @UiTemplate("VariableEditor.ui.xml")
  interface MyUiBinder extends UiBinder<Widget, OhsApp> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  public interface Widgets extends Selectors {

    @Selector(".opal-table-selector input")
    GQuery tableSelector();

    @Selector(".opal-table-selector button")
    GQuery tableSelectorButton();

    @Selector("#tableEditor")
    GQuery tableEditor();

    @Selector("button.opal-previous")
    GQuery previousButton();

    @Selector("button.opal-next")
    GQuery nextButton();

    @Selector("input.opal-goto")
    GQuery gotoVariable();

    @Selector("legend span")
    GQuery variableName();

    @Selector("#valueType")
    GQuery valueType();

    @Selector("#label")
    GQuery label();

    @Selector(".opal-script-item textarea")
    GQuery scriptArea();

    @Selector(".opal-script-item button")
    GQuery testButton();

    @Selector("div.opal-script-result")
    GQuery valuesDialog();

    @Selector("div.opal-script-result ul")
    GQuery values();

    @Selector("div.opal-script-result p.ui-state-error")
    GQuery errorMsg();

    @Selector("li.opal-categories-item")
    GQuery categories();

    @Selector("table.opal-variable-categories tbody")
    GQuery categoriesTable();

    @Selector("table.opal-variable-attributes tbody")
    GQuery attributesTable();
  }

  @UiField
  HTMLPanel panel;

  @UiField
  TextArea scriptArea;

  Widgets widgets;

  JsArray<VariableDto> variables;

  int index;

  public OhsApp() {
    uiBinder.createAndBindUi(this);
    widgets = GWT.create(Widgets.class);
  }

  @Override
  public void onModuleLoad() {
    RootPanel.get().add(panel);
    widgets.tableEditor().hide();
    widgets.previousButton().as(Ui).button(Button.Options.create().icons(Icons.create().primary("ui-icon-circle-triangle-w"))).disable().click(new Function() {
      @Override
      public void f(Element e) {
        --index;
        initVariable();
      }
    });
    widgets.nextButton().as(Ui).button(Button.Options.create().icons(Icons.create().secondary("ui-icon-circle-triangle-e"))).disable().click(new Function() {
      @Override
      public void f(Element e) {
        index++;
        initVariable();
      }
    });
    widgets.testButton().as(Ui).button(Button.Options.create().icons(Icons.create().secondary("ui-icon-calculator"))).disable().click(new Function() {
      @Override
      public boolean f(Event e) {
        VariableDto dto = variables.get(index);
        fetchValues(dto);
        return false;
      }
    });
    // Remember and re-select selection range when regaining focus
    widgets.scriptArea().blur(new Function() {
      @Override
      public boolean f(Event e) {
        int start = scriptArea.getCursorPos();
        int length = scriptArea.getSelectionLength();
        // Store the last selection range as data in the text area
        widgets.scriptArea().data("sel", new int[] { start, length });
        return true;
      }
    }).focus(new Function() {
      @Override
      public boolean f(Event e) {
        int[] sel = widgets.scriptArea().data("sel", int[].class);
        if(sel != null) {
          scriptArea.setSelectionRange(sel[0], sel[1]);
          widgets.scriptArea().removeData("sel");
        }
        return true;
      }
    }).keyup(new Function() {
      @Override
      public boolean f(Event e) {
        int keyCode = e.getKeyCode();
        if(keyCode == KeyCodes.KEY_ENTER && e.getCtrlKey()) {
          widgets.testButton().click();
          return false;
        }
        return true;
      }
    });
    widgets.valuesDialog().as(Ui).dialog(Dialog.Options.create().autoOpen(false).title("Results").resizable(true));
    widgets.tableSelectorButton().as(Ui).button(Button.Options.create().icons(Icons.create().primary("ui-icon-pencil"))).click(new Function() {
      @Override
      public void f(Element e) {
        // TODO: why is focus() not working?
        widgets.tableSelector().removeAttr("disabled").focus();
      }
    });
    widgets.gotoVariable().as(Ui).autocomplete().disable();
    loadTables();
  }

  public void gotTables(JsArray<DatasourceDto> resource) {
    JsArray<Autocomplete.Source> tables = JavaScriptObject.createArray().cast();
    for(int i = 0; i < resource.length(); i++) {
      DatasourceDto dto = resource.get(i);
      for(int t = 0; t < dto.getTableArray().length(); t++) {
        String tableName = dto.getTableArray().get(t);
        tables.push(Autocomplete.Source.create().value(dto.getLink() + "/table/" + tableName).label(dto.getName() + '.' + tableName));
      }
    }

    widgets.tableSelector().as(Ui).autocomplete(Autocomplete.Options.create().source(tables)).bind(Autocomplete.Event.select, new Function() {
      @Override
      public boolean f(Event e, Object data) {
        Source selected = ((Autocomplete.Event) data).item();
        widgets.tableSelector().attr("disabled", "true");
        loadVariables(selected.value());
        return false;
      }
    }).bind(Autocomplete.Event.search, new Function() {
      @Override
      public boolean f(Event e) {
        if(widgets.tableEditor().visible()) {
          widgets.tableEditor().fadeOut();
        }
        return true;
      }
    });

  }

  public void gotVariables(JsArray<VariableDto> resource) {
    this.variables = resource;
    this.index = 0;
    initVariable();

    JsArray<Autocomplete.Source> sources = JavaScriptObject.createArray().cast();
    for(int i = 0; i < this.variables.length(); i++) {
      VariableDto dto = this.variables.get(i);
      Autocomplete.Source source = Autocomplete.Source.create().value(Integer.toString(i));
      String label = findAttribute("label", dto);
      if(label.length() > 0) {
        source.label(dto.getName() + ": " + label);
      } else {
        source.label(dto.getName());
      }
      sources.push(source);
    }

    widgets.tableEditor().fadeIn();
    widgets.gotoVariable().as(Ui).autocomplete(Autocomplete.Options.create().source(sources)).enable().bind(Autocomplete.Event.select, new Function() {
      @Override
      public boolean f(Event e, Object data) {
        Source selected = ((Autocomplete.Event) data).item();
        index = Integer.valueOf(selected.value());
        initVariable();
        // Clear the goto text field
        $(e).val("");
        e.preventDefault();
        e.stopPropagation();
        return false;
      }
    });

  }

  public void initVariable() {
    boolean enablePrevious = index > 0;
    boolean enableNext = index < variables.length() - 1;

    widgets.previousButton().as(Ui).button(Button.Options.create().disabled(enablePrevious == false));
    widgets.nextButton().as(Ui).button(Button.Options.create().disabled(enableNext == false));
    widgets.values().children().remove();

    VariableDto dto = variables.get(index);
    widgets.variableName().text(dto.getName());
    widgets.label().val(findAttribute("label", dto));
    widgets.scriptArea().val(findAttribute("script", dto));
    widgets.scriptArea().removeData("sel");
    widgets.testButton().as(Ui).button().enable();
    widgets.valueType().val(dto.getValueType());
    initCategories(dto);
    initAttributes(dto);
  }

  public void initCategories(VariableDto dto) {
    widgets.categoriesTable().children().remove();
    for(int i = 0; i < dto.getCategoriesArray().length(); i++) {
      CategoryDto cat = dto.getCategoriesArray().get(i);
      String icon = cat.getIsMissing() ? "ui-icon ui-icon-check" : "ui-icon ui-icon-close";
      $("<tr><td>" + cat.getName() + "</td><td>" + findAttribute("label", cat) + "</td><td class=\"" + icon + "\"></td><td><button id=\"edit\"></button><button id=\"delete\"></button></td></tr>").appendTo(widgets.categoriesTable())//
      .children("#edit").as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-pencil")))//
      .next().as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-minus")));
    }/*
      * $("<tr><td><input type=\"text\"/></td><td><label for=\"missing\">Toggle</label><input id=\"missing\" type=\"checkbox\"/></td><td><button id=\"add\"></button></td></tr>"
      * ).appendTo(widgets.categoriesTable())//
      * .children("#missing").as(Ui).button(Button.Options.create().icons(Button.
      * Icons.create().primary("ui-icon-close"))).click(new Function() {
      * 
      * @Override public void f(Element e) { boolean checked = ((InputElement) e.cast()).isChecked();
      * $(e).as(Ui).button(Button.Options.create().icons(Button.Icons.create().primary(checked ? "ui-icon-check" :
      * "ui-icon-close"))); }
      * }).parents("tr").children("#add").as(Ui).button(Button.Options.create().text(false).icons(Button
      * .Icons.create().primary("ui-icon-plus")));
      */
  }

  public void initAttributes(VariableDto dto) {
    widgets.attributesTable().children().remove();
    for(int i = 0; i < dto.getAttributesArray().length(); i++) {
      AttributeDto attr = dto.getAttributesArray().get(i);

      $("<tr><td>" + attr.getName() + "</td><td>" + attr.getLocale() + "</td><td>" + attr.getValue() + "</td><td><button id=\"edit\"></button><button id=\"delete\"></button></td></tr>")//
      .appendTo(widgets.attributesTable())//
      .children("#edit").as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-pencil")))//
      .next().as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-minus")));
    }
  }

  private String findAttribute(String string, JsArray<AttributeDto> attrs) {
    for(int i = 0; i < attrs.length(); i++) {
      AttributeDto attr = attrs.get(i);
      if(attr.getName().equals(string)) {
        return attr.getValue();
      }
    }
    return "";
  }

  private String findAttribute(String string, VariableDto dto) {
    return findAttribute(string, dto.getAttributesArray());
  }

  private String findAttribute(String string, CategoryDto dto) {
    return findAttribute(string, dto.getAttributesArray());
  }

  private void loadVariables(String tableUri) {
    request("/ws" + tableUri + "/variables", new DefaultCallback<JsArray<VariableDto>>() {

      @Override
      protected void onResource(JsArray<VariableDto> resource) {
        gotVariables(resource);
      }
    });
  }

  private void loadTables() {
    request("/ws/datasources", new DefaultCallback<JsArray<DatasourceDto>>() {
      @Override
      protected void onResource(JsArray<DatasourceDto> resource) {
        gotTables(resource);
      }
    });
  }

  private void gotValues(JsArray<ValueDto> resource) {
    for(int i = 0; i < resource.length(); i++) {
      String value = resource.get(i).hasValue() ? resource.get(i).getValue() : "<span class=\"opal-missing\">NULL</span>";
      $("<li>" + value + "</li>").appendTo(widgets.values());
    }
    widgets.errorMsg().hide();
    widgets.valuesDialog().as(Ui).dialog().open();
  }

  private void fetchValues(VariableDto variable) {
    widgets.values().children().remove();

    String js = scriptArea.getSelectedText();
    if(js == null || js.length() == 0) {
      js = scriptArea.getText();
    }

    request("/ws" + widgets.tableSelector().val() + "/eval?valueType=" + widgets.valueType().val() + "&script=" + URL.encodeComponent(js), new DefaultCallback<JsArray<ValueDto>>() {
      @Override
      protected void onResource(JsArray<ValueDto> resource) {
        gotValues(resource);
      }
    });
  }

  private void request(String uri, DefaultCallback<?> callback) {
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, uri);
    builder.setHeader("Accept", "application/json");
    builder.setCallback(callback);
    try {
      builder.send();
    } catch(RequestException e) {
    }
  }

  private abstract class DefaultCallback<T> implements RequestCallback {

    @Override
    public void onError(Request request, Throwable exception) {
      widgets.errorMsg().show().children("span.opal-label").text(exception.getMessage());
      widgets.valuesDialog().as(Ui).dialog().open();
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
      if(response.getStatusCode() < 400) {
        final T resource = (T) JsonUtils.unsafeEval(response.getText());
        onResource(resource);
      } else if(response.getStatusCode() >= 400) {
        widgets.errorMsg().show().children("span.opal-label").text(response.getText());
        widgets.valuesDialog().as(Ui).dialog().open();
      }
    }

    abstract protected void onResource(T resource);

  }
}
