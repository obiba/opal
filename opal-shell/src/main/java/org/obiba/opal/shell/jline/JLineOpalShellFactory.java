/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.jline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.OpalShellFactory;
import org.springframework.stereotype.Component;

/**
 * A factory for {@code JLineOpalShell} instances
 */
@Component
public class JLineOpalShellFactory implements OpalShellFactory {

  public JLineOpalShellFactory() {
  }

  public OpalShell newShell(CommandRegistry commandRegistry, InputStream in, OutputStream out, OutputStream err) {
    if(commandRegistry == null) throw new IllegalArgumentException("commandRegistry cannot be null");
    if(in == null) throw new IllegalArgumentException("in cannot be null");
    if(out == null) throw new IllegalArgumentException("out cannot be null");
    if(err == null) throw new IllegalArgumentException("err cannot be null");
    return new JLineOpalShell(commandRegistry, in, new LineSeparatorEnforcingWriter(new PrintWriter(out)));
  }

  // TODO: Remove this class when https://issues.apache.org/jira/browse/SSHD-67 is fixed
  private static final class LineSeparatorEnforcingWriter extends Writer {

    private final Writer delegate;

    LineSeparatorEnforcingWriter(Writer delegate) {
      this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
      this.delegate.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
      this.delegate.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      for(int i = off; i < len; i++) {
        char c = cbuf[i];
        if(c == '\n') {
          this.delegate.write('\r');
        }
        this.delegate.write(c);
      }
    }
  }
}
