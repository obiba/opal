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

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;

/**
 *
 */
public class ResizeHandle extends Label {

  private UIObject objectToResize;

  public ResizeHandle() {
    super();
    addStyleName("resizable-handle resizable-se");
  }

  public void makeResizable(UIObject objectToResize) {
    this.objectToResize = objectToResize;
    MouseResizeHandler handler = new MouseResizeHandler();
    addMouseDownHandler(handler);
    addMouseMoveHandler(handler);
    addMouseUpHandler(handler);
  }

  private class MouseResizeHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler {

    private boolean dragging = false;

    private int dragStartX;

    private int dragStartY;

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
      if(dragging) {
        int width = evt.getX() - dragStartX + objectToResize.getOffsetWidth();
        objectToResize.setWidth(width + "px");
        int height = evt.getY() - dragStartY + objectToResize.getOffsetHeight();
        objectToResize.setHeight(height + "px");
        // GWT.log("continue drag: height=" + height + " width=" + width);
      }
    }

    @Override
    public void onMouseUp(MouseUpEvent evt) {
      // GWT.log("end drag at x=" + evt.getX() + " y=" + evt.getY());
      dragging = false;
      DOM.releaseCapture(ResizeHandle.this.getElement());
    }

  }

}
