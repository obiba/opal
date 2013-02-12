/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.ace.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AceEditor extends Widget implements HasText {

  @SuppressWarnings("StaticNonFinalField")
  private static int nextId = 0;

  private AceEditorWrapper editor;

  private HandlerRegistration attachHandlerRegistration;

  @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
  public AceEditor() {
    setElement(DOM.createDiv());
    getElement().setId("ace-editor-" + nextId++);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    editor = AceEditorWrapper.createEditor(getElement().getId());
  }

  @Override
  public String getText() {
    return editor.getValue();
  }

  @Override
  public void setText(final String text) {
    if(isAttached()) {
      editor.setValue(text);
      if(attachHandlerRegistration != null) {
        attachHandlerRegistration.removeHandler();
        attachHandlerRegistration = null;
      }
    } else {
      attachHandlerRegistration = addAttachHandler(new AttachEvent.Handler() {
        @Override
        public void onAttachOrDetach(AttachEvent event) {
          setText(text);
        }
      });
    }
  }

  public String getSelectedText() {
    return editor.getSelectedText();
  }
}
