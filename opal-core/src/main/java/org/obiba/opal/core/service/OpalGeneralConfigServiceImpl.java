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

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.event.OpalGeneralConfigUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction;

/**
 * Default implementation of System Service
 */
@Component
public class OpalGeneralConfigServiceImpl implements OpalGeneralConfigService {

  private static final Logger log = LoggerFactory.getLogger(OpalGeneralConfigServiceImpl.class);

  private final OrientDbService orientDbService;

  private final EventBus eventBus;

  @Autowired
  public OpalGeneralConfigServiceImpl(OrientDbService orientDbService, EventBus eventBus) {
    this.orientDbService = orientDbService;
    this.eventBus = eventBus;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  @Override
  public void save(@NotNull final OpalGeneralConfig config) {
    orientDbService.execute(new OrientDbService.WithinDocumentTxCallbackWithoutResult() {
      @Override
      protected void withinDocumentTxWithoutResult(ODatabaseDocument db) {

        ODocument document = getDocument(db);
        if(document == null) {
          document = new ODocument(OpalGeneralConfig.class.getSimpleName());
        }
        orientDbService.copyToDocument(config, document);

        db.begin(OTransaction.TXTYPE.OPTIMISTIC);
        log.debug("save {}", document);
        document.save();
        db.commit();
        eventBus.post(new OpalGeneralConfigUpdatedEvent(config));
      }
    });
  }

  @Override
  @NotNull
  public OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException {
    return orientDbService.execute(new OrientDbService.WithinDocumentTxCallback<OpalGeneralConfig>() {
      @Override
      public OpalGeneralConfig withinDocumentTx(ODatabaseDocument db) {
        ODocument document = getDocument(db);
        if(document == null) {
          throw new OpalGeneralConfigMissingException();
        }
        return orientDbService.fromDocument(OpalGeneralConfig.class, document);
      }
    });
  }

  @Nullable
  private ODocument getDocument(ODatabaseDocument db) {
    String className = OpalGeneralConfig.class.getSimpleName();
    if(db.getMetadata().getSchema().getClass(className) == null) {
      return null;
    }
    return Iterables.getOnlyElement(db.browseClass(className), null);
  }

}
