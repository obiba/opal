package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Bootstrap Modal, resizable and draggable.
 */
public class Modal extends com.github.gwtbootstrap.client.ui.Modal {

  private boolean resizable = false;

  private boolean draggable;

  private boolean resize = false;

  private boolean move = false;

  private int moveStartX;

  private int moveStartY;

  private Widget movePanel;

  public Modal() {
    this(false);
  }

  public Modal(boolean animated) {
    this(animated, true);
  }

  public Modal(boolean animated, boolean dynamicSafe) {
    super(animated, dynamicSafe);
    sinkMouseEvents();
    // modal-header is the drag handle
    setMovePanel(getWidget(0));
    setAutoHide(false);
    setDraggable(true);
    setResizable(false);
  }

  public void setResizable(boolean resizable) {
    this.resizable = resizable;
  }

  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
  }

  public void setAutoHide(boolean autoHide) {
    if (autoHide) {
      setBackdrop(BackdropType.NORMAL);
    } else {
      setBackdrop(BackdropType.STATIC);
    }
  }

  private void sinkMouseEvents() {
    //listen to mouse-events
    DOM.sinkEvents(getElement(), Event.ONMOUSEDOWN |
        Event.ONMOUSEMOVE |
        Event.ONMOUSEUP |
        Event.ONMOUSEOVER);
  }

  /**
   * processes the mouse-events to show cursor or change states
   * - mouseover
   * - mousedown
   * - mouseup
   * - mousemove
   */
  @Override
  public void onBrowserEvent(Event event) {
    switch(DOM.eventGetType(event)) {
      case Event.ONMOUSEOVER:
        onMouseOverEvent(event);
        break;
      case Event.ONMOUSEDOWN:
        onMouseDownEvent(event);
        break;
      case Event.ONMOUSEMOVE:
        onMouseMoveEvent(event);
        break;
      case Event.ONMOUSEUP:
        onMouseUpEvent(event);
        break;
    }
  }

  private void onMouseOverEvent(Event event) {
    //show different cursors
    if(resizable && isCursorResize(event)) {
      DOM.setStyleAttribute(getElement(), "cursor", "se-resize");
    } else if(draggable && isCursorMove(event)) {
      DOM.setStyleAttribute(movePanel.getElement(), "cursor", "move");
    } else {
      DOM.setStyleAttribute(getElement(), "cursor", "default");
    }
  }

  private void onMouseDownEvent(Event event) {
    if(resizable && isCursorResize(event)) {
      //enable/disable resize
      if(!resize) {
        resize = true;
        DOM.setCapture(getElement());
      }
    } else if(draggable && isCursorMove(event)) {
      DOM.setCapture(getElement());
      move = true;
      moveStartX = DOM.eventGetClientX(event);
      moveStartY = DOM.eventGetClientY(event);
    }
  }

  private void onMouseMoveEvent(Event event) {
    //reset cursor-type
    if(!isCursorResize(event) && !isCursorMove(event)) {
      DOM.setStyleAttribute(getElement(), "cursor", "default");
    }

    //calculate and set the new size or move

    if(resize) {
      doResize(event);
    } else if(move) {
      doMove(event);
    }
  }

  private void doResize(Event event) {
    int absX = DOM.eventGetClientX(event);
    int absY = DOM.eventGetClientY(event);
    int originalX = getAbsoluteLeft();
    int originalY = getAbsoluteTop();
    //do not allow mirror-functionality
    if(absY > originalY && absX > originalX) {
      Integer height = absY - originalY + 2;
      setHeight(height + "px");

      Integer width = absX - originalX + 2;
      setWidth(width + "px");
    }
  }

  private void doMove(Event event) {
    int absX = DOM.eventGetClientX(event);
    int absY = DOM.eventGetClientY(event);
    int originalX = getAbsoluteLeft();
    int originalY = getAbsoluteTop();

    int deltaX = absX - moveStartX;
    int deltaY = absY - moveStartY;
    // prepare for next move
    moveStartX = absX;
    moveStartY = absY;
    // no move detected
    if(deltaX == 0 && deltaY == 0) return;
    int newX = originalX + deltaX;
    int newY = originalY + deltaY;

    RootPanel.get().setWidgetPosition(this, (newX >= 0 ? newX : originalX), (newY >= 0 ? newY : originalY));
    // override modal bootstrap rules
    DOM.setStyleAttribute(getElement(), "marginLeft", "0px");
    DOM.setStyleAttribute(getElement(), "marginTop", "0px");
  }

  private void onMouseUpEvent(Event event) {
    //reset states
    if(move) {
      move = false;
      DOM.releaseCapture(getElement());
      DOM.setStyleAttribute(movePanel.getElement(), "cursor", "default");
    }
    if(resize) {
      resize = false;
      DOM.releaseCapture(getElement());
      DOM.setStyleAttribute(getElement(), "cursor", "default");
    }
  }

  /**
   * returns if mousepointer is in region to show cursor-resize
   *
   * @param event
   * @return true if in region
   */
  protected boolean isCursorResize(Event event) {
    int cursorY = DOM.eventGetClientY(event);
    int initialY = getAbsoluteTop();
    int height = getOffsetHeight();

    int cursorX = DOM.eventGetClientX(event);
    int initialX = getAbsoluteLeft();
    int width = getOffsetWidth();

    //only in bottom right corner (area of 10 pixels in square)
    return (((initialX + width - 20) < cursorX && cursorX <= (initialX + width + 20)) &&
        ((initialY + height - 20) < cursorY && cursorY <= (initialY + height + 20)));
  }

  /**
   * sets the element in panel
   *
   * @param movePanel
   */
  public void setMovePanel(Widget movePanel) {
    this.movePanel = movePanel;
  }

  /**
   * is cursor in moving state?
   *
   * @param event event to process
   * @return true if cursor is in movement
   */
  protected boolean isCursorMove(Event event) {
    if(movePanel != null) {
      int cursorY = DOM.eventGetClientY(event);
      int initialY = movePanel.getAbsoluteTop();
      int height = movePanel.getOffsetHeight();
      int cursorX = DOM.eventGetClientX(event);
      int initialX = movePanel.getAbsoluteLeft();
      int width = movePanel.getOffsetWidth();

      return (initialY <= cursorY && initialY + height >= cursorY && initialX <= cursorX &&
          initialX + width >= cursorX);
    } else return false;
  }
}
