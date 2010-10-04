/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;

import com.google.inject.Inject;

/**
 *
 */
public class ExcelDatasourceFormPresenter extends WidgetPresenter<ExcelDatasourceFormPresenter.Display> implements DatasourceFormPresenter {
  //
  // Instance Variables
  //

  @Inject
  private FileSelectionPresenter fileSelectionPresenter;

  //
  // Constructors
  //

  @Inject
  public ExcelDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.bind();
    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    getDisplay().setFileSelectorWidgetDisplay(fileSelectionPresenter.getDisplay());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onUnbind() {
    // fileSelectionPresenter.unbind();
  }

  @Override
  public DatasourceFactoryDto getDatasourceFactory() {
    ExcelDatasourceFactoryDto extensionDto = ExcelDatasourceFactoryDto.create();
    extensionDto.setFile(fileSelectionPresenter.getSelectedFile());
    extensionDto.setReadOnly(false);

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(ExcelDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  @Override
  public boolean isForType(String type) {
    return type.equalsIgnoreCase("Excel");
  }

  @Override
  public void refreshDisplay() {
    // TODO Auto-generated method stub

  }

  @Override
  public void revealDisplay() {
    // TODO Auto-generated method stub

  }

  //
  // Interfaces and Inner Classes
  //

  public interface Display extends DatasourceFormPresenter.Display {

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

  }

  @Override
  public boolean validate() {
    boolean isValid = true;
    String file = fileSelectionPresenter.getSelectedFile();

    if(file.length() == 0) {
      isValid = false;
      fireErrorEvent("ExcelFileRequired");
    } else if(!file.endsWith(".xls") && !file.endsWith(".xlsx")) {
      isValid = false;
      fireErrorEvent("ExcelFileSuffixInvalid");
    }

    return isValid;
  }

  private void fireErrorEvent(String error) {
    eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, error, null));
  }
}
