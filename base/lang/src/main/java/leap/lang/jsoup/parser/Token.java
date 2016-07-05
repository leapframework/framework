package leap.lang.jsoup.parser;

import leap.lang.jsoup.helper.Validate;
import leap.lang.jsoup.nodes.Attribute;
import leap.lang.jsoup.nodes.Attributes;


/**
 * Parse tokens for the Tokeniser.
 */
abstract class Token {
    TokenType type;

    private Token() {
    }
    
    String tokenType() {
        return this.getClass().getSimpleName();
    }

    static class Doctype extends Token {
    	final String		declaration;
        final StringBuilder name = new StringBuilder();
        final StringBuilder publicIdentifier = new StringBuilder();
        final StringBuilder systemIdentifier = new StringBuilder();
        boolean forceQuirks = false;

        Doctype() {
        	this(null);
        }
        
        Doctype(String declaration){
        	this.type = TokenType.Doctype;
        	this.declaration = declaration;
        }
        
        String getDeclaration(){
        	return declaration;
        }

        String getName() {
            return name.toString();
        }

        String getPublicIdentifier() {
            return publicIdentifier.toString();
        }

        public String getSystemIdentifier() {
            return systemIdentifier.toString();
        }

        public boolean isForceQuirks() {
            return forceQuirks;
        }
    }

    static abstract class Tag extends Token {
        protected String tagName;
        private String pendingAttributeName; // attribute names are generally caught in one hop, not accumulated
        private StringBuilder pendingAttributeValue; // but values are accumulated, from e.g. & in hrefs
        private java.lang.Character pendingAttributeQuotedCharacter = '"';

        boolean selfClosing = false;
        Attributes attributes; // start tags get attributes on construction. End tags get attributes on first new attribute (but only for parser convenience, not used).

        private boolean parserCreated;//true means this tag was created by parser,not exists in the origianl html content.
        
        void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                Attribute attribute;
                if (pendingAttributeValue == null)
                    attribute = new Attribute(pendingAttributeName, "", pendingAttributeQuotedCharacter);
                else
                    attribute = new Attribute(pendingAttributeName, pendingAttributeValue.toString(), pendingAttributeQuotedCharacter);
                attributes.put(attribute);
            }
            pendingAttributeName = null;
            pendingAttributeQuotedCharacter = '"';
            if (pendingAttributeValue != null)
                pendingAttributeValue.delete(0, pendingAttributeValue.length());
        }

        void finaliseTag() {
            // finalises for emit
            if (pendingAttributeName != null) {
                // todo: check if attribute name exists; if so, drop and error
                newAttribute();
            }
        }

        String name() {
            Validate.isFalse(tagName.length() == 0);
            return tagName;
        }

        Tag name(String name) {
            tagName = name;
            return this;
        }

        boolean isSelfClosing() {
            return selfClosing;
        }

        boolean isParserCreated() {
			return parserCreated;
		}

		Tag createdByParser(){
			this.parserCreated = true;
			return this;
		}

		@SuppressWarnings({"TypeMayBeWeakened"})
        Attributes getAttributes() {
            return attributes;
        }

        // these appenders are rarely hit in not null state-- caused by null chars.
        void appendTagName(String append) {
            tagName = tagName == null ? append : tagName.concat(append);
        }

        void appendTagName(char append) {
            appendTagName(String.valueOf(append));
        }

        void appendAttributeName(String append) {
            pendingAttributeName = pendingAttributeName == null ? append : pendingAttributeName.concat(append);
        }
        
        void setPendingAttributeQuotedCharacter(java.lang.Character c) {
        	this.pendingAttributeQuotedCharacter = c;
        }

        void appendAttributeName(char append) {
            appendAttributeName(String.valueOf(append));
        }

        void appendAttributeValue(String append) {
            ensureAttributeValue();
            pendingAttributeValue.append(append);
        }

        void appendAttributeValue(char append) {
            ensureAttributeValue();
            pendingAttributeValue.append(append);
        }

        void appendAttributeValue(char[] append) {
            ensureAttributeValue();
            pendingAttributeValue.append(append);
        }

        private final void ensureAttributeValue() {
            if (pendingAttributeValue == null)
                pendingAttributeValue = new StringBuilder();
        }
    }

    static class StartTag extends Tag {
        StartTag() {
            super();
            attributes = new Attributes();
            type = TokenType.StartTag;
        }

        StartTag(String name) {
            this();
            this.tagName = name;
        }
        
        StartTag(String name,boolean createdByParser) {
            this();
            this.tagName = name;
            if(createdByParser) {
            	this.createdByParser();
            }
        }

        StartTag(String name, Attributes attributes) {
            this();
            this.tagName = name;
            this.attributes = attributes;
        }
        
        StartTag(String name, Attributes attributes, boolean createdByParser) {
            this();
            this.tagName = name;
            this.attributes = attributes;
            if(createdByParser){
                this.createdByParser();
            }
        }

        @Override
        public String toString() {
            if (attributes != null && attributes.size() > 0)
                return "<" + name() + " " + attributes.toString() + ">";
            else
                return "<" + name() + ">";
        }
    }

    static class EndTag extends Tag{
        EndTag() {
            super();
            type = TokenType.EndTag;
        }

        EndTag(String name) {
            this();
            this.tagName = name;
        }

        @Override
        public String toString() {
            return "</" + name() + ">";
        }
    }

    static class Comment extends Token {
        final StringBuilder data = new StringBuilder();
        boolean bogus = false;

        Comment() {
            type = TokenType.Comment;
        }

        String getData() {
            return data.toString();
        }

        @Override
        public String toString() {
            return "<!--" + getData() + "-->";
        }
    }

    static class Character extends Token {
        private final String data;

        Character(String data) {
            type = TokenType.Character;
            this.data = data;
        }

        String getData() {
            return data;
        }

        @Override
        public String toString() {
            return getData();
        }
    }

    static class EOF extends Token {
        EOF() {
            type = Token.TokenType.EOF;
        }
    }

    boolean isDoctype() {
        return type == TokenType.Doctype;
    }

    Doctype asDoctype() {
        return (Doctype) this;
    }

    boolean isStartTag() {
        return type == TokenType.StartTag;
    }

    StartTag asStartTag() {
        return (StartTag) this;
    }

    boolean isEndTag() {
        return type == TokenType.EndTag;
    }

    EndTag asEndTag() {
        return (EndTag) this;
    }

    boolean isComment() {
        return type == TokenType.Comment;
    }

    Comment asComment() {
        return (Comment) this;
    }

    boolean isCharacter() {
        return type == TokenType.Character;
    }

    Character asCharacter() {
        return (Character) this;
    }

    boolean isEOF() {
        return type == TokenType.EOF;
    }

    enum TokenType {
        Doctype,
        StartTag,
        EndTag,
        Comment,
        Character,
        EOF
    }
}
