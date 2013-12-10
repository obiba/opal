/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obiba.opal.core.vcs.CommitInfo;
import org.obiba.opal.web.model.Opal;

import com.google.common.base.Strings;

public final class Dtos {

  private Dtos() {}

  public static Opal.VcsCommitInfosDto asDto(Iterable<CommitInfo> commitInfos) {
    Collection<Opal.VcsCommitInfoDto> commitInfoDtos = new ArrayList<>();

    for(CommitInfo commitInfo : commitInfos) {
      commitInfoDtos.add(asDto(commitInfo));
    }

    return Opal.VcsCommitInfosDto.newBuilder().addAllCommitInfos(commitInfoDtos).build();
  }

  public static Opal.VcsCommitInfoDto asDto(CommitInfo commitInfo) {
    Opal.VcsCommitInfoDto.Builder commitInfoDtoBuilder = Opal.VcsCommitInfoDto.newBuilder()
        .setAuthor(commitInfo.getAuthor()).setDate(commitInfo.getDateAsIso8601()).setCommitId(commitInfo.getCommitId())
        .setComment(commitInfo.getComment()).setIsHead(commitInfo.getIsHead()).setIsCurrent(commitInfo.getIsCurrent());

    List<String> diffEntries = commitInfo.getDiffEntries();

    if(diffEntries != null) {
      commitInfoDtoBuilder.addAllDiffEntries(diffEntries);
    }

    String blob = commitInfo.getBlob();

    if(!Strings.isNullOrEmpty(blob)) {
      commitInfoDtoBuilder.setBlob(blob);
    }

    return commitInfoDtoBuilder.build();
  }
}
