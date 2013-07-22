package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MagmaPresenter extends PresenterWidget<MagmaPresenter.Display>
    implements MagmaUiHandlers, DatasourceSelectionChangeEvent.Handler, TableSelectionChangeEvent.Handler,
    VariableSelectionChangeEvent.Handler {

  private final DatasourcePresenter datasourcePresenter;

  private final TablePresenter tablePresenter;

  private final VariablePresenter variablePresenter;

  @Inject
  public MagmaPresenter(EventBus eventBus, Display display, DatasourcePresenter datasourcePresenter,
      TablePresenter tablePresenter, VariablePresenter variablePresenter) {
    super(eventBus, display);
    this.datasourcePresenter = datasourcePresenter;
    this.tablePresenter = tablePresenter;
    this.variablePresenter = variablePresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();

    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(), this);
    addRegisteredHandler(TableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);

    setInSlot(Display.Slot.DATASOURCE, datasourcePresenter);
    setInSlot(Display.Slot.TABLE, tablePresenter);
    setInSlot(Display.Slot.VARIABLE, variablePresenter);
  }

  @Override
  public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
    getView().selectDatasource(event.getSelection());
  }

  @Override
  public void onTableSelectionChanged(TableSelectionChangeEvent event) {
    getView().selectTable(event.getDatasourceName(), event.getTableName());
  }

  @Override
  public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
    getView().selectVariable(event.getDatasourceName(), event.getTableName(), event.getVariableName());
  }

  @Override
  public void onDatasourceSelection(String name) {
    getEventBus().fireEvent(new DatasourceSelectionChangeEvent(this, name));
  }

  @Override
  public void onTableSelection(String datasource, String table) {
    getEventBus().fireEvent(new TableSelectionChangeEvent(this, datasource, table));
  }

  public interface Display extends View, HasUiHandlers<MagmaUiHandlers> {

    enum Slot {
      DATASOURCE,
      TABLE,
      VARIABLE
    }

    void selectDatasource(String name);

    void selectTable(String datasource, String table);

    void selectVariable(String datasource, String table, String variable);
  }

}
