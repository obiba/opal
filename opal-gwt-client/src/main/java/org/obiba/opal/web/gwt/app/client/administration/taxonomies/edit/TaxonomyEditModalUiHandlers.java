package org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;

import com.google.gwt.core.client.JsArray;
import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomyEditModalUiHandlers extends UiHandlers, ModalUiHandlers {

  void onSave(String name, String author, String license, JsArray<LocaleTextDto> titles, JsArray<LocaleTextDto> descriptions);

}
