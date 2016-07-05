package leap.lang.xml;

import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;

import leap.lang.Enumerable;
import leap.lang.Strings;
import leap.lang.collection.UnmodifiableIteratorBase;

import org.w3c.dom.Element;

final class XmlUtils {
	
	static String getElementText(Element element){
		return element.getTextContent();
	}

	public static String escapeAttributeValue(String unescaped) {
		return escapeElementValue(unescaped); // TODO for now
	}

	public static String escapeElementValue(String unescaped) {
		return unescaped.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;").replace("\"", "&quot;");
	}
	
	static class DepthFirstIterator<T> extends UnmodifiableIteratorBase<T> {

		private final Function<T,Enumerable<T>> childrenFn;
		private final Stack<T> stack = new Stack<T>();

		public DepthFirstIterator(T startingNode, Function<T,Enumerable<T>> childrenFn) {
			this.childrenFn = childrenFn;
			this.stack.add(startingNode);
		}
		
		@Override
        protected T computeNext() {
			// first child
			for (T child : childrenFn.apply(stack.peek())) {
				stack.push(child);
				return child;
			}

			// no children
			while (stack.size() > 1) {
				T currentNode = stack.pop();

				// look for next sibling
				boolean foundSelf = false;
				for (T sibling : childrenFn.apply(stack.peek())) {
					if (foundSelf) {
						stack.push(sibling);
						return sibling;
					}
					if (sibling.equals(currentNode)) {
						foundSelf = true;
					}
				}
				// no sibling found, move up and try again
			}
			
			return endOfData();
        }
	}
	
	static final class Predicates {

		public static Predicate<String> endsWith(final String suffix) {
			return new Predicate<String>() {
				public boolean test(String input) {
					return input.endsWith(suffix);
				}
			};
		}

		public static Predicate<String> startsWith(final String prefix) {
			return new Predicate<String>() {
				public boolean test(String input) {
					return input.startsWith(prefix);
				}
			};
		}

		public static <T extends XmlNamed> Predicate<T> xnameEquals(final XmlNamed xname) {
			return new Predicate<T>() {
				public boolean test(T input) {
					return Strings.equals(xname.prefix(), input.prefix()) && Strings.equals(xname.name(), input.name());
				}
			};
		}

		public static <T extends XmlNamed> Predicate<T> xnameEquals(final String name) {
			return new Predicate<T>() {
				public boolean test(T input) {
					return Strings.equals(input.name(), name);
				}
			};
		}
		
		public static <T extends XmlNamed> Predicate<T> xnameEqualsWithPrefix(final String name) {
			return new Predicate<T>() {
				public boolean test(T input) {
					return !Strings.isEmpty(input.prefix()) && Strings.equals(input.name(), name);
				}
			};
		}
		
		public static <T extends XmlNamed> Predicate<T> xnameEquals(final String prefix,final String name) {
			return new Predicate<T>() {
				public boolean test(T input) {
					return Strings.equals(prefix, input.prefix()) && Strings.equals(input.name(), name);
				}
			};
		}		
		
		public static <T> Predicate<T> not(final Predicate<T> predicate) {
			return new Predicate<T>() {
				public boolean test(T input) {
					return !predicate.test(input);
				}
			};
		}
	}
}
