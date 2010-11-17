/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class WizardDialogBox extends DialogBox {

  private DockLayoutPanel contentLayout;

  private SimplePanel step;

  private Button finish;

  private Button next;

  private Button previous;

  private Button cancel;

  private FocusPanel resizeHandle;

  private static Translations translations = GWT.create(Translations.class);

  /**
   * 
   */
  public WizardDialogBox() {
    super();
    initWidget();
  }

  /**
   * @param autoHide
   * @param modal
   */
  public WizardDialogBox(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    initWidget();
  }

  public void setAutoHide(boolean autoHide) {
    super.setAutoHideEnabled(autoHide);
  }

  /**
   * @param autoHide
   */
  public WizardDialogBox(boolean autoHide) {
    super(autoHide);
    initWidget();
  }

  private void initWidget() {
    addStyleName("wizard");

    super.setWidget(contentLayout = new DockLayoutPanel(Unit.EM));
    setSize("45em", "22em");

    // controls
    FlowPanel south;
    contentLayout.addSouth(south = new FlowPanel(), 3.25);
    south.addStyleName("footer");

    south.add(resizeHandle = new FocusPanel());
    resizeHandle.addStyleName("resizable-handle resizable-se");
    ResizableHandler handler = new ResizableHandler();
    resizeHandle.addMouseDownHandler(handler);
    resizeHandle.addMouseMoveHandler(handler);
    resizeHandle.addMouseUpHandler(handler);

    south.add(cancel = new Button("Cancel"));
    initControlStyle(cancel, "cancel");
    south.add(finish = new Button("Finish"));
    initControlStyle(finish, "finish");
    south.add(next = new Button("Next >"));
    initControlStyle(next, "next");
    south.add(previous = new Button("< Previous"));
    initControlStyle(previous, "previous");
    setPreviousEnabled(false);

    // main content
    ScrollPanel scroll = new ScrollPanel();
    contentLayout.add(scroll);
    scroll.setWidget(step = new SimplePanel());
  }

  private void initControlStyle(Button button, String style) {
    button.addStyleName(style);
    button.addStyleName("right-aligned");
    button.addStyleName("small-top-margin");
  }

  public void setFinishVisible(boolean visible) {
    finish.setVisible(visible);
  }

  public void setNextVisible(boolean visible) {
    next.setVisible(visible);
  }

  public void setPreviousVisible(boolean visible) {
    previous.setVisible(visible);
  }

  public void setCancelVisible(boolean visible) {
    cancel.setVisible(visible);
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

  @Override
  public void setSize(String width, String height) {
    contentLayout.setSize(width, height);
  }

  @Override
  public void setWidth(String width) {
    contentLayout.setWidth(width);
  }

  @Override
  public void setHeight(String height) {
    contentLayout.setHeight(height);
  }

  public void setStep(Widget w) {
    this.setWidget(w);
  }

  @Override
  public void setWidget(Widget w) {
    w.addStyleName("main");
    step.setWidget(w);
  }

  @Override
  public void add(Widget w) {
    this.setWidget(w);
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

  private class ResizableHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler {

    private boolean dragging = false;

    private int dragStartX;

    private int dragStartY;

    @Override
    public void onMouseDown(MouseDownEvent evt) {
      // GWT.log("begin drag at x=" + evt.getX() + " y=" + evt.getY());
      dragging = true;
      DOM.setCapture(resizeHandle.getElement());
      dragStartX = evt.getX();
      dragStartY = evt.getY();
    }

    @Override
    public void onMouseMove(MouseMoveEvent evt) {
      if(dragging) {
        int width = evt.getX() - dragStartX + contentLayout.getOffsetWidth();
        contentLayout.setWidth(width + "px");
        int height = evt.getY() - dragStartY + contentLayout.getOffsetHeight();
        contentLayout.setHeight(height + "px");
        // GWT.log("continue drag: height=" + height + " width=" + width);
      }
    }

    @Override
    public void onMouseUp(MouseUpEvent evt) {
      // GWT.log("end drag at x=" + evt.getX() + " y=" + evt.getY());
      dragging = false;
      DOM.releaseCapture(resizeHandle.getElement());
    }

  }

}
