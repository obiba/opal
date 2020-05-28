/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class WizardModalBox extends Modal {

  private static final Translations translations = GWT.create(Translations.class);

  private SimplePanel step;

  private Button finish;

  private Button next;

  private Button previous;

  private Button cancel;

  private Button close;

  public WizardModalBox() {
    initWidget();
  }

  private void initWidget() {
    // main content
    super.add(step = new SimplePanel());
    step.setHeight("25em");
    //step.setWidth("30em");
    // controls
    initControls();
  }

  private void initControls() {
    ModalFooter footer = new ModalFooter();
    super.add(footer);
    initNavigationControls(footer);
  }

  private void initNavigationControls(ModalFooter footer) {
    FlowPanel group = new FlowPanel();
    group.addStyleName("pull-right");
    group.add(previous = new Button(translations.previousLabel()));
    group.add(next = new Button(translations.nextLabel()));
    group.add(finish = new Button(translations.finishLabel()));
    group.add(close = new Button(translations.closeLabel()));
    group.add(cancel = new Button(translations.cancelLabel()));
    cancel.setType(ButtonType.DEFAULT);
    close.setType(ButtonType.PRIMARY);
    finish.setType(ButtonType.PRIMARY);
    next.setType(ButtonType.INFO);
    previous.setType(ButtonType.INFO);
    footer.add(group);

    setPreviousEnabled(false);
  }

  public void setFinishEnabled(boolean enabled) {
    finish.setEnabled(enabled);
  }

  public void setNextEnabled(boolean enabled) {
    next.setEnabled(enabled);
  }

  public void setPreviousEnabled(boolean enabled) {
    previous.setEnabled(enabled);
  }

  public void setCancelEnabled(boolean enabled) {
    cancel.setEnabled(enabled);
  }

  public void setCloseEnabled(boolean enabled) {
    close.setEnabled(enabled);
  }

  public void setCloseVisible(boolean visible) {
    close.setVisible(visible);
    finish.setVisible(!visible);
    next.setVisible(!visible);
    previous.setVisible(!visible);
  }

  public void setWidget(Widget w) {
    step.clear();
    w.removeFromParent();
    step.setWidget(w);
  }

  @Override
  public void add(Widget w) {
    if(step == null) {
      // before step container is defined = header
      super.add(w);
    } else {
      setWidget(w);
    }
  }

  public HandlerRegistration addPreviousClickHandler(ClickHandler handler) {
    return previous.addClickHandler(handler);
  }

  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return finish.addClickHandler(handler);
  }

  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return cancel.addClickHandler(handler);
  }

  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return next.addClickHandler(handler);
  }

  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return close.addClickHandler(handler);
  }

  public void setProgress(boolean progress) {
    if(progress) {
      addStyleName("modal-progress");
    } else {
      removeStyleName("modal-progress");
    }
  }
}
