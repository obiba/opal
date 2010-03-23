package org.obiba.opal.core.tx;

import java.util.Properties;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.transaction.JTATransactionFactory;
import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.transaction.TransactionManagerLookupFactory;

/**
 * An implementation of {@code JTATransactionFactory} for obtaining the {@code UserTransaction} directly from the JOTM
 * {@code TransactionManager} instead of issuing a JNDI lookup. This class depends on having the {@code
 * TransactionManagerLookup} that returns a the JOTM {@code Current} instance (such as the {@code
 * JOTMTransactionManagerLookup}).
 * 
 * <p>
 * As described in the {@code JTATransactionFactory}, this class overrides the {@code getUserTransaction} method to
 * prevent JNDI lookup. The method returns the
 */
public class JOTMTransactionFactory extends JTATransactionFactory {

  /** A reference to JOTM's UserTransaction */
  private UserTransaction ut;

  @Override
  public void configure(Properties props) throws HibernateException {
    super.configure(props);
    TransactionManagerLookup lookup = TransactionManagerLookupFactory.getTransactionManagerLookup(props);
    if(lookup == null) {
      throw new HibernateException("Could not obtain an instance of TransactionManagerLookup. Make sure the property '" + Environment.TRANSACTION_MANAGER_STRATEGY + "' is set.");
    }

    // JOTM implements both UserTransaction and TransactionManager in same class. If we find the TransactionManager, we
    // should find the UserTransaction.
    TransactionManager tm = lookup.getTransactionManager(props);
    if(tm == null) {
      throw new HibernateException("Could not lookup JOTM TransactionManager");
    }
    if(tm instanceof UserTransaction) {
      ut = (UserTransaction) tm;
    } else {
      throw new HibernateException("JTA TransactionManager is not JOTM.");
    }
  }

  @Override
  protected UserTransaction getUserTransaction() {
    return ut;
  }
}
