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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class VariablesTemplateDownloadPanel extends Composite implements HasClickHandlers {

  private final Paragraph paragraph;
  private final Button downloadButton;

  public VariablesTemplateDownloadPanel() {
    final Translations translations = GWT.create(Translations.class);

    FlowPanel layout = new FlowPanel();
    paragraph = createParagraph(translations.variablesTemplateDownloadLabel());
    downloadButton = createButton(translations.variablesTemplateButtonLabel());

    layout.add(paragraph);
    layout.add(downloadButton);
    initWidget(layout);
  }

  /**
   * Called from UiBinder, this is the main paragraph widget serving as the title of the panel
   * @param text
   * @return Paragraph
   */
  private Paragraph createParagraph(String text) {
    Paragraph p = new Paragraph(text);
    p.addStyleName("small-bottom-margin");
    return p;
  }

  private Button createButton(String caption) {
    Button b = new Button(caption);
    b.setIcon(IconType.DOWNLOAD);
    b.setType(ButtonType.INFO);
    b.addStyleName("large-bottom-margin");
    return b;
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    assert downloadButton != null;
    return downloadButton.addClickHandler(handler);
  }
}
