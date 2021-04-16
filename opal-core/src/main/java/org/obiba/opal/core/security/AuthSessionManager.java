/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.security;

import org.obiba.oidc.utils.DefaultOIDCSessionManager;
import org.springframework.stereotype.Component;

@Component
public class AuthSessionManager extends DefaultOIDCSessionManager {
}
