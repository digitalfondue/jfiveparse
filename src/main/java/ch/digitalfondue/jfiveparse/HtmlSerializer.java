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
import java.util.Set;

import static ch.digitalfondue.jfiveparse.Common.*;

/**
 * Implement (mostly) <a href=
 * "https://html.spec.whatwg.org/multipage/syntax.html#serialising-html-fragments"
 * >https://html.spec.whatwg.org/multipage/syntax.html#serialising-html-
 * fragments</a>
 */
public class HtmlSerializer implements NodesVisitor {

    protected final Appendable appendable;
    protected final boolean transformEntities;
    protected final boolean hideEmptyAttributeValue;
    protected final boolean scriptingDisabled;
    private final boolean printOriginalAttributeQuote;
    private final boolean printOriginalAttributeCase;
    private final boolean printOriginalTagName;

    public HtmlSerializer(Appendable appendable, Set<Option> options) {
        this.appendable = appendable;
        this.transformEntities = !options.contains(Option.DONT_TRANSFORM_ENTITIES);
        this.hideEmptyAttributeValue = options.contains(Option.HIDE_EMPTY_ATTRIBUTE_VALUE);
        this.scriptingDisabled = options.contains(Option.SCRIPTING_DISABLED);
        this.printOriginalAttributeQuote = options.contains(Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE);
        this.printOriginalAttributeCase = options.contains(Option.PRINT_ORIGINAL_ATTRIBUTES_CASE);
        this.printOriginalTagName = options.contains(Option.PRINT_ORIGINAL_TAG_CASE);
    }

    private String serializeAttributeName(AttributeNode attribute) {
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

    private String quoteCharacters(AttributeNode attribute) {
        if (printOriginalAttributeQuote) {
            if (attribute.attributeQuoteType == TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE) {
                return "";
            } else if (attribute.attributeQuoteType == TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE) {
                return "'";
            }
        }
        return "\"";
    }

    // escape handling for both text data and attribute value
    private static void escapeTextDataAttributeValue(Appendable appendable,
                                                     String data,
                                                     boolean isAttr,
                                                     boolean transformEntities) throws IOException {
        if (data == null) {
            if (isAttr) {
                appendable.append("null");
            }
            return;
        }
        int len = data.length();
        int start = 0;
        for (int i = 0; i < len; i++) {
            char c = data.charAt(i);
            String replacement = switch (c) {
                case Characters.AMPERSAND -> transformEntities ? "&amp;" : null;
                case Characters.LESSTHAN_SIGN -> !isAttr && transformEntities ? "&lt;" : null;
                case Characters.GREATERTHAN_SIGN -> !isAttr && transformEntities ? "&gt;" : null;
                case '"' -> isAttr && transformEntities ? "&quot;" : null;
                case Characters.NO_BREAK_SPACE -> "&nbsp;";
                default -> null;
            };
            if (replacement != null) {
                if (i > start) {
                    appendable.append(data, start, i);
                }
                appendable.append(replacement);
                start = i + 1;
            }
        }
        if (start < len) {
            appendable.append(data, start, len);
        }
    }

    private static boolean skipEndTag(Element e) {
        return Node.NAMESPACE_HTML_ID == e.namespaceID && isNoEndTag(e.nodeNameID);
    }

    @Override
    public void start(Node node) {
        try {
            if (node instanceof Element e) {
                // TODO: for tag outside of html,mathml,svg namespace : use qualified name!
                appendable.append('<').append(getNodeName(e));
                if (e.hasAttributes()) {
                    for (AttributeNode attr : e.getAttributes()) {
                        appendable.append(' ').append(serializeAttributeName(attr));//

                        if ((hideEmptyAttributeValue || (printOriginalAttributeQuote && attr.attributeQuoteType == TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE))
                                && (attr.getValue() == null || attr.getValue().isEmpty())) {
                            continue;
                        }
                        appendable.append('=').append(quoteCharacters(attr));
                        escapeTextDataAttributeValue(appendable, attr.getValue(), true, transformEntities);
                        appendable.append(quoteCharacters(attr));
                    }
                }
                appendable.append('>');
                if ((Common.isHtmlNS(e, Common.ELEMENT_PRE_ID) || Common.isHtmlNS(e, Common.ELEMENT_TEXTAREA_ID) || Common.isHtmlNS(e, Common.ELEMENT_LISTING_ID)) && //
                        e.hasChildNodes() && //
                        e.getFirstChild() instanceof Text textNode) {
                    String text = textNode.getData();
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
                if (literalAppend) {
                    appendable.append(t.getData());
                } else {
                    escapeTextDataAttributeValue(appendable, t.getData(), false, transformEntities);
                }
            } else if (node instanceof Comment comment) {
                appendable.append("<!--").append(comment.getData()).append("-->");
            } else if (node instanceof ProcessingInstruction processingInstruction) {
                appendable.append("<?").append(processingInstruction.getTarget());
                if (processingInstruction.getData() != null) {
                    appendable.append(' ').append(processingInstruction.getData());
                }
                appendable.append("?>");
            } else if (node instanceof DocumentType dt) {
                // TODO: should append the rest of the attributes if present
                appendable.append("<!DOCTYPE ").append(dt.getName()).append('>');
            }
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
    }

    private String getNodeName(Element e) {
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
        serialize(node, Option.EMPTY_OPTIONS, writer);
    }

    public static void serialize(Node node, Set<Option> options, Writer writer) throws IOException {
        node.traverseWithCurrentNode(new HtmlSerializer(writer, options));
        writer.flush();
    }

    public static String serialize(Node node) {
        return serialize(node, Option.EMPTY_OPTIONS);
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
