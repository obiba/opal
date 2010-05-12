package org.obiba.opal.web.client.smartgwt.client.views;

import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.grid.ListGrid;

public class VariableDetailView implements VariableDetailPresenter.Display {

  private DynamicForm variableDetails = new DynamicForm();

  private ListGrid attributes = new ListGrid();

  public VariableDetailView() {
    variableDetails.setIsGroup(true);
    variableDetails.setGroupTitle("Edit Variable");
    // variableDetails.setFields(new TextItem("name"), new TextItem("valueType"), new TextItem("unit"));
    // variableDetails.setu
  }

  @Override
  public DynamicForm getVariableForm() {
    return variableDetails;
  }

  @Override
  public Widget asWidget() {
    return variableDetails;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

}
