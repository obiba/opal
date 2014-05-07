/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.List;

import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface IndexAdministrationUiHandlers extends UiHandlers {
  void start();

  void stop();

  void suspend();

  void resume();

  void refresh();

  void configure();

  void delete(List<TableIndexStatusDto> statusDtos);

  void schedule(List<TableIndexStatusDto> statusDtos);

  void indexNow(List<TableIndexStatusDto> statusDtos);
}
