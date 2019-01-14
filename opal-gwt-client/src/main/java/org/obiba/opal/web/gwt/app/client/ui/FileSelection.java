/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.web.bindery.event.shared.EventBus;

public class FileSelection extends Composite implements TakesValue<String> {

  private static final Translations translations = GWT.create(Translations.class);

  private TextBox file;
  private Button browseButton;

  private final FileSelectorPresenter.FileSelectionType format;

  public FileSelection(EventBus eventBus, String format) {

    if (format == null || "file".equals(format)) {
      this.format = FileSelectorPresenter.FileSelectionType.FILE;
    } else {
      this.format = FileSelectorPresenter.FileSelectionType.FOLDER;
    }

    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    builder.appendHtmlConstant("<div></div>");
    HTMLPanel panel = new HTMLPanel(builder.toSafeHtml());
    HorizontalPanel horizontalPanel = new HorizontalPanel();

    file = new TextBox();
    file.setReadOnly(true);
    browseButton = new Button(translations.browseButtonLabel());
    browseButton.addStyleName("xsmall-indent");

    horizontalPanel.add(file);
    horizontalPanel.add(browseButton);
    panel.add(horizontalPanel);
    initWidget(panel);

    addEventHandlers(eventBus);
  }

  private void addEventHandlers(final EventBus eventBus) {
    browseButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new FileSelectionRequestEvent(FileSelection.this, format));
      }
    });

    eventBus.addHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {
      @Override
      public void onFileSelection(FileSelectionEvent event) {
        if(FileSelection.this.equals(event.getSource())) {
          file.setValue(event.getSelectedFile().getSelectionPath());
          eventBus.fireEvent(new FileSelectionUpdatedEvent(FileSelection.this));
        }
      }
    });
  }

  @Override
  public void setValue(String value) {
    file.setValue(value);
  }

  @Override
  public String getValue() {
    return file.getValue();
  }

}
