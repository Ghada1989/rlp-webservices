package sa.com.xceed.misbar.rlp.util;


import com.basistech.rosette.dm.AnnotatedText;
import com.basistech.rosette.res.*;
import sa.com.xceed.misbar.model.entityextractor.BasisEntity;
import sa.com.xceed.misbar.utils.MisbarProperties;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class BasisEntityResolver
{


    private static final String BT_SEEDED_DIRECTORY;
    private static final String BT_KNOWLEDGE_BASE;
    private static final String BT_WIKIDATA_INDEX;
    private static final String BT_RESOLVER_LICENSE;
    private EntityResolver resolver;
    private WikiKB kb;
    private static Random random = new Random();

    static
    {
        BT_SEEDED_DIRECTORY = "C:\\RES\\res-0.10.0\\indexes\\wikidata-seeded-index-9";
        BT_KNOWLEDGE_BASE = "C:\\RES\\res-0.10.0\\kb\\wikidatakb-3.bin";
        BT_WIKIDATA_INDEX= "C:\\RES\\res-0.10.0\\indexes";
        BT_RESOLVER_LICENSE = "C:\\RES\\res-0.10.0\\licenses\\res-license.xml";
    }

    public List<BasisEntity> extractResolvedEntity( String text )
    {
        BasisEntityExtractor entityExtractor = null;
        try
        {
            setupRESBuilder();
            entityExtractor = new BasisEntityExtractor();
            AnnotatedText annotatedText = entityExtractor.extractEntitiesFromText(text);
            String docID = getRandomId();
            RESInputDocument inputDoc = new RESInputDocument(docID, annotatedText);
            RESOutput resOutput = resolver.resolve(inputDoc);
            return displayResult(resOutput);
        }
        catch ( Exception e )
        {
            return new ArrayList<BasisEntity>();
        }
//        finally
//        {
//            shutdownResolver();
//            if ( entityExtractor != null )
//            {
//                entityExtractor.close();
//            }
//            return new ArrayList<BasisEntity>();
//        }
    }

    public String getRandomId()
    {
        return new BigInteger(130, random).toString(32);
    }

    private void shutdownResolver()
    {
        if ( resolver != null )
        {
            try
            {
                resolver.shutdown();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }
    }

    private void setupRESBuilder()
            throws Exception
    {
        File seededIndexDir = new File(BT_SEEDED_DIRECTORY);
        kb = new WikiKB(new File(BT_KNOWLEDGE_BASE));

        EntityResolver.Builder builder = EntityResolver.Builder.newLinkingOnlyBuilder();
        builder.addDefaultConfiguration();
        builder.configureWikidata(new File(BT_WIKIDATA_INDEX), EnumSet.allOf(SupportedWikiLanguage.class));
        // Set the RES license.  For sample purposes, we expect you to have placed
        // your license file in res/licenses/.  The license can also be set without
        // an api call by just placing "res-license.xml" in your classpath.
        builder.setLicense(new File(BT_RESOLVER_LICENSE));

        // We recommend the default configuration. This uses a model trained on news
        // articles in the supported RES languages.  It also configures the resolver to
        // resolve PERSON, ORGANIZATION, and LOCATION mentions from REX.
        builder.addDefaultConfiguration();

        // appendMode true will cause subsequent runs to pick off where
        // the previous run left off.  Setting it to false will overwrite the data
        // for each run.
        builder.setAppendMode(false);

        // There may be multiple seeded indexes. We ship one which was derived from dumps of
        // English, Spanish, Chinese, Japanese, and Arabic Wikipedia and Wikidata. Here we use all the languages.
        // We have a specific method to configure RES to resolve against Wikidata. In this example
        // we build an Entity Resolver capable of resolving against all supported languages.
        // If you are only processing English language documents you can configure RES
        // for English:
        // builder.configureWikidata(seededIndexDir, EnumSet.of(SupportedWikiLanguage.ENGLISH));
        builder.configureWikidata(seededIndexDir, EnumSet.allOf(SupportedWikiLanguage.class));

        // parallelism is the number of threads available to an internal ForkJoinPool.
        // We recommend setting this to the minimum of 8 and the number of cores on
        // your machine.
        builder.setParallelism(1);

        resolver = builder.build();

    }

    private List<BasisEntity> displayResult( RESOutput resOutput )
    {
        String docId = resOutput.getDocId();
        for ( ResolvedEntity r : resOutput.getResults() )
        {
            if ( r.isError() )
            {
                continue;
            }

            String entityId = r.getEntityId();
            System.out.println(String.format("%s\t%s\t%d\t%d\t%d\t%s", docId,
                    r.getEntityId(), r.getChainId(), r.getMentionSpan().getStart(),
                    r.getMentionSpan().getEnd(), r.getMentionSpan().getText()));

            // The entity ids in the seeded index refer to an external knowledge base (kb).
            // In this case, our seeded index uses ids from Wikidata.  You can fetch metadata
            // about the entity directly from live Wikidata, but we illustrate this with a
            // local cached version via the WikiKB class.
            WikiKB.Entry entry = kb.lookup(entityId);
            if ( entityId.startsWith("Q") && entry != null )
            {
                // Wikidata URL
                System.out.println("  wikidata URL: " + entry.getWikidataURL());

                // Localized labels
                System.out.println("  en label: " + entry.getLabel("en"));
                System.out.println("  zh label: " + entry.getLabel("zh"));

                // Localized descriptions
                System.out.println("  en description: " + entry.getDescription("en"));
                System.out.println("  zh description: " + entry.getDescription("zh"));

                // Wikipedia URLs
                System.out.println("  en wiki URL: " + entry.getWikipediaURL("en"));
                System.out.println("  zh wiki URL: " + entry.getWikipediaURL("zh"));

                // Freebase URL
                System.out.println("  Freebase URL: " + entry.getFreebaseURL());

                // Image URL
                System.out.println("  Image URL: " + entry.getImageURL());

                // Latitude/Longitude
                System.out.println("  Lat/Long: " + entry.getCoords());
            }
        }
        return null;
    }

    public static void main(String[] args){
        BasisEntityResolver resolver1= new BasisEntityResolver();
        resolver1.extractResolvedEntity("الملك سلمان يهني أمير منطة الرياض");
    }
}
