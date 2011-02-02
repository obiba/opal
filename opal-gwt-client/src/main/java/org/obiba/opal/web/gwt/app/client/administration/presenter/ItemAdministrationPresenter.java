/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for presenting administration widgets.
 */
public abstract class ItemAdministrationPresenter<D extends WidgetDisplay> extends WidgetPresenter<D> {

  public ItemAdministrationPresenter(D display, EventBus eventBus) {
    super(display, eventBus);
  }

  public abstract String getName();

  public Widget getWidget() {
    return super.getDisplay().asWidget();
  }

}
