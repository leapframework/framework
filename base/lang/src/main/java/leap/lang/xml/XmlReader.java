package leap.lang.xml;

import leap.lang.Sourced;
import leap.lang.text.PlaceholderResolver;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Closeable;
import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * A wrapper interface of {@link XMLEventReader} to make it more easy to use.
 */
public interface XmlReader extends Closeable,Sourced {
	
	/**
	 * Returns a string indicates the {@link Sourced} and current line number of this reader.
	 */
	String getCurrentLocation();

    /**
     * Returns current event.
     */
    XMLEvent event();

	/**
	 * Sets to <code>true</code> will trim all the string value returned by this reader.
	 * 
	 * <p>
	 * Default value is <code>true</code>
	 */
	void setTrimAll(boolean trimAll);
	
	/**
	 * Returns is this reader trims all the string value.
	 */
	boolean isTrimAll();
	
	/**
	 * @see XMLEventReader#nextEvent()
	 */
    boolean next();

    /**
     * Loops inside current opened element until element end.
     */
    default void loopInsideElement(Runnable func) {
        QName elementName = getElementName();
        int elementCount = 1;

        while(next()) {
            if(isEndElement(elementName)) {
                if(elementCount == 1) {
                    return;
                }else{
                    elementCount --;
                }
            } else if(isStartElement(elementName)) {
                elementCount++;
            }
            func.run();
        }
    }

    boolean nextWhileNotEnd(QName elementName);
    
    boolean nextWhileNotEnd(String elementLocalName);
    
    boolean nextToStartElement();
    
    boolean nextToStartElement(String localName);
    
    boolean nextToStartElement(QName name);
    
    boolean nextToEndElement();
    
    boolean nextToEndElement(String localName);
    
    boolean nextToEndElement(QName name);
    
    /**
     * @see XMLEvent#isProcessingInstruction()
     */
    boolean isProcessingInstruction();
    
    /**
     * @see #isProcessingInstruction()
     * @see #getProcessingInstructionTarget()
     */
    boolean isProcessingInstruction(String target);

    /**
     * @see XMLEvent#isCharacters()
     */
    boolean isCharacters();

    String getCharacters();
    
    /**
     * @see XMLEvent#isStartElement()
     */
    boolean isStartElement();
    
    boolean isStartElement(QName name);
    
    boolean isStartElement(String localName);
    
    /**
     * @see XMLEvent#isEndElement()
     */
    boolean isEndElement();
    
    boolean isEndElement(QName name);
    
    boolean isEndElement(String localName);
    
    /**
     * @see XMLEvent#isEndDocument()
     */
    boolean isEndDocument();
    
    String getProcessingInstructionTarget();
    
    String getProcessingInstructionContent();
    
    /**
     * @see StartElement#getName()
     */
    QName getElementName();
    
    String getElementLocalName();
    
    /**
     * Returns current line number.
     */
	int getLineNumber();
	
    //get element text

    /**
     * Returns current element's text.
     * 
     * <p/>
     * Note : 
     * <strong>
     * The underlying reader will move to the end of current element.
     * 
     * so it will make this wrapper's state not inconsistent with the underlying reader.
     * 
     * you must call {@link #next()} after calling this method to make the state inconsistent 
     * 
     * between this wrapper and the underlying reader.
     * </strong>
     * 
     * @see XMLEventReader#getElementText()
     */
    String getElementTextAndEnd();
    
    default Integer getIntegerElementTextAndEnd() {
    	String s = getElementTextAndEnd();
    	if(null != s && s.length() > 0) {
    		return Integer.parseInt(s);
    	}
    	return null;
    }

    /**
     * Returns the attribute names as {@link Iterator}.
     */
    Iterator<String> getAttributeLocalNames();

    /**
     * Returns the attribute names as {@link Iterator}.
     */
    Iterator<QName> getAttributeNames();
    
    //get string attribute
    boolean hasAttribute(QName name);
    
    boolean hasAttribute(String localName);
    
    String getAttribute(QName name);
    
    String getAttribute(String localName);
    
    String getAttributeOrNull(QName name);
    
    String getAttributeOrNull(String localName);    
    
    String getAttribute(QName name,String defaultValue);
    
