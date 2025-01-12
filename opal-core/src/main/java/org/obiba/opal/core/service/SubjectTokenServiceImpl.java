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

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.crypto.CryptoException;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.event.SubjectProfileDeletedEvent;
import org.obiba.opal.core.service.security.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class SubjectTokenServiceImpl implements SubjectTokenService {

  private static final Logger log = LoggerFactory.getLogger(SubjectTokenServiceImpl.class);

  private static final String LEGACY_DATE = "2022-05-01";

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int TOKEN_LENGTH = 32;


  /**
   * Number of times the user password is hashed for attack resiliency
   */
  @Value("${org.obiba.opal.security.password.nbHashIterations}")
  private int nbHashIterations;

  /**
   * Nb of days since creation after which the token is removed.
   */
  @Value("${org.obiba.opal.security.login.pat.expiresIn}")
  private int expiresIn;

  /**
   * Nb of days of inactivaty after which token needs to be renewed.
   */
  @Value("${org.obiba.opal.security.login.pat.activityTimeout}")
  private int activityTimeout;

  private final OrientDbService orientDbService;

  private final OpalConfigurationService opalConfigurationService;

  private final Date legacyDate;

  @Autowired
  public SubjectTokenServiceImpl(OrientDbService orientDbService, OpalConfigurationService opalConfigurationService) {
    this.orientDbService = orientDbService;
    this.opalConfigurationService = opalConfigurationService;
    Date legacyDate;
    try {
      legacyDate = new SimpleDateFormat("yyyy-MM-dd").parse(LEGACY_DATE);
    } catch (ParseException e) {
      legacyDate = new Date();
    }
    this.legacyDate = legacyDate;
  }

  @Override
  public void start() {
    orientDbService.createUniqueIndex(SubjectToken.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public SubjectToken saveToken(SubjectToken token) {
    if (hasToken(token.getPrincipal(), token.getName())) {
      throw new DuplicateSubjectTokenException(token);
    }
    if (!token.hasToken()) {
      throw new IllegalArgumentException("Access token is missing");
    }
    token.setToken(hashToken(token.getToken()));
    orientDbService.save(token, token);
    return token;
  }

  @Override
  public void deleteToken(String id) {
    try {
      orientDbService.delete(getToken(id));
    } catch (NoSuchSubjectTokenException e) {
      // ignore
    }
  }

  @Override
  public void deleteToken(String principal, String name) {
    try {
      getTokens(principal).stream()
          .filter(tk -> tk.getName().equals(name))
          .forEach(orientDbService::delete);
    } catch (Exception e) {
      // ignore
    }
  }

  @Override
  public void renewToken(String principal, String name) {
    Optional<SubjectToken> first = getTokens(principal).stream()
        .filter(tk -> tk.getName().equals(name)).findFirst();
    first.ifPresent(token -> orientDbService.save(token, token));
  }

  @Override
  public SubjectToken getToken(String id) throws NoSuchSubjectTokenException {
    SubjectToken template = new SubjectToken();
    template.setToken(hashToken(id));
    SubjectToken token = orientDbService.findUnique(template);
    if (token == null) {
      throw new NoSuchSubjectTokenException(id);
    }
    return token;
  }

  @Override
  public SubjectToken getToken(String id, String principal) throws NoSuchSubjectTokenException {
    SubjectToken token = getToken(id);
    if (token.getPrincipal().equals(principal)) return token;
    throw new NoSuchSubjectTokenException(id);
  }

  @Override
  public void touchToken(SubjectToken token) {
    token.setUpdated(new Date());
    orientDbService.save(token, token);
  }

  @Override
  public SubjectTokenTimestamps getTokenTimestamps(SubjectToken token) {
    Date updated = token.getUpdated().before(legacyDate) ? legacyDate : token.getUpdated();
    Date inactiveAt = activityTimeout > 0 ? DateUtils.addDays(updated, activityTimeout) : null;
    Date expiresAt = expiresIn > 0 ? DateUtils.addDays(token.getCreated(), expiresIn) : null;
    return new SubjectTokenTimestamps(inactiveAt, expiresAt);
  }

  @Override
  public boolean hasToken(String id) {
    SubjectToken template = new SubjectToken();
    template.setToken(hashToken(id));
    SubjectToken token = orientDbService.findUnique(template);
    return token != null;
  }

  @Override
  public boolean hasToken(String principal, String name) {
    return getTokens(principal).stream().anyMatch(tk -> tk.getName().equals(name));
  }

  @Override
  public void deleteTokens(String principal) {
    getTokens(principal).forEach(orientDbService::delete);
  }

  @Override
  public List<SubjectToken> getTokens(String principal) {
    return Lists.newArrayList(orientDbService.list(SubjectToken.class,
        String.format("select from %s where principal = ?", SubjectToken.class.getSimpleName()), principal));
  }

  @Override
  public String generateToken() {
    SecureRandom random = new SecureRandom();
    return IntStream.range(0, TOKEN_LENGTH)
        .mapToObj(i -> String.valueOf(CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))))
        .collect(Collectors.joining());
  }

  @Subscribe
  public void onSubjectProfileDeleted(SubjectProfileDeletedEvent event) {
    deleteTokens(event.getProfile().getPrincipal());
  }

  /**
   * Remove expired tokens. Check is triggered every day at 1am.
   */
  @Scheduled(cron = "0 0 1 * * *")
  public void removeExpiredTokens() {
    if (expiresIn <= 0) return;
    Iterable<SubjectToken> tokens = orientDbService.list(SubjectToken.class);
    Date now = new Date();
    for (SubjectToken token : tokens) {
      Date expiresAt = DateUtils.addDays(token.getCreated(), expiresIn);
      if (now.after(expiresAt)) {
        log.info("Removing expired personal access token: {}:{}", token.getPrincipal(), token.getName());
        orientDbService.delete(token);
      }
    }
  }

  private String hashToken(String id) {
    return new Sha512Hash(id, opalConfigurationService.getOpalConfiguration().getSecretKey(), nbHashIterations)
        .toString();
  }
}
