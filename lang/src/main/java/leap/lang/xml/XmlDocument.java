package leap.lang.xml;

/**
 * A simple interface of xml document.
 */
public interface XmlDocument {

	/**
	 * Returns the location of this document.
	 */
	String location();

	/**
	 * Returns the root element.
	 */
	XmlElement rootElement();

	/**
	 * Returns a string indicates the xml content of this document.
	 */
	String toXml();

}