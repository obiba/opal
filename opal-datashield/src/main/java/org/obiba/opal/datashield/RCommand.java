/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.r.RSession;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.shell.commands.AbstractOpalRuntimeDependentCommand;
import org.obiba.opal.shell.commands.CommandUsage;
import org.rosuda.REngine.REXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@CommandUsage(description = "Issue commands to R")
public class RCommand extends AbstractOpalRuntimeDependentCommand<RCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(RCommand.class);

  @Autowired
  private PlatformTransactionManager txManager;

  public void execute() {
    new TransactionTemplate(txManager).execute(new TransactionCallback() {
      public Object doInTransaction(TransactionStatus status) {
        RSession script = getR();
        try {
          if(options.isTable()) {
            script.attach(MagmaEngineTableResolver.valueOf(options.getTable()).resolveTable());
          } else {
            REXP result = script.eval(options.getEval());
            getShell().printf(script.toString(result));
          }
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    });
  }

  private RSession getR() {
    RSession script = (RSession) SecurityUtils.getSubject().getSession().getAttribute(RSession.class);
    if(script == null) {
      SecurityUtils.getSubject().getSession().setAttribute(RSession.class, script = new RSession());
    }
    return script;
  }
}
