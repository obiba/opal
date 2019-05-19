/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class LocalizedLabel extends Composite {

  interface LocalizedLabelUiBinder extends UiBinder<Widget, LocalizedLabel> {}

  private static final LocalizedLabelUiBinder uiBinder = GWT.create(LocalizedLabelUiBinder.class);

  @UiField
  Label locale;

  @UiField
  InlineLabel text;

  public LocalizedLabel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public LocalizedLabel(String locale, String text) {
    initWidget(uiBinder.createAndBindUi(this));
    this.locale.setText(locale);
    this.text.setText(text);
  }

  public void setLocale(String locale) {
    this.locale.setText(locale);
  }

  public void setText(String text) {
    this.text.setText(text);
  }

}
