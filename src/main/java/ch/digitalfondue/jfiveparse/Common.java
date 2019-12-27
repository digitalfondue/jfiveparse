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

import java.util.*;

/**
 * Dumping ground for static functions and constants.
 */
class Common {

    static int toLowerCase(int chr) {
        if (isUpperCaseASCIILetter(chr)) {
            return chr + 0x0020;
        } else {
            return chr;
        }
    }

    static String convertToAsciiLowerCase(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append((char) toLowerCase(c));
        }
        return sb.toString();
    }

    static boolean isUpperCaseASCIILetter(int chr) {
        return chr >= Characters.LATIN_CAPITAL_LETTER_A && chr <= Characters.LATIN_CAPITAL_LETTER_Z;
    }

    static boolean isLowerCaseASCIILetter(int chr) {
        return chr >= Characters.LATIN_SMALL_LETTER_A && chr <= Characters.LATIN_SMALL_LETTER_Z;
    }

    static boolean isASCIIHexDigit(int chr) {
        return (chr >= 0x0030 && chr <= 0x0039) || //
                (chr >= 0x0041 && chr <= 0x0046) || //
                (chr >= 0x0061 && chr <= 0x0066);//
    }

    static boolean isASCIIDigit(int chr) {
        return chr >= 0x0030 && chr <= 0x0039;
    }

    static boolean isAlphaNumericASCII(int chr) {
        return isLowerCaseASCIILetter(chr) || isUpperCaseASCIILetter(chr) || isASCIIDigit(chr);
    }

    private static final char[] SYSTEM = new char[] {'s', 'y', 's', 't', 'e', 'm'}; //system
    private static final char[] PUBLIC = new char[] {'p', 'u', 'b', 'l', 'i', 'c'}; //public
    private static final char[] DOCTYPE = new char[] {'d', 'o', 'c', 't', 'y', 'p', 'e'}; //doctype

    static boolean matchCharCaseInsensitiveSystem(int[] chars) {
        return matchCharsCaseInsensitive(SYSTEM, chars);
    }

    static boolean matchCharsCaseInsensitivePublic(int[] chars) {
        return matchCharsCaseInsensitive(PUBLIC, chars);
    }

    static boolean matchCharsCaseInsensitiveDoctype(int[] chars) {
        return matchCharsCaseInsensitive(DOCTYPE, chars);
    }

    private static boolean matchCharsCaseInsensitive(char[] str, int[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (str[i] != toLowerCase(chars[i])) {
                return false;
            }
        }
        return true;
    }

    // ------------

    static void adjustMathMLAttributes(Attributes attrs) {
        if (attrs.containsKey("definitionurl")) {
            AttributeNode attr = attrs.get("definitionurl");
            attr.name = "definitionURL";
            attrs.put(attr);
            attrs.remove("definitionurl");
        }
    }

    private static final HashMap<String, String> SVG_ATTRIBUTES = new HashMap<>();
    static {
        SVG_ATTRIBUTES.put("attributename", "attributeName");
        SVG_ATTRIBUTES.put("attributetype", "attributeType");
        SVG_ATTRIBUTES.put("basefrequency", "baseFrequency");
        SVG_ATTRIBUTES.put("baseprofile", "baseProfile");
        SVG_ATTRIBUTES.put("calcmode", "calcMode");
        SVG_ATTRIBUTES.put("clippathunits", "clipPathUnits");
        SVG_ATTRIBUTES.put("diffuseconstant", "diffuseConstant");
        SVG_ATTRIBUTES.put("edgemode", "edgeMode");
        SVG_ATTRIBUTES.put("filterunits", "filterUnits");
        SVG_ATTRIBUTES.put("glyphref", "glyphRef");
        SVG_ATTRIBUTES.put("gradienttransform", "gradientTransform");
        SVG_ATTRIBUTES.put("gradientunits", "gradientUnits");
        SVG_ATTRIBUTES.put("kernelmatrix", "kernelMatrix");
        SVG_ATTRIBUTES.put("kernelunitlength", "kernelUnitLength");
        SVG_ATTRIBUTES.put("keypoints", "keyPoints");
        SVG_ATTRIBUTES.put("keysplines", "keySplines");
        SVG_ATTRIBUTES.put("keytimes", "keyTimes");
        SVG_ATTRIBUTES.put("lengthadjust", "lengthAdjust");
        SVG_ATTRIBUTES.put("limitingconeangle", "limitingConeAngle");
        SVG_ATTRIBUTES.put("markerheight", "markerHeight");
        SVG_ATTRIBUTES.put("markerunits", "markerUnits");
        SVG_ATTRIBUTES.put("markerwidth", "markerWidth");
        SVG_ATTRIBUTES.put("maskcontentunits", "maskContentUnits");
        SVG_ATTRIBUTES.put("maskunits", "maskUnits");
        SVG_ATTRIBUTES.put("numoctaves", "numOctaves");
        SVG_ATTRIBUTES.put("pathlength", "pathLength");
        SVG_ATTRIBUTES.put("patterncontentunits", "patternContentUnits");
        SVG_ATTRIBUTES.put("patterntransform", "patternTransform");
        SVG_ATTRIBUTES.put("patternunits", "patternUnits");
        SVG_ATTRIBUTES.put("pointsatx", "pointsAtX");
        SVG_ATTRIBUTES.put("pointsaty", "pointsAtY");
        SVG_ATTRIBUTES.put("pointsatz", "pointsAtZ");
        SVG_ATTRIBUTES.put("preservealpha", "preserveAlpha");
        SVG_ATTRIBUTES.put("preserveaspectratio", "preserveAspectRatio");
        SVG_ATTRIBUTES.put("primitiveunits", "primitiveUnits");
        SVG_ATTRIBUTES.put("refx", "refX");
        SVG_ATTRIBUTES.put("refy", "refY");
        SVG_ATTRIBUTES.put("repeatcount", "repeatCount");
        SVG_ATTRIBUTES.put("repeatdur", "repeatDur");
        SVG_ATTRIBUTES.put("requiredextensions", "requiredExtensions");
        SVG_ATTRIBUTES.put("requiredfeatures", "requiredFeatures");
        SVG_ATTRIBUTES.put("specularconstant", "specularConstant");
        SVG_ATTRIBUTES.put("specularexponent", "specularExponent");
        SVG_ATTRIBUTES.put("spreadmethod", "spreadMethod");
        SVG_ATTRIBUTES.put("startoffset", "startOffset");
        SVG_ATTRIBUTES.put("stddeviation", "stdDeviation");
        SVG_ATTRIBUTES.put("stitchtiles", "stitchTiles");
        SVG_ATTRIBUTES.put("surfacescale", "surfaceScale");
        SVG_ATTRIBUTES.put("systemlanguage", "systemLanguage");
        SVG_ATTRIBUTES.put("tablevalues", "tableValues");
        SVG_ATTRIBUTES.put("targetx", "targetX");
        SVG_ATTRIBUTES.put("targety", "targetY");
        SVG_ATTRIBUTES.put("textlength", "textLength");
        SVG_ATTRIBUTES.put("viewbox", "viewBox");
        SVG_ATTRIBUTES.put("viewtarget", "viewTarget");
        SVG_ATTRIBUTES.put("xchannelselector", "xChannelSelector");
        SVG_ATTRIBUTES.put("ychannelselector", "yChannelSelector");
        SVG_ATTRIBUTES.put("zoomandpan", "zoomAndPan");
    }

    static void adjustSVGAttributes(Attributes attrs) {
        if (attrs.isEmpty()) {
            return;
        }

        for (String lowerCaseAttr : new ArrayList<>(attrs.keySet())) {
            if (SVG_ATTRIBUTES.containsKey(lowerCaseAttr)) {
                AttributeNode attr = attrs.get(lowerCaseAttr);
                attr.name = SVG_ATTRIBUTES.get(lowerCaseAttr);
                attrs.put(attr);
                attrs.remove(lowerCaseAttr);
            }
        }
    }

    private static final Map<String, String[]> FOREIGN_ATTRIBUTES_TO_ADJUST = new HashMap<>();
    static {
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xlink:actuate",   new String[] { "xlink", "actuate", Node.NAMESPACE_XLINK });
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xlink:arcrole",   new String[] {"xlink", "arcrole", Node.NAMESPACE_XLINK });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xlink:href",      new String[] { "xlink", "href", Node.NAMESPACE_XLINK });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xlink:role",      new String[] { "xlink", "role", Node.NAMESPACE_XLINK });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xlink:show",      new String[] { "xlink", "show", Node.NAMESPACE_XLINK });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xlink:title",     new String[] { "xlink", "title", Node.NAMESPACE_XLINK });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xlink:type",      new String[] { "xlink", "type", Node.NAMESPACE_XLINK });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xml:lang",        new String[] { "xml", "lang", Node.NAMESPACE_XML });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xml:space",       new String[] { "xml", "space", Node.NAMESPACE_XML });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xmlns",           new String[] { null, "xmlns", Node.NAMESPACE_XMLNS });//
        FOREIGN_ATTRIBUTES_TO_ADJUST.put("xmlns:xlink",     new String[] { "xmlns", "xlink", Node.NAMESPACE_XMLNS });
    }

    static void adjustForeignAttributes(Attributes attrs) {

        if (attrs.isEmpty()) {
            return;
        }

        for (String lowerCaseAttr: new ArrayList<>(attrs.keySet())) {
            if (FOREIGN_ATTRIBUTES_TO_ADJUST.containsKey(lowerCaseAttr)) {
                String[] adj = FOREIGN_ATTRIBUTES_TO_ADJUST.get(lowerCaseAttr);
                AttributeNode attr = attrs.get(lowerCaseAttr);
                attr.prefix = adj[0];
                attr.name = adj[1];
                attr.namespace = adj[2];
                attrs.put(attr);
                attrs.remove(lowerCaseAttr);
            }
        }
    }


    // ----------------

    static boolean isMathMLIntegrationPoint(Element e) {
        String nodeName = e.nodeName;
        return Node.NAMESPACE_MATHML.equals(e.getNamespaceURI()) && ("mi".equals(nodeName) || //
                "mo".equals(nodeName) || //
                "mn".equals(nodeName) || //
                "ms".equals(nodeName) || //
                "mtext".equals(nodeName));
    }

    static boolean isHtmlIntegrationPoint(Element e) {
        String nodeName = e.nodeName;
        String namespaceUri = e.namespaceURI;

        return (("annotation-xml".equals(nodeName) && Node.NAMESPACE_MATHML.equals(namespaceUri)) && //
                matchEncoding(e.getAttributes().get("encoding")))
                || //
                (Node.NAMESPACE_SVG.equals(namespaceUri) && (//
                "foreignObject".equals(nodeName) || //
                        "desc".equals(nodeName) || //
                "title".equals(nodeName)));
    }

    // TODO: this code is horrible
    private static boolean matchEncoding(AttributeNode target) {
        if (target == null) {
            return false;
        }

        String value = target.getValue();

        final String html = "text/html";
        final String xhtml = "application/xhtml+xml";

        final int targetLen = value.length();
        if (targetLen == html.length()) {
            for (int i = 0; i < targetLen; i++) {
                if (Common.toLowerCase(value.charAt(i)) != html.charAt(i)) {
                    return false;
                }
            }
            return true;
        } else if (targetLen == xhtml.length()) {
            for (int i = 0; i < targetLen; i++) {
                if (Common.toLowerCase(value.charAt(i)) != xhtml.charAt(i)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    static boolean isTabLfFfCrOrSpace(int chr) {
        return chr == Characters.TAB || chr == Characters.LF || chr == Characters.FF || chr == Characters.CR || chr == Characters.SPACE;
    }

    static boolean isStartTagNamed(byte tokenType, String named, String tagName) {
        return tokenType == TreeConstructor.START_TAG && named.equals(tagName);
    }

    static boolean isEndTagNamed(byte tokenType, String named, String tagName) {
        return tokenType == TreeConstructor.END_TAG && named.equals(tagName);
    }

    private static final HashSet<String> SPECIAL_ELEMENTS_HTML_SET_V2 = new HashSet<>();

    static {
        Collections.addAll(SPECIAL_ELEMENTS_HTML_SET_V2, "address", "applet", "area", "article", "aside", "base", "basefont", "bgsound", "blockquote", "body", "br", "button", "caption", "center",
                "col", "colgroup", "dd", "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure", "footer", "form", "frame", "frameset", "h1", "h2", "h3",
                "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "iframe", "img", "input", "li", "link", "listing", "main", "marquee", "menu",
                "meta", "nav", "noembed", "noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script", "section", "select", "source", "style", "summary",
                "table", "tbody", "td", "template", "textarea", "tfoot", "th", "thead", "title", "tr", "track", "ul", "wbr", "xmp");
    }

    static boolean isSpecialCategory(Element element) {
    	String nodeName = element.nodeName;
    	String nodeNameSpaceUri = element.namespaceURI;
        if (Node.NAMESPACE_HTML.equals(nodeNameSpaceUri)) {
            return SPECIAL_ELEMENTS_HTML_SET_V2.contains(nodeName);
        } else if (Node.NAMESPACE_MATHML.equals(nodeNameSpaceUri)) {
            return isSpecialElementsMathML(nodeName);
        } else if (Node.NAMESPACE_SVG.equals(nodeNameSpaceUri)) {
            return isSpecialElementsSVG(nodeName);
        } else {
            return false;
        }
    }

    // ---------------------------------------------------------------------

    //

    static boolean isInCommonInScope(Element element) {
    	String tagName = element.nodeName;
    	String namespace = element.namespaceURI; 
        if (Node.NAMESPACE_HTML.equals(namespace)) {
            switch (tagName) {
                case "td":
                case "th":
                case "html":
                case "table":
                case "applet":
                case "object":
                case "caption":
                case "marquee":
                case "template":
                    return true;
                default:
                    return false;
            }
        } else if (Node.NAMESPACE_MATHML.equals(namespace)) {
            return isInCommonInScopeMathMl(tagName);
        } else if (Node.NAMESPACE_SVG.equals(namespace)) {
            return isInCommonInScopeSVG(tagName);
        }
        return false;
    }

    // "foreignObject", "desc", "title"
    private static boolean isSpecialElementsSVG(String tagName) {
        return isInCommonInScopeSVG(tagName);
    }

    private static boolean isInCommonInScopeSVG(String tagName) {
        switch (tagName) {
        case "foreignObject":
        case "desc":
        case "title":
            return true;
        default:
            return false;
        }
    }

    // "mi", "mo", "mn", "ms", "mtext", "annotation-xml"
    private static boolean isSpecialElementsMathML(String tagName) {
        return isInCommonInScopeMathMl(tagName);
    }

    private static boolean isInCommonInScopeMathMl(String tagName) {
        switch (tagName) {
        case "mi":
        case "mo":
        case "mn":
        case "ms":
        case "mtext":
        case "annotation-xml":
            return true;
        default:
            return false;
        }
    }

    // ---------------

    static boolean isImpliedTag(Element e) {
        if (!Node.NAMESPACE_HTML.equals(e.namespaceURI)) {
            return false;
        }
        switch (e.nodeName) {
        case "dd":
        case "dt":
        case "li":
        case "optgroup":
        case "option":
        case "p":
        case "rb":
        case "rp":
        case "rt":
        case "rtc":
            return true;
        default:
            return false;
        }
    }

    // SERIALIZATION

    static boolean isNoEndTag(String nodeName) {
        switch (nodeName) {
            case "area":
            case "base":
            case "basefont":
            case "bgsound":
            case "br":
            case "col":
            case "embed":
            case "frame":
            case "hr":
            case "img":
            case "input":
            case "keygen":
            case "link":
            case "meta":
            case "param":
            case "source":
            case "track":
            case "wbr":
                return true;
            default:
                return false;
        }
    }

    static boolean isTextNodeParent(String nodeName) {
        switch (nodeName) {
            case "style":
            case "script":
            case "xmp":
            case "iframe":
            case "noembed":
            case "noframes":
            case "plaintext":
                return true;
            default:
                return false;
        }
    }
    //

    // ---------------

    static final HashMap<String, String> ELEMENTS_NAME_CACHE_V2 = new HashMap<>();

    static {

        for (String s : new String[] { "big", //
                "rb",//
                "math", //
                "rp", //
                "rt", //
                "rtc", //
                "svg", //
                "strike",//
                "s",//
                "u", //
                "font", //
                "small",//
                "tt", //
                "optgroup",//
                "image",//
                "option", //
                "keygen", //
                "dialog",//
                "a",//
                "address", //
                "applet", //
                "area",//
                "article",//
                "aside",//
                "b",//
                "base",//
                "basefont",//
                "bgsound",//
                "blockquote",//
                "body",//
                "br",//
                "button",//
                "cite",//
                "caption",//
                "center",//
                "code",//
                "col",//
                "colgroup",//
                "dd",//
                "del",//
                "details",//
                "dir",//
                "div",//
                "dl",//
                "dt",//
                "em",//
                "embed",//
                "fieldset",//
                "figcaption",//
                "figure",//
                "footer",//
                "form",//
                "frame",//
                "frameset",//
                "h1",//
                "h2",//
                "h3",//
                "h4",//
                "h5",//
                "h6",//
                "head",//
                "header",//
                "hgroup",//
                "hr",//
                "html",//
                "i",//
                "iframe",//
                "img",//
                "input",//
                "label",//
                "li",//
                "link",//
                "listing",//
                "main",//
                "marquee",//
                "menu",//
                "meta",//
                "nav",//
                "nobr",//
                "noembed",//
                "noframes",//
                "noscript",//
                "noindex",//
                "object",//
                "ol",//
                "p",//
                "param",//
                "plaintext",//
                "pre",//
                "script",//
                "section",//
                "select",//
                "source",//
                "span",//
                "style",//
                "summary",//
                "sup",//
                "strong",//
                "table",//
                "tbody",//
                "td",//
                "template",//
                "textarea",//
                "tfoot",//
                "th",//
                "thead",//
                "time",//
                "title",//
                "tr",//
                "track",//
                "ul",//
                "wbr",//
                "xmp" }) {
            ELEMENTS_NAME_CACHE_V2.put(s, s);
        }
    }

    static boolean isImpliedTagsThoroughly(String nodeName) {
        switch (nodeName) {
            case "caption":
            case "colgroup":
            case "dd":
            case "dt":
            case "li":
            case "optgroup":
            case "option":
            case "p":
            case "rb":
            case "rp":
            case "rt":
            case "rtc":
            case "tbody":
            case "td":
            case "tfoot":
            case "th":
            case "thead":
            case "tr":
                return true;
            default:
                return false;
        }
    }

    static String join(Iterator<String> l) {
        StringBuilder sb = new StringBuilder();
        while (l.hasNext()) {
            sb.append(l.next());
            if (l.hasNext()) {
                sb.append(' ');
            }

        }
        return sb.toString();
    }
    
    static boolean is(Element element, String name, String namespace) {
    	return element.nodeName.equals(name) && element.namespaceURI.equals(namespace);
    }
    
    static boolean isHtmlNS(Element element, String name) {
    	return element.nodeName.equals(name) && element.namespaceURI.equals(Node.NAMESPACE_HTML);
    }
}
