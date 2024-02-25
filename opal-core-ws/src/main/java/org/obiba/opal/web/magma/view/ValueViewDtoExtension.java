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

import org.obiba.magma.ValueView;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;

import javax.validation.constraints.NotNull;

/**
 * Contract for converting a View to a ViewDto and back.
 */
public interface ValueViewDtoExtension {

  boolean isExtensionOf(@NotNull ViewDto viewDto);

  boolean isDtoOf(@NotNull ValueView view);

  ValueView fromDto(ViewDto viewDto);

  TableDto asTableDto(ViewDto viewDto, TableDto.Builder tableDtoBuilder);

  ViewDto asDto(ValueView view);
}