package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Stack;

import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.event.ShowEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Bootstrap Modal, resizable and draggable.
 */
public class Modal extends com.github.gwtbootstrap.client.ui.Modal {

  private static ModalStack modalStack = new ModalStack();

  private boolean resizable = false;

  private boolean draggable;

  private boolean resize = false;

  private boolean move = false;

  private int moveStartX;

  private int moveStartY;

  private Widget movePanel;

  private boolean hiddenOnStack = false;

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
    History.addValueChangeHandler(new HistoryChangeValueHandler());
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

  @Override
  public void show() {

    if (!modalStack.isEmty() && !modalStack.has(this)) {
      modalStack.hideCurrent();
    }

    modalStack.push(this);

    super.show();
  }

  @Override
  protected void onShow(Event e) {
    if (!hiddenOnStack) fireEvent(new ShowEvent(e));
  }

  @Override
  protected void onShown(Event e) {
    if (!hiddenOnStack) super.onShown(e);
    hiddenOnStack = false;
  }

  protected void onHide(Event e) {
    if (!hiddenOnStack) super.onHide(e);
  }

  /**
   * This method is called once the widget is completely hidden.
   */
  protected void onHidden(Event e) {
    if (!hiddenOnStack) {
      super.onHidden(e);

      modalStack.pop(this);
      modalStack.showCurrent();
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
     * processes the mouse-events to showCurrent cursor or change states
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
    //showCurrent different cursors
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
   * returns if mousepointer is in region to showCurrent cursor-resize
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

  private class HistoryChangeValueHandler implements ValueChangeHandler<String> {

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
      Modal modal = Modal.this;
      if (modal.isVisible()) modal.hide();
    }
  }

  // These class is part of a temporary HACK until get-bootstrap library supports stacked modal dialogs.
  // Currently this is not supported: https://github.com/twbs/bootstrap/issues/8785

  private static class ModalStack {

    private Stack<Modal> currentlyShown = new Stack<Modal>();

    public boolean isEmty() {
      return currentlyShown.size() == 0;
    }

    public boolean has(Modal modal) {
      return currentlyShown.search(modal) != -1;
    }

    private void push(Modal modal) {
      if (!modal.hiddenOnStack && currentlyShown.search(modal) == -1) currentlyShown.push(modal);
      GWT.log("push() :: " + currentlyShown.size());
    }

    private void pop(Modal modal) {
      Modal top = currentlyShown.peek();

      if (!top.equals(modal)) {
        throw new IllegalArgumentException("Modal dialog is not on the stack");
      }

      currentlyShown.pop();

      GWT.log("push() :: " + currentlyShown.size());
    }

    public void hideCurrent() {
      Modal current = currentlyShown.peek();
      if (current != null) {
        current.hiddenOnStack = true;
        current.hide();
      }
    }

    public void showCurrent() {
      if (currentlyShown.size() > 0) {
        Modal current = currentlyShown.peek();
        current.show();
      }
    }
  }

}
