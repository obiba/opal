/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.opal.core.unit.FunctionalUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("UnusedDeclaration")
public class OpalConfiguration {

  private String secretKey;

  private String fileSystemRoot;

  private MagmaEngineFactory magmaEngineFactory;

  private final Set<FunctionalUnit> functionalUnits;

  private final Set<ReportTemplate> reportTemplates;

  private final List<OpalConfigurationExtension> extensions;

  public OpalConfiguration() {
    functionalUnits = Sets.newLinkedHashSet();
    reportTemplates = Sets.newLinkedHashSet();
    extensions = Lists.newArrayList();
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setFileSystemRoot(String fileSystemRoot) {
    this.fileSystemRoot = fileSystemRoot;
  }

  public String getFileSystemRoot() {
    return fileSystemRoot;
  }

  public MagmaEngineFactory getMagmaEngineFactory() {
    return magmaEngineFactory;
  }

  public void setMagmaEngineFactory(MagmaEngineFactory magmaEngineFactory) {
    this.magmaEngineFactory = magmaEngineFactory;
  }

  public void addExtension(OpalConfigurationExtension extension) {
    if(!hasExtension(extension.getClass())) {
      extensions.add(extension);
    }
  }

  public <T extends OpalConfigurationExtension> T getExtension(Class<T> type) {
    try {
      return Iterables.getOnlyElement(Iterables.filter(extensions, type));
    } catch(IndexOutOfBoundsException e) {
      throw new NoSuchElementException();
    }
  }

  public <T extends OpalConfigurationExtension> boolean hasExtension(Class<T> type) {
    return Iterables.size(Iterables.filter(extensions, type)) == 1;
  }

  public Set<FunctionalUnit> getFunctionalUnits() {
    return functionalUnits;
  }

  public Set<ReportTemplate> getReportTemplates() {
    return Collections.unmodifiableSet(reportTemplates);
  }

  public void setReportTemplates(Collection<ReportTemplate> reportTemplates) {
    this.reportTemplates.clear();
    if(reportTemplates != null) {
      this.reportTemplates.addAll(reportTemplates);
    }
  }

  @Nullable
  public ReportTemplate getReportTemplate(String name) {
    for(ReportTemplate reportTemplate : reportTemplates) {
      if(reportTemplate.getName().equals(name)) {
        return reportTemplate;
      }
    }
    return null;
  }

  public boolean hasReportTemplate(String name) {
    return getReportTemplate(name) != null;
  }

  public void removeReportTemplate(String name) {
    ReportTemplate reportTemplateToRemove = getReportTemplate(name);
    if(reportTemplateToRemove != null) {
      reportTemplates.remove(reportTemplateToRemove);
    }
  }

  public void addReportTemplate(ReportTemplate reportTemplate) {
    reportTemplates.add(reportTemplate);
  }

  public boolean hasReportTemplates() {
    return reportTemplates.size() > 0;
  }

}