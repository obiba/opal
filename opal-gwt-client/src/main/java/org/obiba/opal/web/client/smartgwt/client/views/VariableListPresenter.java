package org.obiba.opal.web.client.smartgwt.client.views;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.client.smartgwt.client.smartgwt.data.fields.DataSourceFieldBuilder;
import org.obiba.opal.web.client.smartgwt.client.views.event.ValueTableChangedEvent;
import org.obiba.opal.web.client.smartgwt.client.views.event.ValueTableChangedHandler;
import org.obiba.opal.web.client.smartgwt.client.views.event.VariableChangedEvent;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.widgets.DataBoundComponent;
import com.smartgwt.client.widgets.grid.events.HasSelectionChangedHandlers;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;

public class VariableListPresenter extends WidgetPresenter<VariableListPresenter.Display> {

  public interface Display extends WidgetDisplay {

    public DataBoundComponent getVariableList();

    public HasSelectionChangedHandlers getVariableSelection();

  }

  public VariableListPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    eventBus.addHandler(ValueTableChangedEvent.getType(), new ValueTableChangedHandler() {

      public void onValueTableChanged(ValueTableChangedEvent event) {
        RestDataSource restDS = new RestDataSource();
        // restDS.setFetchDataURL("http://192.168.1.197:8080/jersey/datasource/opal-data/StandingHeight/variables");
        restDS.setFetchDataURL("/" + event.getValueTable().getAttribute("href"));
        restDS.setDataFormat(DSDataFormat.XML);
        restDS.setXmlRecordXPath("//variable");
        // TODO: reuse the fields instead of creating new objects
        restDS.setFields(DataSourceFieldBuilder.newField("name", FieldType.TEXT).title("Name").valueXPath("@name").build(), //
        DataSourceFieldBuilder.newField("type", FieldType.TEXT).title("Type").valueXPath("@valueType").build(), //
        DataSourceFieldBuilder.newField("unit", FieldType.TEXT).title("Units").valueXPath("@unit").build(), //
        DataSourceFieldBuilder.newField("label", FieldType.TEXT).title("Label").valueXPath("attributes/attribute[@name='label' and @locale='en']").build()/*
                                                                                                                                                           * ,/
                                                                                                                                                           * /
                                                                                                                                                           * DataSourceFieldBuilder
                                                                                                                                                           * .
                                                                                                                                                           * newField
                                                                                                                                                           * (
                                                                                                                                                           * "attributes"
                                                                                                                                                           * ,
                                                                                                                                                           * FieldType
                                                                                                                                                           * .
                                                                                                                                                           * TEXT
                                                                                                                                                           * )
                                                                                                                                                           * .
                                                                                                                                                           * title
                                                                                                                                                           * (
                                                                                                                                                           * "Attributes"
                                                                                                                                                           * )
                                                                                                                                                           * .
                                                                                                                                                           * children
                                                                                                                                                           * (
                                                                                                                                                           * )
                                                                                                                                                           * .
                                                                                                                                                           * valueXPath
                                                                                                                                                           * (
                                                                                                                                                           * "attributes/attribute/@name"
                                                                                                                                                           * )
                                                                                                                                                           * .
                                                                                                                                                           * build
                                                                                                                                                           * (
                                                                                                                                                           * )
                                                                                                                                                           */);

        getDisplay().getVariableList().setDataSource(restDS);
        getDisplay().getVariableList().fetchData();
      }
    });

    getDisplay().getVariableSelection().addSelectionChangedHandler(new SelectionChangedHandler() {

      public void onSelectionChanged(SelectionEvent event) {
        if(event.getState()) {
          Record variable = event.getRecord();
          variable.setAttribute("ds", getDisplay().getVariableList().getDataSource());
          eventBus.fireEvent(new VariableChangedEvent(variable));
        }
      }

    });
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  public void refreshDisplay() {
  }

  public void revealDisplay() {
  }

}
