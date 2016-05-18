package sa.com.xceed.misbar.rlp.webservices;

import com.github.jmkgreen.morphia.logging.MorphiaLoggerFactory;
import com.github.jmkgreen.morphia.logging.slf4j.SLF4JLogrImplFactory;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import sa.com.xceed.misbar.dao.FilesMongoDAO;
import sa.com.xceed.misbar.dao.MisbarUserMongoDAO;
import sa.com.xceed.misbar.model.files.MisbarFile;
import sa.com.xceed.misbar.model.files.MisbarUserRole;
import sa.com.xceed.misbar.rlp.webservices.wadl.CustomWadlGeneratorConfig;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;


/**
 * Created with IntelliJ IDEA.
 * User: Fahad Najib
 * Date: 3/24/13
 * Time: 12:35 PM
 */
public class JerseyHttpsEnabled
{

    static Server server = new Server(8099);
    private static ArrayList<String> idList = new ArrayList<String>();
    private static String keyStorePath = JerseyHttpsEnabled.class.getClassLoader().getResource("misbar.keystore").getPath();
    private static String hostName = "localhost";//app-dev.misbaronline.com

    public static void main(String[] args) throws Exception
    {
        startServer(true,"applicationContext.xml");
       // hitWebservice();
    }

    public static void startServer(boolean join,String appContextConfig) throws Exception
    {
        if(join)
            MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory.class);
        ServletHolder sh = new ServletHolder(new com.sun.jersey.spi.spring.container.servlet.SpringServlet());
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "com.yammer.metrics.jersey, sa.com.xceed.misbar.webservices");

        //CORS support
        sh.setInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters",ResponseCorsFilter.class.getCanonicalName());

        sh.setInitParameter("com.sun.jersey.spi.container.ResourceFilters"
                ,"com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory,sa.com.xceed.misbar.webservices.validation.RequiredParamResourceFilterFactory");

        //custom WADL Generator
        sh.setInitParameter("com.sun.jersey.config.property.WadlGeneratorConfig", CustomWadlGeneratorConfig.class.getCanonicalName());

        //POJO mapper
        sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature","true");

        ServletContextHandler context = new ServletContextHandler(server,"/", ServletContextHandler.SESSIONS);

        context.addEventListener(new ContextLoaderListener());
        context.addEventListener(new RequestContextListener());
        context.setInitParameter("contextConfigLocation","classpath:"+appContextConfig);


        ServletHolder metricsServlet = new ServletHolder(new com.yammer.metrics.reporting.MetricsServlet());
        metricsServlet.setInitParameter("metrics-uri","/metrics");
        metricsServlet.setInitParameter("show-jvm-metrics","false");

        context.addServlet(sh,"/*");
        context.addServlet(metricsServlet,"/Metrics/*");
        context.setSecurityHandler(basicAuth(appContextConfig));

        SslContextFactory theSSLFactory = new SslContextFactory();
        theSSLFactory.setKeyStoreInputStream(JerseyHttpsEnabled.class.getClassLoader().getResourceAsStream("misbar.keystore"));
