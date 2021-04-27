package leap.web.assets;

import leap.lang.resource.Resource;

import java.util.Locale;

/**
 * @author kael
 * 2021-04-27
 */
public interface AssetDynamicResourceResolver {
    /**
     * resolve dynamic asset resource
     * @return <code>null</code> if unsupported this path
     */
    Resource resolve(String path, Locale locale, Resource dir);

}
