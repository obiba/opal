/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.r.list;

import org.obiba.opal.web.model.client.opal.r.RSessionDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface RSessionsUiHandlers extends UiHandlers {

  void onTerminate(RSessionDto session);

  void onRefresh();

}
