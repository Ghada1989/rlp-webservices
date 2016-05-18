package sa.com.xceed.misbar.rlp.util;

import com.basistech.rlp.*;
import com.basistech.rosette.dm.AnnotatedText;
import com.basistech.rosette.dm.tools.AraDmConverter;
import com.basistech.util.LanguageCode;
import com.basistech.util.Pathnames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sa.com.xceed.misbar.utils.MisbarProperties;

import java.io.File;
import java.io.IOException;

public class BasisEntityExtractor
{

    private static final String BT_ROOT_DIRECTORY;
    private static final String ENV_CONFIG;
    private static final String ENTITY_EXTRACTION_CONTEXT_PATH;
    private static Logger logger = LoggerFactory.getLogger(BasisEntityExtractor.class);


    private static RLPEnvironment env;
    private ContextParameters contextParam;
    private RLPContext rlpContext;

    static
    {
        BT_ROOT_DIRECTORY ="C:\\RLP";
        ENV_CONFIG = "C:\\RLP\\rlp\\etc\\rlp-environment.xml";
        ENTITY_EXTRACTION_CONTEXT_PATH ="C:\\RLP\\rlp\\samples\\etc\\rlp-ne-context.xml";
        initializeEnvironment();
    }

    public BasisEntityExtractor()
    {

    }

    private static void initializeEnvironment()
    {
        Pathnames.setBTRootDirectory(BT_ROOT_DIRECTORY);
        EnvironmentParameters envParams = new EnvironmentParameters();
        envParams.setEnvironmentDefinition(new File(ENV_CONFIG));
        env = new RLPEnvironment(envParams);
        try
        {
            env.initialize();
        }
        catch ( RLPException e )
        {
            logger.error(e.getMessage());
        }
    }

    private void setupContextDefinitionForEntityExtraction()
    {
        contextParam = new ContextParameters();
        try
        {
            contextParam.setContextDefinition(new File(ENTITY_EXTRACTION_CONTEXT_PATH));
        }
        catch ( IOException e )
        {
            logger.error(e.getMessage());
        }
    }

    private void setupRLP()
    {
        setupContextDefinitionForEntityExtraction();
        rlpContext = null;
        try
        {
            rlpContext = env.getContext(contextParam);
            rlpContext.setProperty("com.basistech.jsonw.skip", "true");
            rlpContext.setProperty("com.basistech.er.resolve_entities", "true");
        }
        catch ( NullPointerException nullPointerException )
        {
            logger.error(nullPointerException.getMessage());
        }
        catch ( RLPException e )
        {
            logger.error(e.getMessage());
        }
    }

    public AnnotatedText extractEntitiesFromText( String text )
    {
        setupRLP();
        AnnotatedText annotatedText;
        try
        {
            rlpContext.process(text, LanguageCode.UNKNOWN);
            annotatedText = getAnnotatedText();
        }
        catch ( RLPException e )
        {
            annotatedText = processTextWithDefaultLanguage(text);
        }
        catch ( Exception ex )
        {
            annotatedText = null;
        }
        return annotatedText;
    }

    private AnnotatedText processTextWithDefaultLanguage( String text )
    {
        try
        {
            rlpContext.process(text, LanguageCode.ARABIC);
            return getAnnotatedText();
        }
        catch ( Exception e )
        {
            logger.error(e.getMessage());
            return null;
        }
    }

    private AnnotatedText getAnnotatedText()
    {
        ResultAccess resultAccess = new ResultAccess(rlpContext);
        if ( resultAccess != null )
        {
        return AraDmConverter.convert(resultAccess);
        }
        else
        {
            throw new RuntimeException();
        }
    }

    public void close() {
        rlpContext.close();
        env.close();
    }

//    private void enforceMultipleInstancesOfNamedEntityReturnedWithSameEntityType( NamedEntityIteratorResultAccess neResultAccess )
//    {
//        neResultAccess.setEnforceConsistentType(true);
//    }

//    public List<BasisEntity> extractEntitiesFromText( String text )
//    {
//        setupRLP();
//        List<BasisEntity> entities;
//        try
//        {
//            rlpContext.process(text, LanguageCode.UNKNOWN);
//            entities = getExtractedEntities();
//        }
//        catch ( RLPException e )
//        {
//            entities = processTextWithDefaultLanguage(text);
//        }
//        catch ( Exception ex )
//        {
//            entities = new ArrayList<BasisEntity>();
//        }
//        return entities;
//    }
//
//    private List<BasisEntity> processTextWithDefaultLanguage( String text )
//    {
//        try
//        {
//            rlpContext.process(text, LanguageCode.ARABIC);
//            return getExtractedEntities();
//        }
//        catch ( Exception e )
//        {
//            logger.error(e.getMessage());
//            return new ArrayList<BasisEntity>();
//        }
//    }
//
//    private AnnotatedText getAnnotatedText()
//    {
//        ResultAccess resultAccess = new ResultAccess(rlpContext);
//        return AraDmConverter.convert(resultAccess);
//    }
//
//    private List<BasisEntity> getExtractedEntities()
//    {
//        ResultAccess resultAccess = new ResultAccess(rlpContext);
//        if ( resultAccess != null )
//        {
//            NamedEntityIteratorResultAccess neResultAccess = new NamedEntityIteratorResultAccess(resultAccess);
//            enforceMultipleInstancesOfNamedEntityReturnedWithSameEntityType(neResultAccess);
//            return iterateThroughResultAndGetEntities(neResultAccess);
//        }
//        else
//        {
//            throw new RuntimeException();
//        }
//    }
//
//    private void enforceMultipleInstancesOfNamedEntityReturnedWithSameEntityType( NamedEntityIteratorResultAccess neResultAccess )
//    {
//        neResultAccess.setEnforceConsistentType(true);
//    }
//
//    private List<BasisEntity> iterateThroughResultAndGetEntities( NamedEntityIteratorResultAccess neResultAccess )
//    {
//        List<BasisEntity> entities = new ArrayList<BasisEntity>();
//        NamedEntityData ned = new NamedEntityData();
//        while ( neResultAccess.next(ned) )
//        {
//            BasisEntity entity = new BasisEntity();
//            entity.setName(ned.getNormalizedNamedEntity());
//            entity.setSource(RLPNENameMap.toSourceString(ned.getSource()));
//            entity.setType(RLPNENameMap.toString(ned.getType()));
//            entities.add(entity);
//        }
//        return entities;
//    }
}
