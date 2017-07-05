/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import org.obiba.opal.spi.search.SearchQueryExecutor;

/**
 * Creates a @{SearchQueryExecutor}.
 */
public interface SearchQueryExecutorFactory {

  SearchQueryExecutor create();

}
