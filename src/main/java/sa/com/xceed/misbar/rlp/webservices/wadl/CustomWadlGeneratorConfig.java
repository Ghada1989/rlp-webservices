package sa.com.xceed.misbar.rlp.webservices.wadl;

import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Fahad Najib
 * Date: 4/23/13
 * Time: 4:03 PM
 */
public class CustomWadlGeneratorConfig extends WadlGeneratorConfig
{
    @Override
    public List<WadlGeneratorDescription> configure() {
        return generator(CustomWadlGenerator.class).descriptions();
    }
}

