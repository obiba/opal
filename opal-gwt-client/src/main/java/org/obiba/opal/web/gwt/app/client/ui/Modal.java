package org.obiba.opal.web.gwt.app.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import javax.annotation.Nullable;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.base.AlertBase;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ShowEvent;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Bootstrap Modal, resizable and draggable.
 */
public class Modal extends com.github.gwtbootstrap.client.ui.Modal {

  private static final int INDEX_HEADER = 0;

  private static final int INDEX_BODY = 1;

  private static final int RESIZE_CURSOR_MARGIN = 30; // pixel

  private static final int DEFAULT_MINIMUM_WIDTH = 200;

  private static final ModalStack MODAL_STACK = new ModalStack();

  private final Panel alertPlace;

  private boolean resizable = false;

  private boolean draggable;

  private boolean resize = false;

  private boolean move = false;

  private int moveStartX;

  private int moveStartY;

  private Widget movePanel;

  private boolean hiddenOnStack = false;

  private int bodyVerticalMargin = 0;

  private int minHeight = 0;

  private int minWidth = DEFAULT_MINIMUM_WIDTH;

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
    setResizable(true);
    History.addValueChangeHandler(new HistoryChangeValueHandler());
    add(alertPlace = new FlowPanel());
  }

  public void setResizable(boolean resizable) {
    this.resizable = resizable;
  }

  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
  }

  public void setAutoHide(boolean autoHide) {
    if(autoHide) {
      setBackdrop(BackdropType.NORMAL);
    } else {
      setBackdrop(BackdropType.STATIC);
    }
  }

  @Override
  public void show() {

    if(!MODAL_STACK.isEmpty() && !MODAL_STACK.has(this)) {
      MODAL_STACK.hideCurrent();
    }

    MODAL_STACK.push(this);

    super.show();
    getElement().getStyle().setProperty("marginTop", "0px");
    // in case the last position was cached
    scrollTop(getBodyWidget().getElement());
  }

  @Override
  protected void onShow(Event e) {
    if(!hiddenOnStack) fireEvent(new ShowEvent(e));
  }

  @Override
  protected void onShown(Event e) {
    if(!hiddenOnStack) super.onShown(e);
    hiddenOnStack = false;
    // calculate once the modal is shown, too expensive to do this on every mouse move
    calculateBodyVerticalMargin();
    setInitialDimension();
  }

  public void setBusy(boolean value) {
    updateCursor(value ? Style.Cursor.WAIT : Style.Cursor.DEFAULT);

    if(value) {
      unsinkMouseEvents();
    } else {
      sinkMouseEvents();
    }
  }

  public void setMinWidth(int width) {
    if(width < DEFAULT_MINIMUM_WIDTH) return;
    minWidth = width;
  }

  public void setMinHeight(int height) {
    if(height < 0) return;
    minHeight = height;
  }

  public void setPadding(int padding) {
    setBoddyPadding(getBodyWidget().getElement(), padding);
  }

  public int getBodyHeight() {
    return getBodyWidget().getOffsetHeight();
  }

  @Override
  protected void onHide(Event e) {
    if(!hiddenOnStack) super.onHide(e);
  }

  /**
   * This method is called once the widget is completely hidden.
   */
  @Override
  protected void onHidden(Event e) {
    if(!hiddenOnStack) {
      super.onHidden(e);

      MODAL_STACK.pop(this);
      MODAL_STACK.showCurrent();
    }

  }

  private void setInitialDimension() {
    setInitialWidth();
    setInitialHeight();
  }

  private void setInitialHeight() {
    if(minHeight == 0 || getOffsetHeight() > minHeight) return;

    resizeBodyVertically(minHeight);
    // NOTE: setting the height and the body height causes footer draw glitches
    // for now we only set the width on the body
//    setHeight(minHeight + "px");
    setMaxHeigth(minHeight + "px");
  }

  private void setInitialWidth() {
    if(minWidth == 0 || getOffsetWidth() > minWidth) return;
    setWidth(minWidth);
  }

  private void sinkMouseEvents() {
    //listen to mouse-events
    DOM.sinkEvents(getElement(), Event.ONMOUSEDOWN |
        Event.ONMOUSEMOVE |
        Event.ONMOUSEUP |
        Event.ONMOUSEOVER);
  }

  private void unsinkMouseEvents() {
    DOM.sinkEvents(getElement(), 0);
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
        onMouseUpEvent();
        break;
    }
  }

  protected Widget getHeaderWidget() {
    return getWidgetAt(INDEX_HEADER);
  }

  protected Widget getBodyWidget() {
    return getWidgetAt(INDEX_BODY);
  }

  protected Widget getFooterWidget() {

    for(Widget child : getChildren()) {
      if(child instanceof ModalFooter) {
        return child;
      }
    }

    return null;
  }

  private void onMouseOverEvent(Event event) {
    //showCurrent different cursors
    if(resizable && isCursorResize(event)) {
      updateCursor(Style.Cursor.SE_RESIZE);
    } else if(draggable && isCursorMove(event)) {
      updateCursor(Style.Cursor.MOVE);
    } else {
      updateCursor(Style.Cursor.DEFAULT);
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
      moveStartX = event.getClientX();
      moveStartY = event.getClientY();
    }
  }

  private void onMouseMoveEvent(Event event) {
    //calculate and set the new size or move

    if(resize) {
      doResize(event);
    } else if(move) {
      doMove(event);
    }
  }

  private void doResize(Event event) {
    int absX = event.getClientX();
    int absY = event.getClientY();
    int originalX = getAbsoluteLeft();
    int originalY = getAbsoluteTop();
    //do not allow mirror-functionality
    if(absY > originalY && absX > originalX) {
      Integer height = Math.max(minHeight, absY - originalY + 2);
      Integer width = Math.max(minWidth, absX - originalX + 2);
      resizeBodyVertically(height);
      // NOTE: setting the height and the body height causes footer draw glitches
      // for now we only set the width on the body
//      setHeight(height + "px");
      // NOTE: com.github.gwtbootstrap.client.ui.Modal.setWith() also centers the window, using UIObject.setWidth()
      setWidth(width + "px");
      setMaxHeigth(height + "px");
    }
  }

  private void calculateBodyVerticalMargin() {
    Widget header = getHeaderWidget();
    Widget footer = getFooterWidget();

    int headerHeight = header == null ? 0 : header.getOffsetHeight();
    int footerHeight = footer == null ? 0 : footer.getOffsetHeight();
    bodyVerticalMargin = headerHeight + footerHeight;
    minHeight = Math.max(bodyVerticalMargin, minHeight);
  }

  private void resizeBodyVertically(int height) {
    Widget body = getBodyWidget();
    int newHeight = Math.max(minHeight - bodyVerticalMargin, height - bodyVerticalMargin);
    body.setHeight(newHeight + "px");
  }

  private Widget getWidgetAt(int index) {
    try {
      return getWidget(index);
    } catch(IndexOutOfBoundsException ignored) {
    }
    return null;
  }

  private void doMove(Event event) {
    int absX = event.getClientX();
    int absY = event.getClientY();
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

    RootPanel.get().setWidgetPosition(this, newX >= 0 ? newX : originalX, newY >= 0 ? newY : originalY);
    // override modal bootstrap rules
    getElement().getStyle().setProperty("marginLeft", "0px");
    getElement().getStyle().setProperty("marginTop", "0px");
  }

  private void onMouseUpEvent() {
    //reset states
    if(move) {
      move = false;
      DOM.releaseCapture(getElement());
      updateCursor(Style.Cursor.DEFAULT);
    }
    if(resize) {
      resize = false;
      DOM.releaseCapture(getElement());
      updateCursor(Style.Cursor.DEFAULT);
    }
  }

  private void updateCursor(Style.Cursor cursor) {
    getElement().getStyle().setCursor(cursor);
  }

  /**
   * returns if mousepointer is in region to showCurrent cursor-resize
   *
   * @param event
   * @return true if in region
   */
  protected boolean isCursorResize(Event event) {
    int cursorY = event.getClientY();
    int cursorX = event.getClientX();
    int xBound = getAbsoluteLeft() + getOffsetWidth();
    int yBound = getAbsoluteTop() + getOffsetHeight();

    return xBound - RESIZE_CURSOR_MARGIN < cursorX && cursorX <= xBound + RESIZE_CURSOR_MARGIN &&
        yBound - RESIZE_CURSOR_MARGIN < cursorY && cursorY <= yBound + RESIZE_CURSOR_MARGIN;
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
      int cursorY = event.getClientY();
      int initialY = movePanel.getAbsoluteTop();
      int height = movePanel.getOffsetHeight();
      int cursorX = event.getClientX();
      int initialX = movePanel.getAbsoluteLeft();
      int width = movePanel.getOffsetWidth();

      return initialY <= cursorY && initialY + height >= cursorY && initialX <= cursorX &&
          initialX + width >= cursorX;
    }
    return false;
  }

  /**
   * Closes the alerts along with the highlighted groups
   */
  public void closeAlerts() {
    Collection<Alert> alerts = new ArrayList<Alert>();
    for(Widget anAlertPlace : alertPlace) {
      alerts.add((Alert) anAlertPlace);
    }
    for(Alert alert : alerts) {
      // automatically removes the alert from the alertPlace children list, hence the two loops
      alert.close();
    }
  }

  public void clearAlert() {
    alertPlace.clear();
  }

  public void clearAlert(HasType<ControlGroupType> group) {
    clearAlert();
    group.setType(ControlGroupType.NONE);
  }

  // This method should probably live somewhere else, but we currently only use it to add debug info during testing.
  public static String alertDebugInfo(JsArrayString items) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < items.length(); i++) {
          sb.append("<pre>");
          sb.append(items.get(i));
          sb.append("</pre>\n");
      }
      return sb.toString();
  }

  public void addAlert(Alert alert) {
    scrollTop(getBodyWidget().getElement());
    alertPlace.add(alert);
  }

  public void addAlert(String message, AlertType type) {
    addAlert(message, type, (CloseHandler<AlertBase>) null);
  }

  public void addAlert(String message, AlertType type, final HasType<ControlGroupType> group) {
    group.setType(ControlGroupType.ERROR);
    addAlert(message, type, new CloseHandler<AlertBase>() {
      @Override
      public void onClose(CloseEvent<AlertBase> event) {
        group.setType(ControlGroupType.NONE);
      }
    });
  }

  public void addAlert(String message, AlertType type, @Nullable final CloseHandler<AlertBase> groupCloseHandler) {
    final Alert alert = new Alert(message);
    alert.setType(type);
    alert.setAnimation(true);
    alert.setClose(true);
    alert.addCloseHandler(new CloseHandler<AlertBase>() {
      @Override
      public void onClose(CloseEvent<AlertBase> event) {
        alert.removeFromParent();
        if(groupCloseHandler != null) groupCloseHandler.onClose(event);
      }
    });
    addAlert(alert);
  }

  private class HistoryChangeValueHandler implements ValueChangeHandler<String> {

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
      Modal modal = Modal.this;
      if(modal.isVisible()) modal.hide();
    }
  }

  // These class is part of a temporary HACK until get-bootstrap library supports stacked modal dialogs.
  // Currently this is not supported: https://github.com/twbs/bootstrap/issues/8785

  private static class ModalStack {

    private final Stack<Modal> currentlyShown = new Stack<Modal>();

    public boolean isEmpty() {
      return currentlyShown.isEmpty();
    }

    public boolean has(Modal modal) {
      return currentlyShown.search(modal) != -1;
    }

    private void push(Modal modal) {
      if(!modal.hiddenOnStack && currentlyShown.search(modal) == -1) {
        currentlyShown.push(modal);
      }
    }

    private void pop(Modal modal) {
      Modal top = currentlyShown.peek();

      if(!top.equals(modal)) {
        throw new IllegalArgumentException("Modal dialog is not on the stack");
      }

      currentlyShown.pop();
    }

    public void hideCurrent() {
      Modal current = currentlyShown.peek();
      if(current != null) {
        current.hiddenOnStack = true;
        current.hide();
      }
    }

    public void showCurrent() {
      if(currentlyShown.size() > 0) {
        Modal current = currentlyShown.peek();
        current.show();
      }
    }
  }

  private native void scrollTop(Element e) /*-{
      $wnd.jQuery(e).scrollTop(0);
  }-*/;

  private native void setBoddyPadding(Element e, int padding) /*-{
      $wnd.jQuery(e).css("padding", padding + "px");
  }-*/;


}
