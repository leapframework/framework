window.less.Parser.importer = function(path, currentFileInfo, callback, env) {
	
	log.debug("importing '" + path + "' from '" + currentFileInfo.filename + "'");
	
    if (!/^\//.test(path) && !/^\w+:/.test(path) && currentFileInfo.currentDirectory) {
    	path = currentFileInfo.currentDirectory + path;
    }
    
    if (path != null) {
        try {
            var parser = new (window.less.Parser)({optimization : env.optimization,
								                   paths : env.paths,
								                   filename : path,
								                   dumpLineNumbers : env.lineNumbers
			            						  });
            
            var content = String(importer.read(path));
            
            parser.parse(content, function(e, root) {
            	callback(e,root,path);	           	
            });
        } catch (e) {
        	callback(e,null,path);
        }
    }
};

function lessc(input,currentDirectory,filename) {
    var result = null;
    var parser = new less.Parser({processImports:true,
    							  relativeUrls : true,
    							  currentFileInfo:{
    								  currentDirectory:currentDirectory,
    								  filename:filename
    							  }
    							 });
    try{
        parser.parse(input, function(err, tree) {
            if(err == null ) {
                result = tree.toCSS();
            } else {
                result = err;
            }
        });
    }catch(err){
    	result = err;
    }
    return result;
};

function lessc1(){
	return lessc(input,currentDirectory,filename);
}