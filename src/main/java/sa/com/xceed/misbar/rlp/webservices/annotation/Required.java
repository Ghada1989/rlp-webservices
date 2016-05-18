package sa.com.xceed.misbar.rlp.webservices.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: Fahad Najib
 * Date: 3/17/13
 * Time: 3:49 PM
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Required
{
      String[] value();
}
