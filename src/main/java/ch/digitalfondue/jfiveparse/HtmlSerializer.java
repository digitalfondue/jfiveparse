/**
 * Copyright (C) 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

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

    public HtmlSerializer(Appendable appendable, Set<Option> options) {
        this.appendable = appendable;
        this.transformEntities = !options.contains(Option.DONT_TRANSFORM_ENTITIES);
        this.hideEmptyAttributeValue = options.contains(Option.HIDE_EMPTY_ATTRIBUTE_VALUE);
        this.scriptingDisabled = options.contains(Option.SCRIPTING_DISABLED);
    }

    protected static String serializeAttributeName(Attribute attribute) {
        String name = attribute.getName();
        String namespace = attribute.getNamespace();

        if (Node.NAMESPACE_XML.equals(namespace)) {
            return "xml:" + name;
        } else if (Node.NAMESPACE_XMLNS.equals(namespace)) {
            return "xmlns".equals(name) ? "xmlns" : ("xmlns:" + name);
        } else if (Node.NAMESPACE_XLINK.equals(namespace)) {
            return "xlink:" + name;
        } else if (namespace != null) {
            return namespace + ":" + name;// TODO check
        } else {
            return name;
        }
    }

    protected String escapeAttributeValue(Attribute attribute) {
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
        return Node.NAMESPACE_HTML.equals(e.getNamespaceURI()) && Arrays.binarySearch(Common.NO_END_TAG, e.getNodeName()) >= 0;
    }

    @Override
    public void start(Node node) {
        try {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                // TODO: for tag outside of html,mathml,svg namespace : use
                // qualified name!
                appendable.append('<').append(e.getNodeName());
                for (Attribute attr : e.getAttributes()) {
                    appendable.append(' ').append(serializeAttributeName(attr));//

                    if (hideEmptyAttributeValue && (attr.getValue() == null || "".equals(attr.getValue()))) {
                        continue;
                    }
                    appendable.append("=\"").append(escapeAttributeValue(attr)).append("\"");
                }
                appendable.append('>');

                if ((e.is("pre", Node.NAMESPACE_HTML) || e.is("textarea", Node.NAMESPACE_HTML) || e.is("listing", Node.NAMESPACE_HTML)) && //
                        e.hasChildNodes() && //
                        e.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                    String text = ((Text) e.getFirstChild()).getData();
                    if (text.length() > 0 && text.charAt(0) == Characters.LF) {
                        appendable.append(Characters.LF);
                    }
                }

            } else if (node.getNodeType() == Node.TEXT_NODE) {
                // TODO: handle the case when the nodes are created with
                // scripting disabled

                Node parent = node.getParentNode();
                boolean literalAppend = false;
                if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                    Element p = (Element) parent;
                    literalAppend = Node.NAMESPACE_HTML.equals(p.getNamespaceURI())
                            && (Arrays.binarySearch(Common.TEXT_NODE_PARENT, p.getNodeName()) >= 0 || ("noscript".equals(p.getNodeName()) && !scriptingDisabled));
                }
                Text t = (Text) node;
                appendable.append(literalAppend ? t.getData() : escapeTextData(t.getData()));

            } else if (node.getNodeType() == Node.COMMENT_NODE) {
                appendable.append("<!--").append(((Comment) node).getData()).append("-->");
            } else if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
                // TODO: should append the rest of the attributes if present
                appendable.append("<!DOCTYPE ").append(((DocumentType) node).getName()).append(">");
            }
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
    }

    @Override
    public void end(Node node) {
        try {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                if (!skipEndTag(e)) {
                    appendable.append("</").append(e.getNodeName()).append(">");
                }
            }
        } catch (IOException ioe) {
            throw new SerializationException(ioe);
        }
    }

    public static class SerializationException extends RuntimeException {

        private static final long serialVersionUID = -2182908125163112627L;

        public SerializationException(Throwable t) {
            super(t);
        }

    }

}
