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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
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

  public void makeResizable(UIObject objectToResize, int minHeight) {
    makeResizable(objectToResize, -1, minHeight);
  }

  public void makeResizable(UIObject objectToResize, int minWidth, int minHeight) {
    MouseResizeHandler handler = new MouseResizeHandler(objectToResize, minWidth, minHeight);
    addMouseDownHandler(handler);
    addMouseMoveHandler(handler);
    addMouseUpHandler(handler);
  }

  private class MouseResizeHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler {

    private final UIObject objectToResize;

    private boolean dragging = false;

    private int dragStartX;

    private int dragStartY;

    private final int minWidth;

    private final int minHeight;

    private MouseResizeHandler(UIObject objectToResize, int minWidth, int minHeight) {
      this.objectToResize = objectToResize;
      this.minWidth = minWidth;
      this.minHeight = minHeight;
    }

    private int getX(MouseEvent<?> evt) {
      return evt.getClientX();
    }

    private int getY(MouseEvent<?> evt) {
      return evt.getClientY();
    }

    @Override
    public void onMouseDown(MouseDownEvent evt) {
      dragging = true;
      DOM.setCapture(getElement());
      dragStartX = getX(evt);
      dragStartY = getY(evt);
      //GWT.log("begin drag at x=" + dragStartX + " y=" + dragStartY);
    }

    @Override
    public void onMouseMove(MouseMoveEvent evt) {
      if(!dragging) return;
      //GWT.log("continue drag at x=" + getY(evt) + " y=" + getY(evt));

      //GWT.log("  client.height=" + objectToResize.getElement().getClientHeight());
      int height = getY(evt) - dragStartY + objectToResize.getElement().getClientHeight();
      if(height >= minHeight) {
        objectToResize.setHeight(height + "px");
      }

      int width = getX(evt) - dragStartX + objectToResize.getElement().getClientWidth();
      if(minWidth >= 0 && direction == Direction.SOUTH_EAST) {
        if(width >= minWidth) objectToResize.setWidth(width + "px");
      }
      //GWT.log("  height=" + height + " width=" + width);
      dragStartX = getX(evt);
      dragStartY = getY(evt);
    }

    @Override
    public void onMouseUp(MouseUpEvent evt) {
      //GWT.log("end drag at x=" + getY(evt) + " y=" + getY(evt));
      dragging = false;
      DOM.releaseCapture(getElement());
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
