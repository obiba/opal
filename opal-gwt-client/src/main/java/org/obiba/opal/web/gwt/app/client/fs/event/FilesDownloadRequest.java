/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.fs.event;

import java.util.List;

import org.obiba.opal.web.model.client.opal.FileDto;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Optional;
import com.gwtplatform.dispatch.annotation.Order;

@GenEvent
public class FilesDownloadRequest {

  @Order(0)
  FileDto parent;

  @Order(1)
  List<FileDto> children;

  @Optional
  @Order(2)
  String password;
}
