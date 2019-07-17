/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.collect.Lists;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Component
public class SubjectTokenServiceImpl implements SubjectTokenService {

  private static final Logger log = LoggerFactory.getLogger(SubjectTokenServiceImpl.class);


  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(SubjectToken.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public SubjectToken saveToken(SubjectToken token) {
    if (hasToken(token.getPrincipal(), token.getName())) {
      throw new IllegalArgumentException("Subject token with name " + token.getName() + " already exists for principal " + token.getPrincipal());
    }
    if (!token.hasToken()) {
      token.setToken(UUID.randomUUID().toString());
    }
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
          .forEach(tk -> orientDbService.delete(tk));
    } catch (Exception e) {
      // ignore
    }
  }

  @Override
  public SubjectToken getToken(String id) throws NoSuchSubjectTokenException {
    SubjectToken template = new SubjectToken();
    template.setToken(id);
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
  public boolean hasToken(String id) {
    SubjectToken template = new SubjectToken();
    template.setToken(id);
    SubjectToken token = orientDbService.findUnique(template);
    return token != null;
  }

  @Override
  public boolean hasToken(String principal, String name) {
    return getTokens(principal).stream().anyMatch(tk -> tk.getName().equals(name));
  }

  @Override
  public void deleteTokens(String principal) {
    getTokens(principal).forEach(t -> orientDbService.delete(t));
  }

  @Override
  public List<SubjectToken> getTokens(String principal) {
    return Lists.newArrayList(orientDbService.list(SubjectToken.class,
        String.format("select from %s where principal = ?", SubjectToken.class.getSimpleName()), principal));
  }
}
