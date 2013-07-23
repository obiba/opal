package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;

public abstract class TabPanelTitle {

  protected Translations translations = GWT.create(Translations.class);

  protected final String title;

  protected TabPanelTitle(String title) {
    this.title = title;
  }

}
