/**
 * Copyright © 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.io.IOException;
import java.io.Serial;
import java.io.Writer;
import java.util.EnumSet;
import java.util.Set;

import static ch.digitalfondue.jfiveparse.Common.*;

/**
 * Implement (mostly) <a href=
 * "https://html.spec.whatwg.org/multipage/syntax.html#serialising-html-fragments"
 * >https://html.spec.whatwg.org/multipage/syntax.html#serialising-html-
 * fragments</a>
 */
public class HtmlSerializer implements NodesVisitor<Node> {

    protected final Appendable appendable;
    protected final boolean transformEntities;
    protected final boolean hideEmptyAttributeValue;
    protected final boolean scriptingDisabled;
    protected final boolean printOriginalAttributeQuote;
    protected final boolean printOriginalAttributeCase;
    protected final boolean printOriginalTagName;

    public HtmlSerializer(Appendable appendable, Set<Option> options) {
        this.appendable = appendable;
        this.transformEntities = !options.contains(Option.DONT_TRANSFORM_ENTITIES);
        this.hideEmptyAttributeValue = options.contains(Option.HIDE_EMPTY_ATTRIBUTE_VALUE);
        this.scriptingDisabled = options.contains(Option.SCRIPTING_DISABLED);
        this.printOriginalAttributeQuote = options.contains(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE);
        this.printOriginalAttributeCase = options.contains(Option.PRINT_ORIGINAL_ATTRIBUTES_CASE);
        this.printOriginalTagName = options.contains(Option.PRINT_ORIGINAL_TAG_CASE);
    }

    protected String serializeAttributeName(AttributeNode attribute) {
        String lowercaseName = attribute.getName();
        String name = printOriginalAttributeCase ? attribute.originalName : lowercaseName;
        String namespace = attribute.getNamespace();

        if (Node.NAMESPACE_XML.equals(namespace)) {
            return "xml:" + name;
        } else if (Node.NAMESPACE_XMLNS.equals(namespace)) {
            return "xmlns".equals(lowercaseName) ? "xmlns" : ("xmlns:" + name);
        } else if (Node.NAMESPACE_XLINK.equals(namespace)) {
            return "xlink:" + name;
        } else if (namespace != null) {
            return namespace + ":" + name;// TODO check
        } else {
            return name;
        }
    }

    protected String quoteCharacters(AttributeNode attribute) {
        if (printOriginalAttributeQuote) {
            if (attribute.attributeQuoteType == TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE) {
                return "";
            } else if (attribute.attributeQuoteType == TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE) {
                return "'";
            }
        }
        return "\"";
    }

    protected String escapeAttributeValue(AttributeNode attribute) {
        String s = attribute.getValue();
        if (s != null) {
            if (transformEntities) {
                s = s.replace("&", "&amp;").replace("\"", "&quot;");
            }
            s = s.replace(Character.valueOf(Characters.NO_BREAK_SPACE).toString(), "&nbsp;");
        }
        return s;
    }

    protected String escapeTextData(String s) {
        if (s != null) {
            if (transformEntities) {
                s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            }
            s = s.replace(Character.valueOf(Characters.NO_BREAK_SPACE).toString(), "&nbsp;");
        }
        return s;
    }

    protected static boolean skipEndTag(Element e) {
        return Node.NAMESPACE_HTML_ID == e.namespaceID && isNoEndTag(e.nodeNameID);
    }

