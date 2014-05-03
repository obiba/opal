/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell;

import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.web.model.Commands;

public final class Dtos {
  private Dtos() {
  }

  public static Commands.CommandStateDto asDto(CommandJob commandJob) {
    Commands.CommandStateDto.Builder dtoBuilder = Commands.CommandStateDto.newBuilder() //
        .setId(commandJob.getId()) //
        .setName(commandJob.getName())//
        .setCommand(commandJob.getCommand().getName()) //
        .setCommandArgs(commandJob.getCommand().toString()) //
        .setOwner(commandJob.getOwner()) //
        .setStatus(commandJob.getStatus().toString()) //
        .addAllMessages(commandJob.getMessages());

    if(commandJob.hasProject()) {
      dtoBuilder.setProject(commandJob.getProject());
    }

    if(commandJob.getStartTime() != null) {
      dtoBuilder.setStartTime(DateTimeType.get().valueOf(commandJob.getStartTime()).toString());
    }
    if(commandJob.getEndTime() != null) {
      dtoBuilder.setEndTime(DateTimeType.get().valueOf(commandJob.getEndTime()).toString());
    }

    if(commandJob.getMessageProgress() != null) {
      dtoBuilder.setProgress(progressAsDto(commandJob));
    }

    return dtoBuilder.build();
  }

  private static Commands.CommandStateDto.ProgressDto.Builder progressAsDto(CommandJob commandJob) {
    Commands.CommandStateDto.ProgressDto.Builder dtoBuilder = Commands.CommandStateDto.ProgressDto.newBuilder()//
        .setMessage(commandJob.getMessageProgress())//
        .setCurrent(commandJob.getCurrentProgress().intValue())//
        .setEnd(commandJob.getEndProgress().intValue())//
        .setPercent(commandJob.getPercentProgress());
    return dtoBuilder;
  }
}
