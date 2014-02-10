/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.bookmark.list;

import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface BookmarkListUiHandlers extends UiHandlers {

  ActionHandler<BookmarkDto> getActionHandler();
}
