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

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ResizeHandle extends Widget
    implements HasText, HasMouseDownHandlers, HasMouseUpHandlers, HasMouseMoveHandlers {

  public enum Direction {
    SOUTH_EAST, SOUTH
  }

  private Direction direction;

  public ResizeHandle() {
    this(Direction.SOUTH_EAST);
  }

  public ResizeHandle(Direction direction) {
    super();
    setElement(Document.get().createDivElement());
    addStyleName("resizable-handle");
    setResizeDirection(direction);
  }

  public void setResizeDirection(Direction direction) {
    this.direction = direction;
    removeStyleName("resizable-se");
    removeStyleName("resizable-s");
    switch(direction) {
      case SOUTH_EAST:
        addStyleName("resizable-se");
        break;
      case SOUTH:
        addStyleName("resizable-s");
        break;
    }
  }

  public void makeResizable(UIObject objectToResize) {
    makeResizable(objectToResize, 0, 0);
  }

  public void makeResizable(UIObject objectToResize, int minWidth, int minHeight) {
    MouseResizeHandler handler = new MouseResizeHandler(objectToResize, minWidth, minHeight);
    addMouseDownHandler(handler);
    addMouseMoveHandler(handler);
    addMouseUpHandler(handler);
  }

  private class MouseResizeHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler {

    private UIObject objectToResize;

    private boolean dragging = false;

    private int dragStartX;

    private int dragStartY;

    private int minWidth;

    private int minHeight;

    public MouseResizeHandler(UIObject objectToResize, int minWidth, int minHeight) {
      super();
      this.objectToResize = objectToResize;
      this.minWidth = minWidth;
      this.minHeight = minHeight;
    }

    @Override
    public void onMouseDown(MouseDownEvent evt) {
      // GWT.log("begin drag at x=" + evt.getX() + " y=" + evt.getY());
      dragging = true;
      DOM.setCapture(ResizeHandle.this.getElement());
      dragStartX = evt.getX();
      dragStartY = evt.getY();
    }

    @Override
    public void onMouseMove(MouseMoveEvent evt) {
      if(!dragging) return;

      int height = evt.getY() - dragStartY + objectToResize.getOffsetHeight();
      if(height >= minHeight) objectToResize.setHeight(height + "px");

      if(direction.equals(Direction.SOUTH_EAST)) {
        int width = evt.getX() - dragStartX + objectToResize.getOffsetWidth();
        if(width >= minWidth) objectToResize.setWidth(width + "px");
      }
      // GWT.log("continue drag: height=" + height + " width=" + width);
    }

    @Override
    public void onMouseUp(MouseUpEvent evt) {
      // GWT.log("end drag at x=" + evt.getX() + " y=" + evt.getY());
      dragging = false;
      DOM.releaseCapture(ResizeHandle.this.getElement());
    }

  }

  @Override
  public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
    return addDomHandler(handler, MouseDownEvent.getType());
  }

  @Override
  public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
    return addDomHandler(handler, MouseUpEvent.getType());
  }

  @Override
  public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
    return addDomHandler(handler, MouseMoveEvent.getType());
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }

}
