/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget for selecting items (strings).
 */
public class ItemSelectorPresenter extends WidgetPresenter<ItemSelectorPresenter.Display> {
  //
  // Instance Variables
  //

  //
  // Constructors
  //

  @Inject
  public ItemSelectorPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setItemInputDisplay(ItemInputDisplay itemInputDisplay);

    void addItem(String item);

    void removeItem(int row);

    void clear();

    int getItemCount();

    List<String> getItems();
  }

  public interface ItemInputDisplay {

    void clear();

    String getItem();

    Widget asWidget();

    void setEnterKeyHandler(EnterKeyHandler handler);
  }

  public interface EnterKeyHandler {

    void enterKeyPressed();
  }
}