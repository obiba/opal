package org.obiba.opal.web.gwt.app.client.administration.taxonomies.git;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomyGitImportModalUiHandlers extends UiHandlers, ModalUiHandlers {
  void onImport(String name, String repository, String reference, String path);
}
