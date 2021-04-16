/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.fs.presenter;

import java.util.List;

import org.obiba.opal.web.model.client.opal.FileDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FolderDetailsUiHandlers extends UiHandlers {

  void onFilesChecked(List<FileDto> files);

  void onFolderSelection(FileDto fileDto);

}
