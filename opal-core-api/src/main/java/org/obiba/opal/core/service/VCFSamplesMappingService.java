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

import org.obiba.opal.core.domain.VCFSamplesMapping;

import jakarta.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface VCFSamplesMappingService extends SystemService {

  String TABLE_ENTITY_TYPE = "Sample";

  Iterable<VCFSamplesMapping> getVCFSamplesMappings();

  @NotNull
  VCFSamplesMapping getVCFSamplesMapping(@NotNull String name) throws NoSuchProjectException;

  boolean hasVCFSamplesMapping(@NotNull String name);

  void save(@NotNull VCFSamplesMapping project) throws ConstraintViolationException;

  void delete(@NotNull String name) throws NoSuchVCFSamplesMappingException;

  void deleteProjectSampleMappings(@NotNull String project);

  List<String> getFilteredSampleIds(@NotNull String projectName, @NotNull String filteringTable, boolean withControl);

  List<String> getControls(@NotNull String projectName);

  Map<String, ParticipantRolePair> findParticipantIdBySampleId(@NotNull String projectName, @NotNull Collection<String> samplesIds);

  class ParticipantRolePair {

    private String participant;
    private String role;

    ParticipantRolePair(String participant, String role) {
      this.participant = participant;
      this.role = role;
    }

    public String getParticipantId() {
      return participant;
    }

    public String getRole() {
      return role;
    }
  }
}
