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

import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.js.views.VariablesClause;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.View.Builder;
import org.obiba.magma.views.support.AllClause;
import org.obiba.magma.views.support.NoneClause;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.FileViewDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

/**
 * An implementation of {@Code ViewDtoExtension} that can create {@code View} instances by de-serializing an
 * {@code XStream} document. This can eventually be extended for other types of file-based de-serialization.
 */
@SuppressWarnings("UnusedDeclaration")
@Component
public class FileViewDtoExtension implements ViewDtoExtension {

  private final OpalRuntime opalRuntime;

  @Autowired
  public FileViewDtoExtension(OpalRuntime opalRuntime) {
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    this.opalRuntime = opalRuntime;
  }

  @Override
  public boolean isExtensionOf(@NotNull ViewDto viewDto) {
    return viewDto.hasExtension(FileViewDto.view);
  }

  @Override
  public boolean isDtoOf(@NotNull View view) {
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
    try {
      FileObject file = opalRuntime.getFileSystem().getRoot().resolveFile(fileDto.getFilename());
      if(file.exists()) {
        try(InputStream is = file.getContent().getInputStream()) {
          return makeViewFromFile(viewBuilder, fileDto, is);
        }
      }
      throw new RuntimeException("cannot find file specified '" + fileDto.getFilename() + "'");
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private View makeViewFromFile(Builder viewBuilder, FileViewDto fileDto, InputStream is) {
    switch(fileDto.getType()) {
      case SERIALIZED_XML:
        return makeViewFromXMLFile(viewBuilder, is);
      case EXCEL:
        return makeViewFromExcelFile(viewBuilder, is);
    }
    throw new IllegalStateException("unknown view file type " + fileDto.getType());
  }

  private View makeViewFromXMLFile(Builder viewBuilder, InputStream is) {
    // Serialized view
    View view = (View) MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream()
        .fromXML(is);
    return viewBuilder.select(view.getSelectClause()).list(view.getListClause()).where(view.getWhereClause()).build();
  }

  private View makeViewFromExcelFile(Builder viewBuilder, InputStream is) {
    Datasource ed = new ExcelDatasource("tmp", is);
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

  @Override
  public TableDto asTableDto(ViewDto viewDto, Magma.TableDto.Builder tableDtoBuilder) {
    FileViewDto fileDto = viewDto.getExtension(FileViewDto.view);
    try {
      FileObject file = opalRuntime.getFileSystem().getRoot().resolveFile(fileDto.getFilename());
      if(file.exists()) {
        try(InputStream is = file.getContent().getInputStream()) {
          return makeTableDtoFromFile(tableDtoBuilder, fileDto, is);
        }
      }
      throw new RuntimeException("cannot find file specified '" + fileDto.getFilename() + "'");
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private TableDto makeTableDtoFromFile(Magma.TableDto.Builder tableDtoBuilder, FileViewDto fileDto, InputStream is) {
    switch(fileDto.getType()) {
      case SERIALIZED_XML:
        return makeTableDtoFromXMLFile(tableDtoBuilder, is);
      case EXCEL:
        return makeTableDtoFromExcelFile(tableDtoBuilder, is);
      default:
        throw new IllegalStateException("unknown view file type " + fileDto.getType());
    }
  }

  private TableDto makeTableDtoFromXMLFile(TableDto.Builder tableDtoBuilder, InputStream is) {
    // Serialized view
    View view = (View) MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream()
        .fromXML(is);
    view.initialise();
    for(VariableValueSource vs : view.getListClause().getVariableValueSources()) {
      Variable v = vs.getVariable();
      tableDtoBuilder.setEntityType(v.getEntityType());
      tableDtoBuilder.addVariables(Dtos.asDto(v));
    }
    if (!tableDtoBuilder.hasEntityType()) {
      tableDtoBuilder.setEntityType("Participant");
    }
    return tableDtoBuilder.build();
  }

  private TableDto makeTableDtoFromExcelFile(TableDto.Builder tableDtoBuilder, InputStream is) {
    Datasource ed = new ExcelDatasource("tmp", is);
    try {
      Initialisables.initialise(ed);
      // Get the first table, whichever it is
      ValueTable t = ed.getValueTables().iterator().next();
      tableDtoBuilder.setEntityType(t.getEntityType());
      for(Variable v : t.getVariables()) {
        tableDtoBuilder.addVariables(Dtos.asDto(v));
      }
      return tableDtoBuilder.build();
    } finally {
      Disposables.silentlyDispose(ed);
    }
  }

}