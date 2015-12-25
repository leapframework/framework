package leap.lang.xml;

/**
 * A simple interface of xml element.
 */
public interface XmlElement {

	/**
	 * Returns the owner element of this element.
	 */
	XmlDocument document();

	/**
	 * Returns the first child element of the given name, or <code>null</code> if not found.
	 */
	XmlElement childElement(String localName);

	/**
	 * Returns the text of this element.
	 * 
	 * @see Element#getTextContent().
	 */
	String text();

}