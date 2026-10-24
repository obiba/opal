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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.obiba.shiro.authc.TicketAuthenticationToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * A bearer JWT from the configured provider logs in as the same principal, with the same groups, as a browser login.
 */
public class OpalOIDCRealmTest {

  private static HttpServer provider;

  private static String issuer;

  private static RSAKey key;

  @BeforeClass
  public static void startProvider() throws Exception {
    key = new RSAKeyGenerator(2048).keyID("k1").generate();
    provider = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    issuer = "http://127.0.0.1:" + provider.getAddress().getPort();
    String discovery = "{\"issuer\":\"" + issuer + "\",\"jwks_uri\":\"" + issuer + "/jwks\"," +
        "\"subject_types_supported\":[\"public\"],\"id_token_signing_alg_values_supported\":[\"RS256\"]}";
    String jwks = new JWKSet(key.toPublicJWK()).toString();
    for (Map.Entry<String, String> route : Map.of("/.well-known/openid-configuration", discovery, "/jwks", jwks).entrySet()) {
      provider.createContext(route.getKey(), exchange -> {
        byte[] body = route.getValue().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
          out.write(body);
        }
      });
    }
    provider.start();
  }

  @AfterClass
  public static void stopProvider() {
    provider.stop(0);
  }

  @Test
  public void bearerTokenFromProviderLogsInWithUsernameAndGroups() throws Exception {
    OpalOIDCRealm realm = realm();
    AuthenticationInfo info = realm.getAuthenticationInfo(bearer(jwt(issuer, "opal", 60)));
    assertThat(info.getPrincipals().getPrimaryPrincipal()).isEqualTo("alice");
    assertThat(info.getPrincipals().getRealmNames()).containsOnly("keycloak");
    assertThat(realm.hasRole(info.getPrincipals(), "analysts")).isTrue();
    assertThat(realm.hasRole(info.getPrincipals(), "keycloak")).isTrue();
  }

  /**
   * #4235: the groups of a login are the ones of its own UserInfo, whatever a previous login of the same user carried.
   */
  @Test
  public void groupsFollowTheLatestLogin() throws Exception {
    OpalOIDCRealm realm = realm();
    AuthenticationInfo first = realm.getAuthenticationInfo(browser("analysts"));
    assertThat(realm.getGroups(first.getPrincipals())).containsOnly("analysts", "keycloak");
    AuthenticationInfo second = realm.getAuthenticationInfo(browser("reviewers"));
    assertThat(realm.getGroups(second.getPrincipals())).containsOnly("reviewers", "keycloak");
  }

  @Test
  public void principalsOfAnotherRealmHaveNoGroups() {
    assertThat(realm().getGroups(new SimplePrincipalCollection("bob", "opal-user-realm"))).isEmpty();
  }

  private OIDCAuthenticationToken browser(String role) throws Exception {
    OIDCCredentials credentials = new OIDCCredentials();
    credentials.setIdToken(JWTParser.parse(jwt(issuer, "opal", 60, role)));
    credentials.setUserInfo(Map.of("sub", "1234", "preferred_username", "alice", "roles", List.of(role)));
    return new OIDCAuthenticationToken(credentials);
  }

  @Test(expected = AuthenticationException.class)
  public void tokenForAnotherAudienceIsRejected() throws Exception {
    realm().getAuthenticationInfo(bearer(jwt(issuer, "other-client", 60)));
  }

  @Test(expected = AuthenticationException.class)
  public void expiredTokenIsRejected() throws Exception {
    realm().getAuthenticationInfo(bearer(jwt(issuer, "opal", -120)));
  }

  @Test
  public void tokenFromAnotherIssuerIsLeftToOtherRealms() throws Exception {
    assertThat(realm().getAuthenticationInfo(bearer(jwt("https://elsewhere.example.org", "opal", 60)))).isNull();
  }

  @Test
  public void opaqueTicketsAndPasswordsAreNotSupported() {
    assertThat(realm().supports(bearer("not-a-jwt"))).isFalse();
    assertThat(realm().supports(new UsernamePasswordToken("alice", "secret"))).isFalse();
  }

  private OpalOIDCRealm realm() {
    OIDCConfiguration configuration = new OIDCConfiguration("keycloak");
    configuration.setClientId("opal");
    configuration.setSecret("s3cret");
    configuration.setDiscoveryURI(issuer + "/.well-known/openid-configuration");
    configuration.setCustomParam("usernameClaim", "preferred_username");
    configuration.setCustomParam("groupsClaim", "roles");
    return new OpalOIDCRealm(configuration);
  }

  private static TicketAuthenticationToken bearer(String token) {
    return new TicketAuthenticationToken(token, "/ws/system/subject-profile/_current", "obibaid");
  }

  private static String jwt(String iss, String aud, int secondsToLive) throws Exception {
    return jwt(iss, aud, secondsToLive, "analysts");
  }

  private static String jwt(String iss, String aud, int secondsToLive, String role) throws Exception {
    long now = System.currentTimeMillis();
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .issuer(iss).subject("1234").audience(aud)
        .issueTime(new Date(now - 1000)).expirationTime(new Date(now + secondsToLive * 1000L))
        .claim("preferred_username", "alice").claim("roles", List.of(role))
        .build();
    SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
    jwt.sign(new RSASSASigner(key));
    return jwt.serialize();
  }
}
