/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public abstract class AbstractOpalModule extends AbstractPresenterModule {

  protected <V extends WizardView, W extends WizardPresenterWidget<V>, P extends WizardProxy<W>> void bindWizardPresenterWidget(Class<W> presenter, Class<V> view, Class<? extends V> impl, Class<P> proxy) {
    bind(proxy).asEagerSingleton();
    bindPresenterWidget(presenter, view, impl);
  }
}
