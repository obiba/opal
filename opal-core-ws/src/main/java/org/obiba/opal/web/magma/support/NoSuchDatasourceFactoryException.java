/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;

/**
 *
 */
public class NoSuchDatasourceFactoryException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private transient DatasourceFactoryDto dto;

  public NoSuchDatasourceFactoryException(DatasourceFactoryDto dto) {
    this.dto = dto;
  }

  public DatasourceFactoryDto getDto() {
    return dto;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();

    // Serialize dto.
    out.write(dto.getSerializedSize());
    out.write(dto.toByteArray());
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    // Deserialize dto.
    int dtoByteCount = in.readInt();
    byte[] dtoBytes = new byte[dtoByteCount];
    in.readFully(dtoBytes);
    dto = DatasourceFactoryDto.parseFrom(dtoBytes);
  }
}
