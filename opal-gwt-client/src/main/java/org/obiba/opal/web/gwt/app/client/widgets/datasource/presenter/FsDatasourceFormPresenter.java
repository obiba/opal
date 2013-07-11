/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.FsDatasourceFactoryDto;

import com.google.web.bindery.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class FsDatasourceFormPresenter extends PresenterWidget<FsDatasourceFormPresenter.Display>
    implements DatasourceFormPresenter {

  public static class Subscriber extends DatasourceFormPresenterSubscriber {

    @Inject
    public Subscriber(EventBus eventBus, FsDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  private final FileSelectionPresenter fileSelectionPresenter;

  @Inject
  public FsDatasourceFormPresenter(Display display, EventBus eventBus, FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenter;
  }

  @Override
  public PresenterWidget<? extends DatasourceFormPresenter.Display> getPresenter() {
    return this;
  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.bind();
    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    getView().setFileSelectorWidgetDisplay(fileSelectionPresenter.getView());
  }

  @Override
  public DatasourceFactoryDto getDatasourceFactory() {
    FsDatasourceFactoryDto extensionDto = FsDatasourceFactoryDto.create();
    extensionDto.setFile(fileSelectionPresenter.getSelectedFile());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(FsDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  @Override
  public boolean isForType(String type) {
    return "fs".equalsIgnoreCase(type);
  }

  public interface Display extends DatasourceFormPresenter.Display {

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

  }

  private void fireErrorEvent(String error) {
    getEventBus().fireEvent(NotificationEvent.newBuilder().error(error).build());
  }

  @Override
  public boolean validateFormData() {
    boolean isValid = true;
    String file = fileSelectionPresenter.getSelectedFile();

    if(file.isEmpty()) {
      isValid = false;
      fireErrorEvent("ZipFileRequired");
    } else if(!file.endsWith(".zip")) {
      isValid = false;
      fireErrorEvent("ZipFileSuffixInvalid");
    }

    return isValid;
  }

  @Override
  public void clearForm() {
    fileSelectionPresenter.getView().setFile("");
  }
}
