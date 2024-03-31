/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.provider;

import com.thoughtworks.xstream.converters.ConversionException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.provider.ErrorDtoExceptionMapper;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Component
@Provider
public class ConversionExceptionMapper
        extends ErrorDtoExceptionMapper<ConversionException> {

    @Override
    protected Response.Status getStatus() {
        return INTERNAL_SERVER_ERROR;
    }

    @Override
    protected Ws.ClientErrorDto getErrorDto(ConversionException exception) {
        return ClientErrorDtos.getErrorMessage(getStatus(), "ConversionException")
                .addArguments(exception.getCause().getMessage()).build();
    }

}
