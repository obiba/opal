/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.event;

import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class VcsCommitInfoReceivedEvent extends GwtEvent<VcsCommitInfoReceivedEvent.Handler> {

  public interface Handler extends EventHandler {

    void onVcsCommitInfoReceived(VcsCommitInfoReceivedEvent event);

  }

  private static final Type<Handler> TYPE = new Type<>();

  private final VcsCommitInfoDto commitInfoDto;

  public VcsCommitInfoReceivedEvent(VcsCommitInfoDto dto) {
    commitInfoDto = dto;
  }


  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onVcsCommitInfoReceived(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  public VcsCommitInfoDto getCommitInfoDto() {
    return commitInfoDto;
  }

}