    @Override
    public void start(Node node) {
        try {
            if (node instanceof Element e) {
                // TODO: for tag outside of html,mathml,svg namespace : use qualified name!
                appendable.append('<').append(getNodeName(e));
                for (AttributeNode attr : e.getAttributes()) {
                    appendable.append(' ').append(serializeAttributeName(attr));//

                    if ((hideEmptyAttributeValue || (printOriginalAttributeQuote && attr.attributeQuoteType == TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE))
                            && (attr.getValue() == null || attr.getValue().isEmpty())) {
                        continue;
                    }
                    appendable.append('=').append(quoteCharacters(attr)).append(escapeAttributeValue(attr)).append(quoteCharacters(attr));
                }
                appendable.append('>');
                if ((Common.isHtmlNS(e, Common.ELEMENT_PRE_ID) || Common.isHtmlNS(e, Common.ELEMENT_TEXTAREA_ID) || Common.isHtmlNS(e, Common.ELEMENT_LISTING_ID)) && //
                        e.hasChildNodes() && //
                        e.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    String text = ((Text) e.getFirstChild()).getData();
                    if (!text.isEmpty() && text.charAt(0) == Characters.LF) {
                        appendable.append(Characters.LF);
                    }
                }

            } else if (node instanceof Text t) {
                // TODO: handle the case when the nodes are created with scripting disabled
                Node parent = node.getParentNode();
                boolean literalAppend = false;
                if (parent instanceof Element p) {
                    literalAppend = Node.NAMESPACE_HTML_ID == p.namespaceID
                            && (isTextNodeParent(p.nodeNameID) || (Common.ELEMENT_NOSCRIPT_ID == p.nodeNameID && !scriptingDisabled));
                }
                appendable.append(literalAppend ? t.getData() : escapeTextData(t.getData()));

            } else if (node instanceof Comment comment) {
                appendable.append("<!--").append(comment.getData()).append("-->");
            } else if (node instanceof DocumentType dt) {
                // TODO: should append the rest of the attributes if present
                appendable.append("<!DOCTYPE ").append(dt.getName()).append('>');
            }
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
    }

    protected String getNodeName(Element e) {
        return printOriginalTagName ? e.originalNodeName : e.getNodeName();
    }

    @Override
    public void end(Node node) {
        try {
            if (node instanceof Element e) {
                if (!skipEndTag(e)) {
                    appendable.append("</").append(getNodeName(e)).append(">");
                }
            }
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
    }

    public static class SerializationException extends RuntimeException {

        @Serial
        private static final long serialVersionUID = -2182908125163112627L;

        public SerializationException(Throwable t) {
            super(t);
        }

    }

    public static void serialize(Node node, Writer writer) throws IOException {
        serialize(node, EnumSet.noneOf(Option.class), writer);
    }

    public static void serialize(Node node, Set<Option> options, Writer writer) throws IOException {
        node.traverseWithCurrentNode(new HtmlSerializer(writer, options));
        writer.flush();
    }

    public static String serialize(Node node) {
        return serialize(node, EnumSet.noneOf(Option.class));
    }

    public static String serialize(Node node, Set<Option> options) {
        StringBuilder sb = new StringBuilder();
        node.traverseWithCurrentNode(new HtmlSerializer(sb, options));
        return sb.toString();
    }

    private static boolean isNoEndTag(int nodeName) {
        return switch (nodeName) {
            case ELEMENT_AREA_ID, ELEMENT_BASE_ID, ELEMENT_BASEFONT_ID, ELEMENT_BGSOUND_ID,
                 ELEMENT_BR_ID, ELEMENT_COL_ID, ELEMENT_EMBED_ID, ELEMENT_FRAME_ID, ELEMENT_HR_ID,
                 ELEMENT_IMG_ID, ELEMENT_INPUT_ID, ELEMENT_KEYGEN_ID,
                 ELEMENT_LINK_ID, ELEMENT_META_ID, ELEMENT_PARAM_ID, ELEMENT_SOURCE_ID, ELEMENT_TRACK_ID,
                 ELEMENT_WBR_ID -> true;
            default -> false;
        };
    }

    private static boolean isTextNodeParent(int nodeNameId) {
        return switch (nodeNameId) {
            case ELEMENT_STYLE_ID, ELEMENT_SCRIPT_ID, ELEMENT_XMP_ID, ELEMENT_IFRAME_ID, ELEMENT_NOEMBED_ID,
                 ELEMENT_NOFRAMES_ID, ELEMENT_PLAINTEXT_ID -> true;
            default -> false;
        };
    }
}
