package sa.com.xceed.misbar.rlp.webservices.wadl;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.wadl.generators.WadlGeneratorJAXBGrammarGenerator;
import com.sun.research.ws.wadl.Option;
import com.sun.research.ws.wadl.Param;
import sa.com.xceed.misbar.rlp.webservices.annotation.Required;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.xml.namespace.QName;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: Fahad Najib
 * Date: 4/23/13
 * Time: 4:02 PM
 */

/**
 * To mention the required parameter in the WADL as "required=true"
 */
public class CustomWadlGenerator extends WadlGeneratorJAXBGrammarGenerator {

    @Override
    public Param createParam( AbstractResource r, AbstractMethod m, final Parameter p )
    {
        Param param = super.createParam(r,m,p);
        if(m!=null){
            Required requiredAnnotation = m.getAnnotation(Required.class);
            if(requiredAnnotation!=null){
                String requiredParams[] = requiredAnnotation.value();
                for(String requiredParam: requiredParams){
                    if(param.getName().equals(requiredParam)){
                        param.setRequired(true);
                    }
                }
            }
        }

        //Handle Enums
        if(p.getParameterClass().getEnumConstants()!=null)
        {
            param.setType(QName.valueOf(p.getParameterClass().getSimpleName()));
            for(Object o: p.getParameterClass().getEnumConstants())
            {
                Option option = new Option();
                option.setValue(o.toString());
                param.getOption().add(option);
            }
        }

        //Handle Lists
        if(p.getParameterClass()==List.class)
        {
            Class actualClass = (Class) ((ParameterizedTypeImpl)p.getParameterType()).getActualTypeArguments()[0];
            if(actualClass.getEnumConstants()!=null)
            {
                for(Object o: actualClass.getEnumConstants())
                {
                    Option option = new Option();
                    option.setValue(o.toString());
                    param.getOption().add(option);
                }
            }
            param.setType(QName.valueOf("List of "+actualClass.getSimpleName()));
        }
        return param;
    }
}
