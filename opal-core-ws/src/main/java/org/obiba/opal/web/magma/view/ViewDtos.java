/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.view;

import com.google.common.collect.ImmutableSet;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableStatus;
import org.obiba.magma.ValueView;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Utilities for handling View Dtos. This class is not static like others because it is extensible through a set of
 * {@code ViewDtoExtension} present in the context.
 */
@Component
public final class ViewDtos {

  private Set<ValueViewDtoExtension> extensions;

  @Autowired
  public void setExtensions(Collection<ValueViewDtoExtension> extensions) {
    this.extensions = ImmutableSet.copyOf(extensions);
  }

  @NotNull
  public ValueView fromDto(@NotNull ViewDto viewDto) {
    for (ValueViewDtoExtension extension : extensions) {
      if (extension.isExtensionOf(viewDto)) {
        return extension.fromDto(viewDto);
      }
    }
    throw new IllegalStateException("Unknown view type");
  }

  @NotNull
  public TableDto asTableDto(@NotNull ViewDto viewDto) {
    TableDto.Builder builder = TableDto.newBuilder().setName(viewDto.getName());
    for (ValueViewDtoExtension extension : extensions) {
      if (extension.isExtensionOf(viewDto)) {
        return extension.asTableDto(viewDto, builder);
      }
    }
    throw new IllegalStateException("Unknown view type");
  }

  @NotNull
  public ViewDto asDto(@NotNull ValueView view) {
    for (ValueViewDtoExtension extension : extensions) {
      if (extension.isDtoOf(view)) {
        return extension.asDto(view).toBuilder()
            .setStatus(Dtos.asDto(view.getStatus())).build();
      }
    }
    throw new IllegalStateException("Unknown view type");
  }

  @NotNull
  private List<ValueTable> getFromTables(@NotNull ViewDto viewDto) {
    List<ValueTable> fromTables = new ArrayList<>();
    for (int i = 0; i < viewDto.getFromCount(); i++) {
      String fromTable = viewDto.getFrom(i);
      fromTables.add(MagmaEngine.get().createReference(fromTable));
    }
    return fromTables;
  }

}
