package net.anuwas.mapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.anuwas.dto.ErrorMessage;
import net.anuwas.exception.BusinessException;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Inject
    ErrorMessage errorMessage;

    @Override
    public Response toResponse(BusinessException exception) {
        errorMessage.setMessage(exception.getMessage());
        errorMessage.setStatus(exception.getStatus());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorMessage).type(MediaType.APPLICATION_JSON).build();
    }
}
