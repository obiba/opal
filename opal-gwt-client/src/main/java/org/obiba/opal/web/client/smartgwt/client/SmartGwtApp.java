package org.obiba.opal.web.client.smartgwt.client;

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.client.smartgwt.client.views.VariableDetailPresenter;
import org.obiba.opal.web.client.smartgwt.client.views.VariableDetailView;
import org.obiba.opal.web.client.smartgwt.client.views.VariableListPresenter;
import org.obiba.opal.web.client.smartgwt.client.views.VariableListView;
import org.obiba.opal.web.client.smartgwt.client.views.event.ValueTableChangedEvent;
import org.obiba.opal.web.client.smartgwt.client.views.event.VariableChangedEvent;
import org.obiba.opal.web.client.smartgwt.client.views.event.VariableChangedHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SmartGwtApp implements EntryPoint {

  private final EventBus eventBus = new DefaultEventBus();

  @Override
  public void onModuleLoad() {
    VLayout layout = new VLayout();
    layout.setWidth100();
    layout.setHeight100();

    Button v1 = new Button("Variables 1");
    v1.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        Record r = new Record();
        r.setAttribute("href", "variables.xml");
        eventBus.fireEvent(new ValueTableChangedEvent(r));
      }
    });
    Button v2 = new Button("variables2.xml");
    v2.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        Record r = new Record();
        r.setAttribute("href", "variables2.xml");
        eventBus.fireEvent(new ValueTableChangedEvent(r));
      }
    });

    layout.addMember(v1);
    layout.addMember(v2);

    VariableListPresenter presenter = new VariableListPresenter(new VariableListView(), eventBus);
    presenter.bind();
    layout.addMember(presenter.getDisplay().asWidget());

    VariableDetailPresenter details = new VariableDetailPresenter(new VariableDetailView(), eventBus);
    details.bind();
    layout.addMember(details.getDisplay().asWidget());

    eventBus.addHandler(VariableChangedEvent.getType(), new VariableChangedHandler() {

      public void onVariableChanged(VariableChangedEvent event) {
        for(String attr : event.getVariable().getAttributes()) {
          GWT.log(attr + ": " + event.getVariable().getAttribute(attr));
        }
      }
    });
    RootPanel.get().add(layout);
  }
}
