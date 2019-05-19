/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A cell that renders a button and takes a delegate to perform actions on mouseUp.
 *
 * @param <C> the type that this Cell represents
 */
public class IconActionCell<C> extends AbstractCell<C> {

  /**
   * The delegate that will handle events from the cell.
   *
   * @param <T> the type that this delegate acts on
   */
  public interface Delegate<T> {
    /**
     * Perform the desired action on the given object.
     *
     * @param value the value to be acted upon
     */
    void executeClick(NativeEvent event, T value);

    void executeMouseDown(NativeEvent event, T value);
  }

  protected final IconType iconType;

  protected final SafeHtml message;

  protected Delegate<C> delegate;

  /**
   * Construct a new {@link ActionCell}.
   *
   * @param iconClass the css class of the icon to display
   * @param delegate the delegate that will handle events
   */
  public IconActionCell(IconType iconType, Delegate<C> delegate) {
    this(iconType, "", delegate);
  }

  public IconActionCell(IconType iconType, String text, Delegate<C> delegate) {
    this(iconType, SafeHtmlUtils.fromString(text), delegate);
  }

  public IconActionCell(IconType iconType, SafeHtml message, Delegate<C> delegate) {
    super("click", "mousedown");
    this.delegate = delegate;
    this.iconType = iconType;
    this.message = message;
  }

  public void setDelegate(Delegate<C> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, C value, NativeEvent event,
      ValueUpdater<C> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if(!isEnabled()) return;
    if("click".equals(event.getType()) || "mousedown".equals(event.getType())) {
      EventTarget eventTarget = event.getEventTarget();
      if(!Element.is(eventTarget)) {
        return;
      }
      if(parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
        // Ignore clicks that occur outside of the main element.
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }
  }

  /**
   * Method to be overridden in order to enable dynamically the icon. Default is enabled.
   *
   * @return
   */
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {

    if(isEnabled()) {
      if(iconType != null) {
        Icon i = new Icon(iconType);
        sb.append(SafeHtmlUtils.fromSafeConstant("<a class=\"iconb\">")).appendHtmlConstant(i.toString())
            .append(message).append(SafeHtmlUtils.fromSafeConstant("</a>"));
      } else {
        sb.append(SafeHtmlUtils.fromSafeConstant("<a class=\"iconb\">")).append(message)
            .append(SafeHtmlUtils.fromSafeConstant("</a>"));
      }
    } else {
      if(iconType != null) {
        Icon i = new Icon(iconType);
        sb.append(SafeHtmlUtils.fromSafeConstant("<span class=\"icon disabled\">")).appendHtmlConstant(i.toString())
            .append(message).append(SafeHtmlUtils.fromSafeConstant("</span>"));
      } else {
        sb.append(SafeHtmlUtils.fromSafeConstant("<span class=\"iconb disabled\">")).append(message)
            .append(SafeHtmlUtils.fromSafeConstant("</span>"));
      }
    }
  }

  @Override
  protected void onEnterKeyDown(Context context, Element parent, C value, NativeEvent event,
      ValueUpdater<C> valueUpdater) {
    if("click".equals(event.getType())) {
      delegate.executeClick(event, value);
    } else {
      delegate.executeMouseDown(event, value);
    }
  }
}
