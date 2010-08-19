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
import gwtquery.plugins.ui.widgets.Tabs;
import gwtquery.plugins.ui.widgets.Autocomplete.Source;
import gwtquery.plugins.ui.widgets.Button.Icons;

import org.obiba.opal.web.gwt.rest.client.DefaultResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.event.RequestCredentialsExpiredEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DescriptiveStatsDto;
import org.obiba.opal.web.model.client.magma.FrequencyDto;
import org.obiba.opal.web.model.client.magma.ValueDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
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

    @Selector("div.opal-login")
    GQuery loginDialog();

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

    @Selector("button.opal-save")
    GQuery saveButton();

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

    @Selector("div.opal-feedback-dialog")
    GQuery feedbackDialog();

    @Selector("div.opal-feedback-dialog p.ui-state-error")
    GQuery errorMsg();

    @Selector("div.opal-script-result")
    GQuery evalResults();

    @Selector("div.opal-script-result ul")
    GQuery values();

    @Selector("div.opal-script-result button")
    GQuery moreValues();

    @Selector(".opal-tabs")
    GQuery tabs();

    @Selector("table.opal-variable-categories tbody")
    GQuery categoriesTable();

    @Selector("table.opal-variable-attributes tbody")
    GQuery attributesTable();

    @Selector("#opal-variable-histogram")
    GQuery histogram();
  }

  private final DefaultEventBus eventBus = new DefaultEventBus();

  private final RequestCredentials credentials = new RequestCredentials();

  @UiField
  HTMLPanel panel;

  @UiField
  TextArea scriptArea;

  Widgets widgets;

  JsArray<VariableDto> variables;

  int index;

  Request currentRequest;

  JqPlot currentPlot;

  public OhsApp() {
    uiBinder.createAndBindUi(this);
    widgets = GWT.create(Widgets.class);
    DefaultResourceRequestBuilder.setup(eventBus, credentials);
  }

  @Override
  public void onModuleLoad() {
    RootPanel.get().add(panel);
    widgets.loginDialog().as(Ui).dialog(Dialog.Options.create().autoOpen(false).modal(true).buttons(Dialog.Buttons.create().define("Login", new Function() {
      @Override
      public void f(Element e) {
        String username = $(e).parents(".ui-dialog").children("#username").val();
        String password = $(e).parents(".ui-dialog").children("#password").val();
        ResourceRequestBuilderFactory.newBuilder().forResource("/auth/sessions").post().withCallback(201, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            // When a 201 happens, we should have credentials, but we'll test anyway.
            if(credentials.hasCredentials()) {
              widgets.loginDialog().as(Ui).dialog().close();
              loadTables();
            }
          }
        }).withFormBody("username", username, "password", password).send();
      }
    })));
    widgets.tableEditor().hide();
    widgets.previousButton().as(Ui).button(Button.Options.create().icons(Icons.create().primary("ui-icon-circle-triangle-w"))).disable().click(new Function() {
      @Override
      public void f(Element e) {
        --index;
        cancelPendingRequest();
        initVariable();
      }
    });
    widgets.nextButton().as(Ui).button(Button.Options.create().icons(Icons.create().secondary("ui-icon-circle-triangle-e"))).disable().click(new Function() {
      @Override
      public void f(Element e) {
        index++;
        cancelPendingRequest();
        initVariable();
      }
    });
    widgets.saveButton().as(Ui).button(Button.Options.create().icons(Icons.create().primary("ui-icon-disk"))).click(new Function() {
      @Override
      public void f(Element e) {
        widgets.saveButton().as(Ui).button(Button.Options.create().icons(Icons.create().primary("ui-icon-pencil"))).disable();
        saveVariable();
      }

    });
    widgets.testButton().as(Ui).button(Button.Options.create().icons(Icons.create().secondary("ui-icon-calculator"))).disable().click(new Function() {
      @Override
      public boolean f(Event e) {
        fetchValues(0);
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
    widgets.feedbackDialog().as(Ui).dialog(Dialog.Options.create().autoOpen(false).title("Results").resizable(true));
    widgets.moreValues().as(Ui).button().click(new Function() {
      @Override
      public void f(Element e) {
        Integer newOffset = $(e).data("offset", Integer.class) + 10;
        fetchValues(newOffset);
      }
    });
    widgets.tableSelectorButton().as(Ui).button(Button.Options.create().icons(Icons.create().primary("ui-icon-pencil"))).click(new Function() {
      @Override
      public void f(Element e) {
        // TODO: why is focus() not working?
        widgets.tableSelector().removeAttr("disabled").focus();
      }
    });
    widgets.gotoVariable().as(Ui).autocomplete().disable();

    $(Document.get()).keydown(new Function() {
      @Override
      public boolean f(Event e) {
        if(e.getCtrlKey()) {
          boolean handled = true;
          switch(e.getKeyCode()) {
          case 'S':
            widgets.saveButton().click();
            break;
          case 'T':
            widgets.testButton().click();
            break;
          case KeyCodes.KEY_RIGHT:
            widgets.nextButton().click();
            break;
          case KeyCodes.KEY_LEFT:
            widgets.previousButton().click();
            break;
          default:
            handled = false;
          }
          if(handled) {
            e.stopPropagation();
            return false;
          }
        }
        return true;
      }
    });

    widgets.tabs().as(Ui).tabs().bind(Tabs.Event.show, new Function() {
      @Override
      public boolean f(Event e, Object data) {
        Tabs.Event tabevent = (Tabs.Event) data;
        if(tabevent.panel().getId().equals("statistics")) {
          if(currentPlot != null) {
            currentPlot.redraw();
          }
        }
        return true;
      }
    });

    if(credentials.hasCredentials()) {
      loadTables();
    } else {
      widgets.loginDialog().as(Ui).dialog().open();
    }

    eventBus.addHandler(RequestCredentialsExpiredEvent.getType(), new RequestCredentialsExpiredEvent.Handler() {

      @Override
      public void onCredentialsExpired(RequestCredentialsExpiredEvent e) {
        widgets.loginDialog().as(Ui).dialog().open();
      }
    });

    eventBus.addHandler(RequestErrorEvent.getType(), new RequestErrorEvent.Handler() {

      @Override
      public void onRequestError(RequestErrorEvent e) {
        widgets.errorMsg().show().children("span.opal-label").text(e.getException().getMessage());
        widgets.evalResults().hide();
        widgets.feedbackDialog().as(Ui).dialog().open();
      }
    });

    eventBus.addHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {

      @Override
      public void onUnhandledResponse(UnhandledResponseEvent e) {
        if(credentials.hasCredentials() && e.getResponse().getStatusCode() >= 400) {
          widgets.errorMsg().show().children("span.opal-label").text(e.getResponse().getText());
          widgets.evalResults().hide();
          widgets.feedbackDialog().as(Ui).dialog().open();
        }
      }
    });
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
      String label = findAttributeValue("label", dto);
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

  private void saveVariable() {
    VariableDto dto = this.variables.get(index);
    dto.setValueType(widgets.valueType().val());
    findAttribute("label", dto).setValue(widgets.label().val());
    findAttribute("script", dto).setValue(widgets.scriptArea().val());
    ResourceRequestBuilderFactory.newBuilder().put().forResource(dto.getLink()).withResourceBody(VariableDto.stringify(dto)).withCallback(200, new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        widgets.saveButton().as(Ui).button(Button.Options.create().icons(Icons.create().primary("ui-icon-disk"))).enable();
      }
    }).send();
  }

  public void initVariable() {
    boolean enablePrevious = index > 0;
    boolean enableNext = index < variables.length() - 1;

    widgets.previousButton().as(Ui).button(Button.Options.create().disabled(enablePrevious == false));
    widgets.nextButton().as(Ui).button(Button.Options.create().disabled(enableNext == false));
    widgets.values().children().remove();

    VariableDto dto = variables.get(index);
    widgets.variableName().text(dto.getName());
    widgets.label().val(findAttributeValue("label", dto));
    widgets.scriptArea().val(findAttributeValue("script", dto));
    widgets.scriptArea().removeData("sel");
    widgets.testButton().as(Ui).button().enable();
    widgets.valueType().val(dto.getValueType());
    initCategories(dto);
    initAttributes(dto);
    plot(dto);
  }

  public void initCategories(VariableDto dto) {
    widgets.categoriesTable().children().remove();
    if(dto.getCategoriesArray() != null) {
      for(int i = 0; i < dto.getCategoriesArray().length(); i++) {
        CategoryDto cat = dto.getCategoriesArray().get(i);
        String icon = cat.getIsMissing() ? "ui-icon ui-icon-check" : "ui-icon ui-icon-close";
        $("<tr><td>" + cat.getName() + "</td><td>" + findAttributeValue("label", cat) + "</td><td class=\"" + icon + "\"></td><td><button id=\"edit\"></button><button id=\"delete\"></button></td></tr>").appendTo(widgets.categoriesTable())//
        .children("#edit").as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-pencil")))//
        .next().as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-minus")));
      }
    }
  }

  public void initAttributes(VariableDto dto) {
    widgets.attributesTable().children().remove();
    if(dto.getAttributesArray() != null) {
      for(int i = 0; i < dto.getAttributesArray().length(); i++) {
        AttributeDto attr = dto.getAttributesArray().get(i);

        $("<tr><td>" + attr.getName() + "</td><td>" + attr.getLocale() + "</td><td>" + attr.getValue() + "</td><td><button id=\"edit\"></button><button id=\"delete\"></button></td></tr>")//
        .appendTo(widgets.attributesTable())//
        .children("#edit").as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-pencil")))//
        .next().as(Ui).button(Button.Options.create().text(false).icons(Button.Icons.create().primary("ui-icon-minus")));
      }
    }
  }

  private void plot(VariableDto dto) {
    widgets.histogram().children().remove();
    currentPlot = null;

    if(isContinuous(dto)) {
      request(dto.getLink() + "/univariate?p=0.05&=p0.5&p=5&p=10&p=15&p=20&p=25&p=30&p=35&p=40&p=45&p=50&p=55&p=60&p=65&p=70&p=75&p=80&p=85&p=90&p=95&p=99.5&p=99.95", new ResourceCallback<DescriptiveStatsDto>() {

        @Override
        public void onResource(Response response, DescriptiveStatsDto resource) {
          JqPlotQQ plot = new JqPlotQQ("opal-variable-histogram", resource.getMin(), resource.getMax());
          plot.push(resource.getPercentilesArray(), resource.getDistributionPercentilesArray());
          plot.plot();
          currentPlot = plot;
        }
      });
    } else if(isCategorical(dto)) {
      request(dto.getLink() + "/frequencies", new ResourceCallback<JsArray<FrequencyDto>>() {

        @Override
        public void onResource(Response response, JsArray<FrequencyDto> freqs) {
          JqPlotBarChart plot = new JqPlotBarChart("opal-variable-histogram");
          for(int i = 0; i < freqs.length(); i++) {
            FrequencyDto value = freqs.get(i);
            if(value.hasValue()) {
              plot.push(value.getName(), value.getValue(), value.getPct() * 100);
            }
          }
          plot.plot();
          currentPlot = plot;
        }
      });
    }
  }

  private boolean isContinuous(VariableDto dto) {
    return isCategorical(dto) == false && dto.getValueType().equalsIgnoreCase("decimal") || dto.getValueType().equalsIgnoreCase("integer");
  }

  private boolean isCategorical(VariableDto dto) {
    return dto.getCategoriesArray() != null && dto.getCategoriesArray().length() > 0;
  }

  private AttributeDto findAttribute(String string, JsArray<AttributeDto> attrs) {
    if(attrs != null) {
      for(int i = 0; i < attrs.length(); i++) {
        AttributeDto attr = attrs.get(i);
        if(attr.getName().equals(string)) {
          return attr;
        }
      }
    }
    return null;
  }

  private String findAttributeValue(String string, VariableDto dto) {
    AttributeDto attr = findAttribute(string, dto.getAttributesArray());
    return attr != null ? attr.getValue() : "";
  }

  private String findAttributeValue(String string, CategoryDto dto) {
    AttributeDto attr = findAttribute(string, dto.getAttributesArray());
    return attr != null ? attr.getValue() : "";
  }

  private AttributeDto findAttribute(String string, VariableDto dto) {
    return findAttribute(string, dto.getAttributesArray());
  }

  private AttributeDto findAttribute(String string, CategoryDto dto) {
    return findAttribute(string, dto.getAttributesArray());
  }

  private void loadVariables(String tableUri) {
    request(tableUri + "/variables", new ResourceCallback<JsArray<VariableDto>>() {

      @Override
      public void onResource(Response response, JsArray<VariableDto> resource) {
        gotVariables(resource);
      }
    });
  }

  private void loadTables() {
    request("/datasources", new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
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
    widgets.evalResults().show();
    widgets.feedbackDialog().as(Ui).dialog().open();
  }

  private void fetchValues(int offset) {
    widgets.moreValues().data("offset", offset);
    widgets.values().children().remove();
    $("<span>Results: " + (offset + 1) + "-" + (offset + 10) + "</span>").appendTo(widgets.values());

    boolean partial = true;
    String js = scriptArea.getSelectedText();
    if(js == null || js.length() == 0) {
      partial = false;
      js = scriptArea.getText();
    }

    request(widgets.tableSelector().val() + "/eval?valueType=" + (partial ? "text" : widgets.valueType().val()) + "&offset=" + offset + "&script=" + URL.encodeQueryString(js), new ResourceCallback<JsArray<ValueDto>>() {

      @Override
      public void onResource(Response response, JsArray<ValueDto> resource) {
        gotValues(resource);
      }
    });
  }

  private void cancelPendingRequest() {
    if(currentRequest != null && currentRequest.isPending()) {
      currentRequest.cancel();
    }
  }

  private <T extends JavaScriptObject> void request(String uri, ResourceCallback<T> callback) {
    currentRequest = ResourceRequestBuilderFactory.<T> newBuilder().forResource(uri).get().withCallback(callback).send();
  }

}
