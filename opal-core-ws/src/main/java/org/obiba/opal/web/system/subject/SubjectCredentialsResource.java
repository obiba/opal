/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.subject;

import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Path("/system/subject-credentials")
public class SubjectCredentialsResource {

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @GET
  public List<Opal.SubjectCredentialsDto> getAll() {
    return Lists.newArrayList(Iterables.transform(subjectCredentialsService.getSubjectCredentials(),
        new Function<SubjectCredentials, Opal.SubjectCredentialsDto>() {
          @Override
          public Opal.SubjectCredentialsDto apply(SubjectCredentials subjectCredentials) {
            return Dtos.asDto(subjectCredentials);
          }
        }));
  }

  @POST
  public Response create(Opal.SubjectCredentialsDto dto) {
    SubjectCredentials subjectCredentials = Dtos.fromDto(dto);
    if(subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName()) != null) {
      ConstraintViolation<SubjectCredentials> violation = ConstraintViolationImpl
          .forBeanValidation("{org.obiba.opal.core.validator.Unique.message}", "must be unique",
              SubjectCredentials.class, subjectCredentials, subjectCredentials, subjectCredentials,
              PathImpl.createPathFromString("name"), null, null);
      throw new ConstraintViolationException(ImmutableSet.of(violation));
    }

    switch(subjectCredentials.getAuthenticationType()) {
      case PASSWORD:
        if(dto.hasPassword()) {
          subjectCredentials.setPassword(subjectCredentialsService.hashPassword(dto.getPassword()));
        }
        break;
      case CERTIFICATE:
        if(dto.hasCertificate()) {
          subjectCredentials.setCertificate(dto.getCertificate().toByteArray());
        } else {
          ConstraintViolation<SubjectCredentials> violation = ConstraintViolationImpl
              .forBeanValidation("{javax.validation.constraints.NotNull.message}", "may not be null",
                  SubjectCredentials.class, subjectCredentials, subjectCredentials, subjectCredentials,
                  PathImpl.createPathFromString("certificate"), null, null);
          throw new ConstraintViolationException(ImmutableSet.of(violation));
        }
        break;
    }
    subjectCredentialsService.save(subjectCredentials);
    return Response.ok().build();
  }
}