package org.obiba.opal.web.client.smartgwt.client.views;

import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.widgets.DataBoundComponent;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.HasSelectionChangedHandlers;

public class VariableListView implements VariableListPresenter.Display {

  private final ListGrid grid;

  public VariableListView() {
    grid = new ListGrid();
    grid.setWidth100();
    grid.setHeight(500);
    grid.setAutoFetchData(true);
  }

  public DataBoundComponent getVariableList() {
    return grid;
  }

  public HasSelectionChangedHandlers getVariableSelection() {
    return grid;
  }

  public Widget asWidget() {
    return grid;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

}
