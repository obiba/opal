/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.view;

import java.io.InputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.View.Builder;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.model.Magma.FileViewDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

/**
 * An implementation of {@Code ViewDtoExtension} that can create {@code View} instances by de-serializing an
 * {@code XStream} document. This can eventually be extended for other types of file-based de-serialization.
 */
@Component
public class FileViewDtoExtension implements ViewDtoExtension {

  private final OpalRuntime opalRuntime;

  @Autowired
  public FileViewDtoExtension(OpalRuntime opalRuntime) {
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    this.opalRuntime = opalRuntime;
  }

  public boolean isExtensionOf(final ViewDto viewDto) {
    return viewDto.hasExtension(FileViewDto.view);
  }

  @Override
  public boolean isDtoOf(View view) {
    // Always false: we cannot convert an existing view to a file.
    return false;
  }

  @Override
  public ViewDto asDto(View view) {
    throw new UnsupportedOperationException();
  }

  @Override
  public View fromDto(ViewDto viewDto, Builder viewBuilder) {
    FileViewDto fileDto = viewDto.getExtension(FileViewDto.view);
    InputStream is = null;
    try {
      FileObject file = opalRuntime.getFileSystem().getRoot().resolveFile(fileDto.getFilename());
      if(file.exists()) {
        switch(fileDto.getType()) {
        case SERIALIZED_XML:
          // Serialized view
          View view = (View) MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream().fromXML(is = file.getContent().getInputStream());
          return viewBuilder.select(view.getSelectClause()).list(view.getListClause()).where(view.getWhereClause()).build();
        case EXCEL:
          ExcelDatasource ed = new ExcelDatasource("tmp", is = file.getContent().getInputStream());
          try {
            Initialisables.initialise(ed);
            // Get the first table, whichever it is
            ValueTable t = ed.getValueTables().iterator().next();
            VariablesClause vc = new VariablesClause();
            vc.setVariables(Sets.newLinkedHashSet(t.getVariables()));
            return viewBuilder.select(new NoneClause()).list(vc).where(new AllClause()).build();

          } finally {
            Disposables.silentlyDispose(ed);
          }
        }
      }
      throw new RuntimeException("cannot find file specified '" + fileDto.getFilename() + "'");
    } catch(FileSystemException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(is);
    }
  }

}