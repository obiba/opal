/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.datasource.view;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.UiHandlers;

public abstract class AbstractCsvOptionsViewWithUiHandlers<C extends UiHandlers>extends AbstractCsvOptionsView implements
    HasUiHandlers<C> {

  private C uiHandlers;

  /**
   * Access the {@link UiHandlers} associated with this {@link com.gwtplatform.mvp.client.View}.
   * <p/>
   * <b>Important!</b> Never call {@link #getUiHandlers()} inside your constructor
   * since the {@link UiHandlers} are not yet set.
   *
   * @return The {@link UiHandlers}, or {@code null} if they are not yet set.
   */
  protected C getUiHandlers() {
    return uiHandlers;
  }

  @Override
  public void setUiHandlers(C uiHandlers) {
    this.uiHandlers = uiHandlers;
  }
}
