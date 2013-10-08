package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;

public class RequiredIcon extends Icon {

  private static final Translations translations = GWT.create(Translations.class);

  public RequiredIcon() {
    super(IconType.ASTERISK);
    addStyleName("icon-required");
    setTitle(translations.required());
  }

}
