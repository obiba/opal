/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.unit;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.security.ApplicationRealm;
import org.obiba.opal.core.service.DuplicateSubjectProfileException;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.SubjectProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;

@Component
/**
 * An implementation of {@link FunctionalUnitService} on top of {@link OpalConfiguration}.
 */
public class DefaultFunctionalUnitService implements FunctionalUnitService {

  @Autowired
  private OpalConfigurationService configService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Override
  public boolean hasFunctionalUnit(String unitName) {
    return getFunctionalUnit(unitName) != null;
  }

  @Override
  public void removeFunctionalUnit(final String unitName) {
    configService.modifyConfiguration(new ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        FunctionalUnit unit = getFunctionalUnit(unitName);
        if(unit != null) {
          config.getFunctionalUnits().remove(unit);
        }
      }
    });
    subjectProfileService.deleteProfile(unitName);
  }

  @Override
  @Nullable
  public FunctionalUnit getFunctionalUnit(@Nullable String unitName) {
    for(FunctionalUnit unit : getConfiguredFunctionalUnits()) {
      if(Objects.equal(unit.getName(), unitName)) {
        return unit;
      }
    }
    return null;
  }

  @Override
  public Set<FunctionalUnit> getFunctionalUnits() {
    return Collections.unmodifiableSet(getConfiguredFunctionalUnits());
  }

  @Override
  public void addOrReplaceFunctionalUnit(final FunctionalUnit functionalUnit) {
    final boolean newUnit = !hasFunctionalUnit(functionalUnit.getName());
    if(newUnit) {
      // check for subject profile
      SubjectProfile found = subjectProfileService.getProfile(functionalUnit.getName());
      if(found != null && !ApplicationRealm.APPLICATION_REALM.equals(found.getRealm())) {
        throw new DuplicateSubjectProfileException(found);
      }
    }
    configService.modifyConfiguration(new ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        FunctionalUnit unit = getFunctionalUnit(functionalUnit.getName());
        if(!newUnit) config.getFunctionalUnits().remove(unit);
        config.getFunctionalUnits().add(functionalUnit);
        if(newUnit) subjectProfileService.ensureProfile(functionalUnit.getName(), ApplicationRealm.APPLICATION_REALM);
      }
    });
  }

  @Override
  public FileObject getUnitDirectory(String unitName) throws NoSuchFunctionalUnitException, FileSystemException {
    if(!hasFunctionalUnit(unitName)) {
      throw new NoSuchFunctionalUnitException(unitName);
    }

    FileObject unitsDir = getOpalRuntime().getFileSystem().getRoot().resolveFile("units");
    unitsDir.createFolder();

    FileObject unitDir = unitsDir.resolveFile(unitName);
    unitDir.createFolder();

    return unitDir;
  }

  private Set<FunctionalUnit> getConfiguredFunctionalUnits() {
    return configService.getOpalConfiguration().getFunctionalUnits();
  }

  // We get the OpalRuntime this way in order to remove the circular dependency:
  // DefaultOpalRuntime -> OpalJettyServer -> SecurityManager -> FunctionalUnitRealm -> FunctionalUnitService ->
  // DefaultOpalRuntime
  private OpalRuntime getOpalRuntime() {
    return applicationContext.getBean(OpalRuntime.class);
  }

}
