/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.constants.ToggleType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class EditorPanel extends FlowPanel {

  private static final Translations translations = GWT.create(Translations.class);

  private final FlowPanel controls;

  private final Button edit;

  private final Button save;

  private final Button cancel;

  private final DeckPanel deck;

  private Button history;

  private Handler handler;

  public EditorPanel() {
    controls = new FlowPanel();
    edit = new Button(translations.editLabel());
    save = new Button(translations.saveLabel());
    cancel = new Button(translations.cancelLabel());
    initControls();
    super.add(deck = new DeckPanel());
  }

  private void initControls() {
    controls.addStyleName("bottom-margin");
    edit.setIcon(IconType.EDIT);
    edit.setType(ButtonType.INFO);
    edit.addStyleName("small-right-indent");
    edit.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showEditor(true);
        if(handler != null) handler.onEdit();
      }
    });
    controls.add(edit);

    save.setType(ButtonType.PRIMARY);
    save.addStyleName("small-right-indent");
    save.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showEditor(false);
        if(handler != null) handler.onSave();
      }
    });
    save.setVisible(false);
    controls.add(save);

    cancel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showEditor(false);
        if(handler != null) handler.onCancel();
      }
    });
    cancel.setVisible(false);
    controls.add(cancel);

    super.add(controls);
  }

  @Override
  public void add(IsWidget child) {
    deck.add(child);
    initDeck();
  }

  @Override
  public void add(Widget w) {
    deck.add(w);
    initDeck();
  }

  private void initDeck() {
    deck.showWidget(0);
    if(deck.getWidgetCount() == 3) {
      history = new Button(translations.historyLabel());
      //history.setType(ButtonType.LINK);
      history.setSize(edit.getSize());
      history.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean visible = deck.getVisibleWidget() == 2;
          if(!visible && handler != null) handler.onHistory();
          showHistory(!visible);
        }
      });
      controls.add(history);
    }
  }

  /**
   * Set the control buttons size.
   *
   * @param size
   */
  public void setSize(ButtonSize size) {
    edit.setSize(size);
    save.setSize(size);
    cancel.setSize(size);
    if (history != null) {
      history.setSize(size);
    }
  }

  public void setHandler(Handler handler) {
    this.handler = handler;
  }

  public void showEditor(boolean visible) {
    edit.setVisible(!visible);
    save.setVisible(visible);
    cancel.setVisible(visible);
    deck.showWidget(visible ? 1 : 0);
    if(history != null) {
      history.setVisible(!visible);
      history.setText(translations.historyLabel());
    }
  }

  public void showHistory(boolean visible) {
    edit.setVisible(true);
    save.setVisible(false);
    cancel.setVisible(false);
    deck.showWidget(visible ? 2 : 0);
    if(visible) {
      history.setText(translations.viewLabel());
    } else {
      history.setText(translations.historyLabel());
    }
  }

  public HasAuthorization getAuthorizer() {
    return new WidgetAuthorizer(edit);
  }

  public interface Handler {

    void onEdit();

    void onSave();

    void onCancel();

    void onHistory();
  }
}
