/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import java.util.List;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.springframework.stereotype.Component;

@Component
public class TaxonomiesConfigurationService implements TaxonomyService {

  private final ExtensionConfigurationSupplier<TaxonomiesConfiguration> configSupplier;

  public TaxonomiesConfigurationService(OpalConfigurationService configService) {
    configSupplier = new ExtensionConfigurationSupplier<TaxonomiesConfiguration>(configService,
        TaxonomiesConfiguration.class);
  }

  private TaxonomiesConfiguration getConfig() {
    if(!configSupplier.hasExtension()) {
      configSupplier.addExtension(new TaxonomiesConfiguration());
    }
    return configSupplier.get();
  }

  @Override
  public List<Taxonomy> getTaxonomies() {
    return getConfig().getTaxonomies();
  }

  @Override
  public boolean hasTaxonomy(String name) {
    return getConfig().hasTaxonomy(name);
  }

  @Override
  public void removeTaxonomy(final String name) {
    configSupplier.modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<TaxonomiesConfiguration>() {
      @Override
      public void doWithConfig(TaxonomiesConfiguration config) {
        config.removeTaxonomy(name);
      }
    });
  }

  @Override
  public void addOrReplaceTaxonomy(Taxonomy taxonomy) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Taxonomy getOrCreateTaxonomy(String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Taxonomy getTaxonomy(String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
