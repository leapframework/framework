package leap.web.assets.css;

import leap.web.assets.Asset;
import leap.web.assets.AssetManager;
import leap.lang.css.CssUrlRewriter;

public class AssetCssUrlRewriter extends CssUrlRewriter {
    
    private final AssetManager manager;
    private final boolean      debug;
    
    public AssetCssUrlRewriter(AssetManager manager, String path, String css, boolean debug) {
        super(path, css);
        this.manager = manager;
        this.debug = debug;
    }

    @Override
    protected String path(String rewritedPath) {
        String assetPath = manager.prefixWithoutAssetsPath(rewritedPath);
        Asset asset = manager.getSource().getAsset(assetPath);
        if(null == asset) {
            return manager.getClientPath(rewritedPath);
        }else{
            return asset.getClientUrl(debug);
        }
    }
}