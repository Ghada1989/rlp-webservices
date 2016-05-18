package sa.com.xceed.misbar.rlp.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sa.com.xceed.misbar.MisbarCache;
import sa.com.xceed.misbar.model.files.MisbarFile;
import sa.com.xceed.misbar.model.files.MisbarUser;
import sa.com.xceed.misbar.utils.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: Fahad Najib
 * Date: 3/16/13
 * Time: 4:20 PM
 */

public class MisbarWebService {
    private static Logger logger = LoggerFactory.getLogger(MisbarWebService.class);
    @Autowired(required = true)
    protected transient HttpServletRequest httpServletRequest;
    private String hashKey;
    private int cacheTime;
    private MisbarUser user;
    private boolean isCacheable;
    private Integer fileId;
    protected Gson gson;

    @PostConstruct //we use post construct to do stuff just before resource class web method is called
    public void postConstruct() {
        user = (MisbarUser) this.httpServletRequest.getUserPrincipal();

        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

        if(httpServletRequest.getParameter(ParameterCatalog.file_id)!=null && !httpServletRequest.getParameter(ParameterCatalog.file_id).isEmpty())
            fileId = Integer.parseInt(httpServletRequest.getParameter("file_id"));
    }

    /**
     * All subclasses will call this method
     *
     * @param output
     * @return
     */
    protected Response returnResponse(String output) {
        boolean test = System.getProperty("test")!=null && System.getProperty("test").equals("true");
        if(!test && isCacheable())
            setCache(output);
        logger.info("Returned Fresh");
        return Response.status(200).entity(output).build();
    }

    protected Response returnErrorBadRequest(String output) {
        boolean test = System.getProperty("test")!=null && System.getProperty("test").equals("true");
        if(!test && isCacheable())
            setCache(output);
        logger.info("Returned 400 error to from service");
        return Response.status(400).entity(output).build();
    }

    protected Response returnErrorInternalServerError(String output) {
        boolean test = System.getProperty("test")!=null && System.getProperty("test").equals("true");
        if(!test && isCacheable())
            setCache(output);
        logger.info("Returned 500 error to from service");
        return Response.status(500).entity(output).build();
    }

    protected void setCache(String output) {

        if(fileId!=null)
        {
            MisbarCache.set(fileId+"", getCacheTime(), getHashKey(), output);
        }
        else
        {
            MisbarCache.set(getHashKey(), getCacheTime(), output);
        }
    }

    public MisbarUser getUser() {
        return user;
    }

    protected void appendDateIntervalIfProvided(StringBuffer query, String field, Long startTime, Long endTime) {

        if (startTime != -1 && endTime != -1) {
            Date startDate = new Date(startTime * 1000L);
            Date endDate = new Date(endTime * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            appendAndIfApplicable(query);

            query.append(field);
            query.append(":");
            query.append("[");
            query.append(format.format(startDate));
            query.append("T00:00:00Z");

            query.append(" ");
            query.append("TO");
            query.append(" ");

            query.append(format.format(endDate));
            query.append("T23:59:59Z");
            query.append("]");
        }
    }

    private void appendAndIfApplicable(StringBuffer query) {
        if (!query.toString().endsWith("AND "))
            if (!query.toString().isEmpty())
                query.append(" AND ");
    }

    protected void appendFileIdIfProvided(StringBuffer query) {
        if(fileId!=null)
        {
            appendAndIfApplicable(query);
            query.append("file_id");
            query.append(":");
            query.append(fileId);
        }
    }

    protected void appendKeywordsIfProvided(StringBuffer query,List<Integer>keywordIDs) {

        if(!keywordIDs.isEmpty()) {
            appendAndIfApplicable(query);
            query.append("keyword_ids");
            query.append(":");
            query.append("(");
            for(Integer keyword:keywordIDs) {
                query.append(" ");
                query.append(keyword);
                query.append(" ");
                query.append("OR");
            }
            query.replace(query.lastIndexOf("OR"), query.lastIndexOf("OR")+2,"");
            query.append(")");
        }
    }
    protected void appendFileIdsForUser(StringBuffer query, MisbarUser user) {
        if(fileId==null)
        {
            List<String> fileIds = new ArrayList<String>();
            for(MisbarFile file:user.getFiles())
            {
                fileIds.add(file.getId()+"");
            }
            appendAndIfApplicable(query);
            if(user.getFiles().size()>0)
            {
                query.append("file_id:(").append(StringUtils.join(fileIds, " OR ")).append(")");
            }
            else
                query.append("file_id:-1");
        }
    }

    public int getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(int cacheTime) {
        this.cacheTime = cacheTime;
    }

    public boolean isCacheable() {
        return isCacheable;
    }

    public void setCacheable(boolean cacheable) {
        isCacheable = cacheable;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }
}