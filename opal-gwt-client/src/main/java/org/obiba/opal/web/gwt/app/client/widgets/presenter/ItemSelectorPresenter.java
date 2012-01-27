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

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Widget for selecting items (strings).
 */
public class ItemSelectorPresenter extends PresenterWidget<ItemSelectorPresenter.Display> {

  @Inject
  public ItemSelectorPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  public interface Display extends View {

    void setItemInputDisplay(ItemInputDisplay itemInputDisplay);

    void addItem(String item);

    void removeItem(int row);

    void clear();

    int getItemCount();

    List<String> getItems();

    void setItems(Iterable<String> items);
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