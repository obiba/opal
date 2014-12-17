/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.ace.client.AceEditor;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.markdown.client.Markdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class MarkdownEditor extends Composite implements HasText {

  interface MarkdownEditorUiBinder extends UiBinder<Widget, MarkdownEditor> {}

  private static final MarkdownEditorUiBinder uiBinder = GWT.create(MarkdownEditorUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  AceEditor editor;

  @UiField
  Panel preview;

  @UiField
  ToggleAnchor toggle;

  public MarkdownEditor() {
    initWidget(uiBinder.createAndBindUi(this));

    toggle.setTexts(translations.previewLabel(), translations.editLabel());
    toggle.setDelegate(new ToggleAnchor.Delegate() {
      @Override
      public void executeOn() {
        togglePreview(true);
      }

      @Override
      public void executeOff() {
        togglePreview(false);
      }

      public void togglePreview(boolean visible) {
        preview.setVisible(visible);
        editor.setVisible(!visible);
        preview.clear();
        if(visible) {
          preview.add(new HTMLPanel(Markdown.parse(getText())));
        }
      }
    });
  }

  @Override
  public String getText() {
    return editor.getText();
  }

  @Override
  public void setText(String text) {
    editor.setText(text);
  }

  public void showPreview(boolean visible) {
    toggle.setOn(true, true);
  }
}
