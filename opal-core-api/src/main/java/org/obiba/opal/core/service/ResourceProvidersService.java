/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.json.JSONObject;
import org.obiba.opal.spi.resource.Resource;

import java.util.List;

/**
 * A service for getting the providers of resources. In practice these providers are R packages.
 */
public interface ResourceProvidersService extends SystemService {

  /**
   * Get all resource providers.
   *
   * @return
   */
  List<ResourceProvider> getResourceProviders();

  /**
   * Get a resource provider by name.
   *
   * @param name
   * @return
   */
  ResourceProvider getResourceProvider(String name) throws NoSuchResourceProviderException;

  /**
   * Get a resource factory by its name and provider.
   *
   * @param provider
   * @param name
   * @return
   */
  ResourceFactory getResourceFactory(String provider, String name) throws NoSuchResourceProviderException, NoSuchResourceFactoryException;

  /**
   * Get a merged list of {@link Category} items.
   *
   * @return
   */
  List<Category> getAllCategories();

  /**
   * Get the merged list of {@link ResourceFactory} items, matching the tag names (or all if none).
   *
   * @param tags
   * @return
   */
  List<ResourceFactory> getResourceFactories(List<String> tags);

  interface ResourceProvider {

    /**
     * Get unique name of the resource provider (in practice: the R package name).
     *
     * @return
     */
    String getName();

    /**
     * Get the profile name where this resource provider was found (in practice: the R server cluster name).
     *
     * @return
     */
    String getProfile();

    /**
     * Get the human readable name.
     *
     * @return
     */
    String getTitle();

    /**
     * Get the detailed description of this resource provider.
     *
     * @return
     */
    String getDescription();

    /**
     * URL to go to a page description of resource provider.
     *
     * @return
     */
    String getWeb();

    /**
     * Get the list of {@link Category} items defined by this resource provider.
     * @return
     */
    List<Category> getCategories();

    /**
     * Get {@link Category} by name.
     *
     * @param name
     * @return
     */
    Category getCategory(String name);

    /**
     * Get the {@link ResourceFactory} items defined by this resource provider.
     *
     * @return
     */
    List<ResourceFactory> getFactories();
  }

  /**
   * A category helps at classifying the resources that can be built. Mot of the time a resource has several dimensions,
   * such as its location, the storage system and the data format. Multiple tags can be applied on a
   * {@link ResourceFactory} and by selecting these tags .
   */
  interface Category {

    /**
     * Unique identifier name of the tag.
     *
     * @return
     */
    String getName();

    /**
     * Get tag title.
     *
     * @return
     */
    String getTitle();

    /**
     * Get tag detailed explanation.
     *
     * @return
     */
    String getDescription();
  }

  /**
   * A resource factory is in charge of defining what are the form schemas for capturing the resource parameters and
   * credentials and from these captured data, it will make a {@link Resource} object.
   */
  interface ResourceFactory {

    /**
     * Get provider name.
     *
     * @return
     */
    String getProvider();

    /**
     * Resource type unique name.
     *
     * @return
     */
    String getName();

    /**
     * Title of the resource type.
     *
     * @return
     */
    String getTitle();

    /**
     * Markdown can be used to describe the resource type.
     *
     * @return
     */
    String getDescription();

    /**
     * Tags applied to the resource.
     *
     * @return
     */
    List<String> getTags();

    /**
     * The form to collect resource type parameters, described by a JSON object.
     *
     * @return
     */
    JSONObject getParametersSchemaForm();

    /**
     * The form to collect resource type credentials, described by a JSON object.
     *
     * @return null if not applicable
     */
    JSONObject getCredentialsSchemaForm();

    /**
     * Make a {@link Resource} object from parameters and credentials that were collected.
     *
     * @param name
     * @param parameters
     * @param credentials can be null if not applicable
     * @return
     */
    Resource createResource(String name, JSONObject parameters, JSONObject credentials);
  }

}
