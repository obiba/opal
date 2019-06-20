package org.obiba.opal.web.gwt.app.client.magma.datasource.presenter;

import elemental.html.File;
import java.util.List;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface RestoreViewsUiHandlers extends ModalUiHandlers {

  void cancel();

  void onSubmitFiles(List<File> files);
}
