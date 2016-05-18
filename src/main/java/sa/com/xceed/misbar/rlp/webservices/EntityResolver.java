package sa.com.xceed.misbar.rlp.webservices;

// Created by Farrukh on 2/14/2016.

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import sa.com.xceed.misbar.rlp.util.BasisEntityResolver;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Service("EntityResolverWebService")
@Scope("request")
@Path("/entityresolver")
@PermitAll
public class EntityResolver extends MisbarWebService
{
    @POST
    @PermitAll
    @Path("/resolve")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getResolvedEntities(String text)
    {
         BasisEntityResolver entityResolver = new BasisEntityResolver();
        return returnResponse(gson.toJson(entityResolver.extractResolvedEntity(text), List.class));
    }



}