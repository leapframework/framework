package leap.lang.xml;

import java.io.Closeable;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import leap.lang.Sourced;
import leap.lang.text.PlaceholderResolver;

/**
 * A wrapper interface of {@link XMLEventReader} to make it more easy to use.
 */
public interface XmlReader extends Closeable,Sourced {
	
	/**
	 * Returns a string indicates the {@link Sourced} and current line number of this reader.
	 */
	String getCurrentLocation();
	
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
    
    String getElementTextAndEndRequired();
    
    Iterator<String> getAttributeNames();
    
    //get string attribute
    boolean hasAttribute(QName name);
    
    boolean hasAttribute(String localName);
    
    String getAttribute(QName name);
    
    String getAttribute(String localName);
    
    String getAttributeOrNull(QName name);
    
    String getAttributeOrNull(String localName);    
    
    String getAttribute(QName name,String defaultValue);
    
    String getAttribute(String localName,String defaultValue);
    
    String getAttributeRequired(QName name);
    
    String getAttributeRequired(String localName);
    
    //get attribute for type
    <T> T getAttribute(QName name,Class<T> targetType);
    
    <T> T getAttribute(String localName,Class<T> targetType);
    
    <T> T getAttribute(QName name,Class<T> targetType, T defaultValue);
    
    <T> T getAttribute(String localName,Class<T> targetType, T defaultValue);
    
    <T> T getAttributeRequired(QName name,Class<T> targetType);
    
    <T> T getAttributeRequired(String localName,Class<T> targetType);    
    
    //get boolean attribute
    
    Boolean getBooleanAttribute(QName name);
    
    Boolean getBooleanAttribute(String localName);
    
    boolean getBooleanAttribute(QName name,boolean defaultValue);
    
    boolean getBooleanAttribute(String localName,boolean defaultValue);
    
    boolean getBooleanAttributeRequired(QName name);
    
    boolean getBooleanAttributeRequired(String localName);
    
    //get int attribute
    
    Integer getIntegerAttribute(QName name);
    
    Integer getIntegerAttribute(String localName);
    
    int getIntAttribute(QName name,int defaultValue);
    
    int getIntAttribute(String localName,int defaultValue);
    
    int getIntAttributeRequire(QName name);
    
    int getIntAttributeRequried(String localName);
    
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
    
    //resolve element text
    
    String resolveElementTextAndEnd();
    
    String resolveRequiredElementTextAndEnd();
    
    //resolve string attribute
    
    String resolveAttribute(QName name);
    
    String resolveAttribute(String localName);
    
    String resolveAttribute(QName name,String defaultValue);
    
    String resolveAttribute(String localName,String defaultValue);
    
    String resolveRequiredAttribute(QName name);
    
    String resolveRequiredAttribute(String localName);
    
    //resolve attribute for type
    <T> T resolveAttribute(QName name,Class<T> targetType);
    
    <T> T resolveAttribute(String localName,Class<T> targetType);
    
    <T> T resolveAttribute(QName name,Class<T> targetType, T defaultValue);
    
    <T> T resolveAttribute(String localName,Class<T> targetType, T defaultValue);
    
    <T> T resolveAttributeRequired(QName name,Class<T> targetType);
    
    <T> T resolveAttributeRequired(String localName,Class<T> targetType);      
    
    //resolve boolean attribute
    
    Boolean resolveBooleanAttribute(QName name);
    
    Boolean resolveBooleanAttribute(String localName);
    
    boolean resolveBooleanAttribute(QName name,boolean defaultValue);
    
    boolean resolveBooleanAttribute(String localName,boolean defaultValue);
    
    boolean resolveBooleanAttributeRequired(QName name);
    
    boolean resolveBooleanAttributeRequired(String localName);
    
    //resolve int attribute
    
    Integer resolveIntegerAttribute(QName name);
    
    Integer resolveIntegerAttribute(String localName);
    
    int resolveIntAttribute(QName name,int defaultValue);
    
    int resolveIntAttribute(String localName,int defaultValue);
    
    int resolveIntAttributeRequired(QName name);
    
    int resolveIntAttributeRequired(String localName);
}