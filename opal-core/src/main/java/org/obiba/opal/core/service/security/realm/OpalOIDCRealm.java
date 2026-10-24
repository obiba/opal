/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.realm;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.obiba.oidc.shiro.realm.DefaultOIDCGroupsExtractor;
import org.obiba.oidc.shiro.realm.OIDCGroupsExtractor;
import org.obiba.oidc.shiro.realm.OIDCRealm;
import org.obiba.oidc.utils.OIDCTokenValidator;
import org.obiba.shiro.authc.TicketAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Collection;
import java.util.Set;

/**
 * OIDC realm that also accepts, on the REST API, a JWT issued by its ID provider and presented as
 * {@code Authorization: Bearer} (which the authentication filter hands over as a ticket token). The token is
 * verified against the provider's JWKS, issuer, audience (the client id) and expiration, then the subject and
 * groups are mapped from its claims exactly as after a browser login, so both paths yield the same principal.
 */
public class OpalOIDCRealm extends OIDCRealm {

  private static final Logger log = LoggerFactory.getLogger(OpalOIDCRealm.class);

  private final OIDCConfiguration configuration;

  /**
   * obiba-oidc 5.3.1 no longer caches authorization info, so the groups are extracted on every permission check:
   * the extractor (and the {@code ScriptEngineManager} SPI scan its constructor runs) is built once per realm.
   */
  private final OIDCGroupsExtractor groupsExtractor = new DefaultOIDCGroupsExtractor();

  public OpalOIDCRealm(OIDCConfiguration configuration) {
    super(configuration);
    this.configuration = configuration;
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return super.supports(token) || (token instanceof TicketAuthenticationToken ticket && parseJWT(ticket.getTicketId()) != null);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    if (!(token instanceof TicketAuthenticationToken ticket)) return super.doGetAuthenticationInfo(token);
    JWT jwt = parseJWT(ticket.getTicketId());
    try {
      // not from this provider: let another realm answer (the validator enforces the issuer from the metadata)
      if (!issuer().equals(jwt.getJWTClaimsSet().getIssuer())) return null;
      // ponytail: validator built per login (one JWKS fetch per session); cache it if logins get frequent
      new OIDCTokenValidator(configuration).validate(jwt, null);
      OIDCCredentials credentials = new OIDCCredentials();
      credentials.setIdToken(jwt);
      credentials.setUserInfo(jwt.getJWTClaimsSet().getClaims());
      AuthenticationInfo info = super.doGetAuthenticationInfo(new OIDCAuthenticationToken(credentials));
      // the signature check was the credentials check: hand the matcher the token's own credentials
      return info == null ? null : new SimpleAuthenticationInfo(info.getPrincipals(), token.getCredentials());
    } catch (ParseException | BadJOSEException | JOSEException e) {
      log.warn("Bearer token rejected by OIDC realm {}: {}", getName(), e.getMessage());
      throw new AuthenticationException("Bearer token rejected by " + getName(), e);
    }
  }

  @Override
  protected OIDCGroupsExtractor newOIDCGroupsExtractor() {
    return groupsExtractor;
  }

  /**
   * The groups this realm grants to these principals, from the UserInfo they carry.
   */
  public Set<String> getGroups(PrincipalCollection principals) {
    Collection<String> roles = doGetAuthorizationInfo(principals).getRoles();
    return roles == null ? Set.of() : Set.copyOf(roles);
  }

  /**
   * OpenID Connect Discovery 1.0, section 4: the discovery document lives at the issuer's well-known location.
   */
  private String issuer() {
    return configuration.getDiscoveryURI().replaceFirst("/\\.well-known/openid-configuration/?$", "");
  }

  private static JWT parseJWT(String token) {
    try {
      return JWTParser.parse(token);
    } catch (ParseException e) {
      return null;
    }
  }
}
