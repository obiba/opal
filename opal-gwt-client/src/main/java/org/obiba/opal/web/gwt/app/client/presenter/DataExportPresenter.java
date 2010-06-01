/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.CopyCommandOptionsDto;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.FunctionalUnitDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;
import com.google.inject.Inject;

public class DataExportPresenter extends WidgetPresenter<DataExportPresenter.Display> {

  public interface Display extends DataCommonPresenter.Display {

    HasSelectionHandlers<TreeItem> getTableTree();

    void setItems(List<TreeItem> items);

    SelectionModel<VariableDto> getTableSelection();

    void addTable(String datasource, String table);

    HasValue<String> getFile();

    RadioButton getDestinationFile();

    void hideDialog();

    JsArrayString getSelectedFiles();

    String getOutFile();

    HasValue<Boolean> isIncremental();

    HasValue<Boolean> isWithVariables();

    HasValue<Boolean> isUseAlias();

    HasValue<Boolean> isUnitId();

    HasValue<Boolean> isDestinationDataSource();

  }

  final private ResourceRequestBuilderFactory factory;

  @Inject
  private ErrorDialogPresenter errorDialog;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public DataExportPresenter(Display display, EventBus eventBus, final ResourceRequestBuilderFactory factory) {
    super(display, eventBus);
    this.factory = factory;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
  }

  protected void addEventHandlers() {

    super.registerHandler(getDisplay().getTableTree().addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        eventBus.fireEvent(new NavigatorSelectionChangeEvent(event.getSelectedItem()));
      }
    }));

    getDisplay().getTableSelection().addSelectionChangeHandler(new SelectionChangeHandler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        eventBus.fireEvent(new VariableSelectionChangeEvent(null));
      }
    });

    super.registerHandler(eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionChangeEvent.Handler() {
      @Override
      public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
        if(event.getSelection().getParentItem() != null) {
          String datasource = event.getSelection().getParentItem().getText();
          String table = event.getSelection().getText();
          getDisplay().addTable(datasource, table);
        }
      }
    }));

    getDisplay().getSubmit().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        List<String> errors = formValidationErrors();
        if(!errors.isEmpty()) {
          errorDialog.bind();
          errorDialog.setErrors(errors);
          errorDialog.revealDisplay();
        } else {
          CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();
          dto.setTablesArray(getDisplay().getSelectedFiles());
          if(getDisplay().isDestinationDataSource().getValue()) {
            dto.setDestination(getDisplay().getSelectedDatasource());
          } else {
            dto.setOut(getDisplay().getOutFile());
          }
          dto.setNonIncremental(!getDisplay().isIncremental().getValue());
          dto.setNoVariables(!getDisplay().isWithVariables().getValue());
          if(getDisplay().isUseAlias().getValue()) dto.setTransform("attribute('alias').isNull().value ? name() : attribute('alias')");
          if(getDisplay().isUnitId().getValue()) dto.setUnit(getDisplay().getSelectedUnit());
          factory.newBuilder().forResource("/shell/copy").post().withResourceBody(CopyCommandOptionsDto.stringify(dto)).withCallback(400, new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              errorDialog.bind();
              errorDialog.setErrors(Arrays.asList(new String[] { response.getText() }));
              errorDialog.revealDisplay();
            }
          }).withCallback(202, new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              getDisplay().hideDialog();
            }
          }).send();

        }
      }
    });
  }

  protected void initDisplayComponents() {
    factory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        getDisplay().setDatasources(datasources);
      }
    }).send();

    factory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getDisplay().setUnits(units);
      }
    }).send();
  }

  private List<String> formValidationErrors() {
    List<String> result = new ArrayList<String>();
    if(getDisplay().getSelectedFiles().length() == 0) {
      result.add("Must select at least one table for export");
    }
    if(getDisplay().getDestinationFile().getValue()) {
      String filename = getDisplay().getFile().getValue();
      if(filename == null || filename.equals("")) {
        result.add("filename cannot be empty");
      } else {
        if(!(filename.endsWith(".xls") || filename.endsWith(".xlsx") || filename.endsWith(".xml"))) {
          result.add("filename must end with .xls, .xlsx or .xml");
        }
      }
    }
    return result;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
    updateTree();
  }

  private void updateTree() {
    factory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        ArrayList<TreeItem> items = new ArrayList<TreeItem>(datasources.length());
        for(int i = 0; i < datasources.length(); i++) {
          DatasourceDto ds = datasources.get(i);
          TreeItem dsItem = new TreeItem(ds.getName());
          dsItem.setUserObject(ds);
          JsArrayString array = ds.getTableArray();
          for(int j = 0; j < array.length(); j++) {
            array.get(j);
            dsItem.addItem(array.get(j));
          }
          items.add(dsItem);
        }
        getDisplay().setItems(items);
      }
    }).send();
  }

}
