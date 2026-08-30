/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import com.google.common.base.Strings;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "vcf_samples_mappings",
    uniqueConstraints = @UniqueConstraint(name = "uk_vcf_samples_mappings_project", columnNames = "project_name"))
public class VCFSamplesMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "project_name", nullable = false)
  private String projectName;

  @NotNull
  @Column(name = "table_reference")
  private String tableReference;

  @NotNull
  @Column(name = "participant_id_variable")
  private String participantIdVariable;

  @NotNull
  @Column(name = "sample_role_variable")
  private String sampleRoleVariable;

  public VCFSamplesMapping() {
  }

  public VCFSamplesMapping(String name) {
    projectName = name;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(VCFSamplesMapping vcfSamplesMapping) {
    return new Builder(vcfSamplesMapping);
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public boolean hasTableReference() {
    return !Strings.isNullOrEmpty(tableReference);
  }

  public String getTableReference() {
    return tableReference;
  }

  public void setTableReference(String tableReference) {
    this.tableReference = tableReference;
  }

  public String getParticipantIdVariable() {
    return participantIdVariable;
  }

  public void setParticipantIdVariable(String participantIdVariable) {
    this.participantIdVariable = participantIdVariable;
  }

  public String getSampleRoleVariable() {
    return sampleRoleVariable;
  }

  public void setSampleRoleVariable(String sampleRoleVariable) {
    this.sampleRoleVariable = sampleRoleVariable;
  }

  public static class Builder {
    private VCFSamplesMapping vcfSamplesMapping;

    private Builder() {
      vcfSamplesMapping = new VCFSamplesMapping();
    }

    public Builder(VCFSamplesMapping samplesMapping) {
      vcfSamplesMapping = new VCFSamplesMapping();
      vcfSamplesMapping.projectName = samplesMapping.projectName;
      vcfSamplesMapping.tableReference = samplesMapping.tableReference;
      vcfSamplesMapping.participantIdVariable = samplesMapping.participantIdVariable;
      vcfSamplesMapping.sampleRoleVariable = samplesMapping.sampleRoleVariable;
    }

    public Builder projectName(String value) {
      vcfSamplesMapping.projectName = value;
      return this;
    }

    public Builder tableName(String value) {
      vcfSamplesMapping.tableReference = value;
      return this;
    }

    public Builder participantIdVariable(String value) {
      vcfSamplesMapping.participantIdVariable = value;
      return this;
    }

    public Builder sampleRoleVariable(String value) {
      vcfSamplesMapping.sampleRoleVariable = value;
      return this;
    }

    public VCFSamplesMapping build() {
      return vcfSamplesMapping;
    }
  }
}
