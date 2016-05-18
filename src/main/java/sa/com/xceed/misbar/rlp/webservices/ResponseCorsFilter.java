package sa.com.xceed.misbar.rlp.webservices;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: Osman
 * Date: 5/19/13
 * Time: 10:20 AM
 */
public class ResponseCorsFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest req, ContainerResponse contResp) {

        String originHeader = req.getHeaderValue("Origin");
        //Check against our approved list here

        Response.ResponseBuilder resp = Response.fromResponse(contResp.getResponse());
        resp.header("Access-Control-Allow-Origin", originHeader)
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT")
                .header("Access-Control-Allow-Credentials","true");

        String reqHead = req.getHeaderValue("Access-Control-Request-Headers");

        if(null != reqHead && !reqHead.equals(null)){
            resp.header("Access-Control-Allow-Headers", reqHead);
        }

        contResp.setResponse(resp.build());
        return contResp;
    }

}