//        theSSLFactory.setKeyStorePath("classpath:misbar.keystore");
        theSSLFactory.setKeyManagerPassword("misbar");
        theSSLFactory.setKeyStorePassword("misbar");
        theSSLFactory.setTrustStorePassword("pwd");

        SslSocketConnector connector = new SslSocketConnector(theSSLFactory);
        connector.setPort(8099);

        SocketConnector socketConnector = new SocketConnector();
        socketConnector.setPort(8020);

        server.setConnectors(new Connector[] { connector,socketConnector });
        server.setHandler(context);
        server.start();
       // warmUp();

        if(join)
            server.join();
    }

    private static final SecurityHandler basicAuth(String appContextConfig) {
        ApplicationContext context = new ClassPathXmlApplicationContext(appContextConfig);
        MisbarUserMongoDAO userMongoDAO = context.getBean(MisbarUserMongoDAO.class);
        MongoLoginService login = new MongoLoginService(userMongoDAO);

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);

        //These are the roles that are allowed in system, maybe we can move them out to db, it
        // will just a flat table with these entries.
        String roles[] = new String[MisbarUserRole.values().length];
        int count=0;
        for(MisbarUserRole misbarUserRole : MisbarUserRole.values()){
            roles[count++] = misbarUserRole.name();
        }
        constraint.setRoles(roles);
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setMethodOmissions(new String[]{"OPTIONS"});
        cm.setPathSpec("/*");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("MisbarRealm");
        csh.addConstraintMapping(cm);
        csh.setLoginService(login);

        return csh;
    }

    public static void stopServer() throws Exception {
        server.stop();
    }

    public static void warmUp(){
        System.out.println("Inside warmup");
        System.out.println("calling fileids");
        getFileIds();
        System.out.println("Calling file summaries");

        //getFilesSummaries();


        System.out.println("Finish file summaries");
        System.out.println("Calling fb trends");
        getFacebookTrends();
        System.out.println("Finish fb Trends");
        System.out.println("fb posts");
        getFacebookPosts();
        System.out.println("Finish fn posts");
        System.out.println("TW links");
        getTwiiterLinks();
        System.out.println("Finish TW links");
    }

    private static void printTime(int id, long startTime, String service) {
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Elapsed Time for "+id+" in milliseconds After "+service+" Stats "+elapsedTime);
        System.out.println("Elapsed Time for "+id+" in seconds After "+service+" Stats "+elapsedTime/1000);
        System.out.println("Elapsed Time for "+id+" in minutes After "+service+" Stats "+(elapsedTime/1000)/60);
    }


//    public static void hitWebservice(){
//        //String keyStorePath = JerseyHttpsEnabled.class.getClassLoader().getResource("misbar.keystore").getPath();
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~ inside hit web service");
//        String output = given().auth().basic("admin@xceed", "123").keystore(keyStorePath, "misbar").
//                get("http://localhost:8010/twitter/stats/summary?file_id=84").asString();
//
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~ output " + output);
//    }

    private static void getFileIds(){
        List<MisbarFile> misbarFileList = FilesMongoDAO.getInstance().getActiveFiles();
        for(int i=0; i< misbarFileList.size() ; i++){
            int fileId = 0;
            fileId =  misbarFileList.get(i).getId();
            idList.add(Integer.toString(fileId));
        }

    }

    public static void getFilesSummaries(){
        //idList = getFileIds();
        if(!idList.isEmpty()){
        for(int j =0; j<idList.size(); j++){
           // if(!idList.get(j).equals("112")){
            long startTime = System.currentTimeMillis();
                String output = given().auth().basic("admin@xceed", "xceed$2011").keystore(keyStorePath, "misbar").
                        get("http://"+hostName+":8010/twitter/stats/summary?file_id=" + idList.get(j)).asString();
            printTime(Integer.parseInt(idList.get(j)),startTime," getFilesSummaries() ");
            System.out.println( j +" This is the output : "+output +" fot the file: " + idList.get(j));
            }
       // }
        }
    }

    public static void getFacebookTrends(){
        String output = given().auth().basic("admin@xceed", "123").keystore(keyStorePath, "misbar").
                get("http://"+hostName+":8010/files/trends?source=FACEBOOK").asString();
    }
    public static void getFacebookPosts(){
        if(!idList.isEmpty()){
            for(int j =0; j<idList.size(); j++){
                String output = given().auth().basic("admin@xceed", "123").keystore(keyStorePath, "misbar").
                        get("http://"+hostName+":8010/facebook/posts?sort_direction=Asc&facebook_posts_sort_by=DATE&file_id=" + idList.get(j) + "&count=10&start_index=0").asString();
            }
        }
    }

    public static void getTwiiterLinks(){
        if(!idList.isEmpty()){
            for(int j =0; j<idList.size(); j++){
                String output = given().auth().basic("admin@xceed", "123").keystore(keyStorePath, "misbar").
                        get("http://"+hostName+":8010/twitter/stats/links?file_id=" + idList.get(j) + "&count=10&start_index=0").asString();
            }
        }
    }

}
