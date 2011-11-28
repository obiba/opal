/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEvaluationPopupPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.SimplePanel;

public class ScriptEvaluationPopupView extends DialogBox implements Display {

  private Translations translations = GWT.create(Translations.class);

  private static final String DIALOG_HEIGHT = "15em";

  private static final String DIALOG_WIDTH = "30em";

  public ScriptEvaluationPopupView() {
    setHeight(DIALOG_HEIGHT);
    setWidth(DIALOG_WIDTH);

    SimplePanel content = new SimplePanel();
    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);

    Button closeButton = createCloseButton();

    content.add(closeButton);
    add(content);
  }

  private Button createCloseButton() {
    Button closeButton = new Button(translations.closeLabel());
    closeButton.addStyleName("btn");
    closeButton.addClickHandler(new CloseHandler());
    return closeButton;
  }

  class CloseHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      hide();
    }
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void showDialog() {
    center();
    show();
  }

  @Override
  public void setScript(String script) {
    GWT.log(script);
  }

}
