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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.opal.core.unit.FunctionalUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class OpalConfiguration {
  //
  // Instance Variables
  //

  private String secretKey;

  private String fileSystemRoot;

  private MagmaEngineFactory magmaEngineFactory;

  private Set<FunctionalUnit> functionalUnits;

  private Set<ReportTemplate> reportTemplates;

  private List<OpalConfigurationExtension> extensions;

  //
  // Constructors
  //

  public OpalConfiguration() {
    functionalUnits = Sets.newLinkedHashSet();
    reportTemplates = Sets.newLinkedHashSet();
    extensions = Lists.newArrayList();
  }

  //
  // Methods
  //
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

  public <T extends OpalConfigurationExtension> T getExtension(Class<T> type) {
    try {
      return Iterables.getOnlyElement(Iterables.filter(extensions, type));
    } catch(IndexOutOfBoundsException e) {
      throw new NoSuchElementException();
    }
  }

  public Set<FunctionalUnit> getFunctionalUnits() {
    return functionalUnits;
  }

  public Set<ReportTemplate> getReportTemplates() {
    return Collections.unmodifiableSet(reportTemplates);
  }

  public void setReportTemplates(Set<ReportTemplate> reportTemplates) {
    this.reportTemplates.clear();
    if(reportTemplates != null) {
      this.reportTemplates.addAll(reportTemplates);
    }
  }

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