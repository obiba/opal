/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map.batch;

import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.obiba.opal.core.domain.metadata.DataItem;
import org.obiba.opal.core.service.MetaDataService;
import org.obiba.opal.datasource.InvalidCatalogueException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *
 */
public class DataItemReader implements ItemStreamReader<DataItem> {

  private String datasource;

  private String catalogueName;

  private SessionFactory sessionFactory;

  private Session session;

  private MetaDataService metadataService;

  private Iterator<DataItem> itemStream;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public void setCatalogueName(String catalogueName) {
    this.catalogueName = catalogueName;
  }

  public void setMetadataService(MetaDataService metadataService) {
    this.metadataService = metadataService;
  }

  public void close() throws ItemStreamException {
    itemStream = null;
    SessionFactoryUtils.closeSession(session);
  }

  public void open(ExecutionContext executionContext) throws ItemStreamException {
    session = SessionFactoryUtils.getSession(sessionFactory, true);
    TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));

    Catalogue catalogue = metadataService.getCatalogue(datasource, catalogueName);
    if(catalogue == null) {
      throw new InvalidCatalogueException(datasource, catalogueName);
    }
    itemStream = catalogue.getDataItems().iterator();
  }

  public void update(ExecutionContext executionContext) throws ItemStreamException {
  }

  public DataItem read() throws Exception, UnexpectedInputException, ParseException {
    if(itemStream.hasNext()) {
      return itemStream.next();
    }
    return null;
  }

}
