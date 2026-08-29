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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.json.JSONObject;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "resource_references",
    uniqueConstraints = @UniqueConstraint(name = "uk_resource_references", columnNames = {"name", "project"}))
public class ResourceReference implements Timestamped, HasUniqueProperties {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @NotBlank
  @Column(nullable = false)
  private String project;

  private String name;

  @Lob
  private String description;

  // plugin name or internal provider
  private String provider;

  // resource factory name
  private String factory;

  @Lob
  @Column(name = "parameters_model")
  private String parametersModel;

  @Lob
  @Column(name = "credentials_model")
  private String credentialsModel;

  @Lob
  @Column(name = "encrypted_credentials_model")
  private String encryptedCredentialsModel;

  /**
   * This one keeps its own timestamps rather than extending AbstractTimestamped, and has since it was written.
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date created = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @NotNull
  public String getProject() {
    return project;
  }

  public void setProject(@NotNull String project) {
    this.project = project;
  }

  public String getProvider() {
    return "opal-resource-commons".equals(provider) ? "resourcer" : provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getFactory() {
    return factory;
  }

  public void setFactory(String factory) {
    this.factory = factory;
  }

  public String getParametersModel() {
    return parametersModel;
  }

  public void setParametersModel(String parametersModel) {
    this.parametersModel = parametersModel;
  }

  public JSONObject getParameters() {
    return Strings.isNullOrEmpty(parametersModel) ? new JSONObject() : new JSONObject(parametersModel);
  }

  public String getCredentialsModel() {
    return credentialsModel;
  }

  public void setCredentialsModel(String credentialsModel) {
    this.credentialsModel = credentialsModel;
  }

  public String getEncryptedCredentialsModel() {
    return encryptedCredentialsModel;
  }

  public void setEncryptedCredentialsModel(String encryptedCredentialsModel) {
    this.encryptedCredentialsModel = encryptedCredentialsModel;
  }

  public JSONObject getCredentials() {
    return Strings.isNullOrEmpty(credentialsModel) ? new JSONObject() : new JSONObject(credentialsModel);
  }

  @NotNull
  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {
      @NotNull
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().valueOf(updated);
      }

      @NotNull
      @Override
      public Value getCreated() {
        return DateTimeType.get().valueOf(created);
      }
    };
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name", "project");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(name, project);
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(@NotNull Date created) {
    this.created = created;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(@NotNull Date updated) {
    this.updated = updated;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("project", project)
        .add("parameters", parametersModel)
        .add("credentials", credentialsModel)
        .toString();
  }

}