    String getAttribute(String localName,String defaultValue);
    
    //get attribute for type
    <T> T getAttribute(QName name,Class<T> targetType);
    
    <T> T getAttribute(String localName,Class<T> targetType);
    
    <T> T getAttribute(QName name,Class<T> targetType, T defaultValue);
    
    <T> T getAttribute(String localName,Class<T> targetType, T defaultValue);
    
    Boolean getBooleanAttribute(QName name);
    
    Boolean getBooleanAttribute(String localName);
    
    boolean getBooleanAttribute(QName name,boolean defaultValue);
    
    boolean getBooleanAttribute(String localName,boolean defaultValue);

    Integer getIntegerAttribute(QName name);
    
    Integer getIntegerAttribute(String localName);
    
    int getIntAttribute(QName name,int defaultValue);
    
    int getIntAttribute(String localName,int defaultValue);

    Float getFloatAttribute(QName name);

    Float getFloatAttribute(String localName);

    float getFloatAttribute(QName name,float defaultValue);

    float getFloatAttribute(String localName,float defaultValue);

    //get required
    String getRequiredElementTextAndEnd();

    String getRequiredAttribute(QName name);

    String getRequiredAttribute(String localName);

    <T> T getRequiredAttribute(QName name, Class<T> targetType);

    <T> T getRequiredAttribute(String localName, Class<T> targetType);

    boolean getRequiredBooleanAttribute(QName name);

    boolean getRequiredBooleanAttribute(String localName);

    int getRequiredIntAttribute(QName name);

    int getRequiredIntAttribute(String localName);

    //resolve element text
    /**
     * Sets the {@link PlaceholderResolver} used by all methods starts with <code>resolve</code>.
     *
     * @see #resolveElementTextAndEnd()
     * @see #resolveAttribute(QName)
     */
    void setPlaceholderResolver(PlaceholderResolver placeholderResolver);

    /**
     * Returns {@link PlaceholderResolver} defines in this reader.
     *
     * <p>
     * Returns <code>null</code> if no {@link PlaceholderResolver} defined in this reader.
     */
    PlaceholderResolver getPlaceholderResolver();
    
    String resolveElementTextAndEnd();
    
    String resolveAttribute(QName name);
    
    String resolveAttribute(String localName);
    
    String resolveAttribute(QName name,String defaultValue);
    
    String resolveAttribute(String localName,String defaultValue);
    
    <T> T resolveAttribute(QName name,Class<T> targetType);
    
    <T> T resolveAttribute(String localName,Class<T> targetType);
    
    <T> T resolveAttribute(QName name,Class<T> targetType, T defaultValue);
    
    <T> T resolveAttribute(String localName,Class<T> targetType, T defaultValue);
    
    Boolean resolveBooleanAttribute(QName name);
    
    Boolean resolveBooleanAttribute(String localName);
    
    boolean resolveBooleanAttribute(QName name,boolean defaultValue);
    
    boolean resolveBooleanAttribute(String localName,boolean defaultValue);
    
    Integer resolveIntegerAttribute(QName name);
    
    Integer resolveIntegerAttribute(String localName);
    
    int resolveIntAttribute(QName name,int defaultValue);
    
    int resolveIntAttribute(String localName,int defaultValue);

    float resolveFloatAttribute(QName name,int defaultValue);

    float resolveFloatAttribute(String localName,int defaultValue);

    //resolve required.

    String resolveRequiredElementTextAndEnd();

    String resolveRequiredAttribute(QName name);

    String resolveRequiredAttribute(String localName);

    <T> T resolveRequiredAttribute(QName name, Class<T> targetType);

    <T> T resolveRequiredAttribute(String localName, Class<T> targetType);

    boolean resolveRequiredBooleanAttribute(QName name);

    boolean resolveRequiredBooleanAttribute(String localName);

    int resolveRequiredIntAttribute(QName name);
    
    int resolveRequiredIntAttribute(String localName);

    /**
     * Resolves all the attributes and apply the function for each attribute(name, value).
     */
    void forEachResolvedAttributes(BiConsumer<QName,String> func);

    /**
     * Gets all the attributes and apply the function for each attribute(name, value).
     */
    void forEachAttributes(BiConsumer<QName,String> func);
}