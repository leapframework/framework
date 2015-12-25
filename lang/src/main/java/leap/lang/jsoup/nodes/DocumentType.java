package leap.lang.jsoup.nodes;


import leap.lang.jsoup.helper.StringUtil;
import leap.lang.jsoup.helper.Validate;
import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.Node;

/**
 * A {@code <!DOCTYPE>} node.
 */
public class DocumentType extends Node {
    // todo: quirk mode from publicId and systemId

	private final String declaration;
	
    /**
     * Create a new doctype element.
     * @param name the doctype's name
     * @param publicId the doctype's public ID
     * @param systemId the doctype's system ID
     * @param baseUri the doctype's base URI
     */
    public DocumentType(String name, String publicId, String systemId, String baseUri) {
    	this(null,name,publicId,systemId,baseUri);
    }
    
    public DocumentType(String declaration, String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        Validate.notEmpty(name);
        this.declaration = declaration;
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }

    @Override
    public String nodeName() {
        return "#doctype";
    }

    @Override
    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
    	if(null == declaration){
    		accum.append("<!DOCTYPE ");	
    	}else{
    		accum.append("<!").append(declaration).append(" ");
    	}
        
        accum.append(attr("name"));
        if (!StringUtil.isBlank(attr("publicId")))
            accum.append(" PUBLIC \"").append(attr("publicId")).append("\"");
        if (!StringUtil.isBlank(attr("systemId")))
            accum.append(" \"").append(attr("systemId")).append("\"");
        accum.append('>');
    }

    @Override
    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
    }
}
