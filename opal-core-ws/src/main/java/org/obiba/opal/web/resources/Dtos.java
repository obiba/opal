/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.resources;

import com.google.common.base.Strings;
import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.web.model.Resources;

import java.util.stream.Collectors;

public class Dtos {

  public static Resources.ResourceProviderDto asDto(ResourceProvidersService.ResourceProvider resourceProvider) {
    Resources.ResourceProviderDto.Builder builder = Resources.ResourceProviderDto.newBuilder()
        .setName(resourceProvider.getName())
        .setTitle(resourceProvider.getTitle())
        .setDescription(resourceProvider.getDescription());

    if (!Strings.isNullOrEmpty(resourceProvider.getWeb()))
      builder.setWeb(resourceProvider.getWeb());

    builder.addAllResourceFactories(resourceProvider.getFactories().stream().map(Dtos::asDto).collect(Collectors.toList()));
    builder.addAllCategories(resourceProvider.getCategories().stream().map(Dtos::asDto).collect(Collectors.toList()));

    return builder.build();
  }

  public static Resources.ResourceCategoryDto asDto(ResourceProvidersService.Category tag) {
    Resources.ResourceCategoryDto.Builder builder = Resources.ResourceCategoryDto.newBuilder()
        .setName(tag.getName())
        .setTitle(tag.getTitle())
        .setDescription(tag.getDescription());

    return builder.build();
  }

  public static Resources.ResourceFactoryDto asDto(ResourceProvidersService.ResourceFactory resourceFactory) {
    Resources.ResourceFactoryDto.Builder builder = Resources.ResourceFactoryDto.newBuilder()
        .setProvider(resourceFactory.getProvider())
        .setName(resourceFactory.getName())
        .setTitle(resourceFactory.getTitle())
        .setParametersSchemaForm(resourceFactory.getParametersSchemaForm().toString())
        .setCredentialsSchemaForm(resourceFactory.getCredentialsSchemaForm().toString());

    if (!Strings.isNullOrEmpty(resourceFactory.getDescription()))
      builder.setDescription(resourceFactory.getDescription());

    if (!resourceFactory.getTags().isEmpty())
      builder.addAllTags(resourceFactory.getTags());

    return builder.build();
  }
}
