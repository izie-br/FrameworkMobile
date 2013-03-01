package $package;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

${Imports}

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
#if ($interface)
#**#public interface ${Filename}  extends ${GenInterface}
#else
#**#public class ${Filename} extends ${GenImpl}
#end
{
#if ($implementation)

    public static final long serialVersionUID = ${GenImpl}.publicSerialVersionUID;

    public ${Filename} () {
        super();
    }

    public ${Filename} (${constructorArgsDecl}) {
        super(${constructorArgs});
    }
#end
}

