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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

/**
 * Utilities for handling View Dtos. This class is not static like others because it is extensible through a set of
 * {@code ViewDtoExtension} present in the context.
 */
@Component
public final class ViewDtos {

  private final Set<ViewDtoExtension> extensions;

  @Autowired
  public ViewDtos(Set<ViewDtoExtension> extensions) {
    this.extensions = ImmutableSet.copyOf(extensions);
  }

  public View fromDto(ViewDto viewDto) {
    List<ValueTable> fromTables = getFromTables(viewDto);
    View.Builder builder = View.Builder.newView(viewDto.getName(), (ValueTable[]) fromTables.toArray(new ValueTable[fromTables.size()]));
    for(ViewDtoExtension extension : extensions) {
      if(extension.isExtensionOf(viewDto)) {
        return extension.fromDto(viewDto, builder);
      }
    }
    throw new IllegalStateException("Unknown view type");
  }

  public ViewDto asDto(View view) {
    for(ViewDtoExtension extension : extensions) {
      if(extension.isDtoOf(view)) {
        return extension.asDto(view);
      }
    }
    throw new IllegalStateException("Unknown view type");
  }

  private List<ValueTable> getFromTables(ViewDto viewDto) {
    List<ValueTable> fromTables = new ArrayList<ValueTable>();
    for(int i = 0; i < viewDto.getFromCount(); i++) {
      String fromTable = viewDto.getFrom(i);
      fromTables.add(MagmaEngine.get().createReference(fromTable));
    }
    return fromTables;
  }

}
