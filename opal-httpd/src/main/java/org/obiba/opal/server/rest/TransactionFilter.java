/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public class TransactionFilter extends Filter {

  private PlatformTransactionManager txManager;

  public TransactionFilter(PlatformTransactionManager txManager) {
    super();
    this.txManager = txManager;
  }

  @Override
  protected int doHandle(final Request request, final Response response) {
    return (Integer) new TransactionTemplate(txManager).execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        return (Integer) TransactionFilter.super.doHandle(request, response);
      }
    });
  }
}
