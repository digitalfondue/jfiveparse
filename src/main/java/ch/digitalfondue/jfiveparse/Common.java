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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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

    private static final char[] SYSTEM = "system".toCharArray();
    private static final char[] PUBLIC = "public".toCharArray();
    private static final char[] DOCTYPE = "doctype".toCharArray();

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
            Attribute attr = attrs.get("definitionurl");
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

    // TODO cleanup, not optimal at all :D
    static void adjustSVGAttributes(Attributes attrs) {
        for (String key : SVG_ATTRIBUTES.keySet()) {
            if (attrs.containsKey(key)) {
                Attribute attr = attrs.get(key);
                attr.name = SVG_ATTRIBUTES.get(key);
                attrs.put(attr);
                attrs.remove(key);
            }
        }
    }

    private static final String[][] FOREIGN_ATTRIBUTES_TO_ADJUST = new String[][] { new String[] { "xlink:actuate", "xlink", "actuate", Node.NAMESPACE_XLINK },//
            new String[] { "xlink:arcrole", "xlink", "arcrole", Node.NAMESPACE_XLINK },//
            new String[] { "xlink:href", "xlink", "href", Node.NAMESPACE_XLINK },//
            new String[] { "xlink:role", "xlink", "role", Node.NAMESPACE_XLINK },//
            new String[] { "xlink:show", "xlink", "show", Node.NAMESPACE_XLINK },//
            new String[] { "xlink:title", "xlink", "title", Node.NAMESPACE_XLINK },//
            new String[] { "xlink:type", "xlink", "type", Node.NAMESPACE_XLINK },//
            new String[] { "xml:lang", "xml", "lang", Node.NAMESPACE_XML },//
            new String[] { "xml:space", "xml", "space", Node.NAMESPACE_XML },//
            new String[] { "xmlns", null, "xmlns", Node.NAMESPACE_XMLNS },//
            new String[] { "xmlns:xlink", "xmlns", "xlink", Node.NAMESPACE_XMLNS },//
    };

    static void adjustForeignAttributes(Attributes attrs) {
        for (String[] adj : FOREIGN_ATTRIBUTES_TO_ADJUST) {
            if (attrs.containsKey(adj[0])) {
                Attribute attr = attrs.get(adj[0]);
                attr.prefix = adj[1];
                attr.name = adj[2];
                attr.namespace = adj[3];
                attrs.put(attr);
                attrs.remove(adj[0]);
            }
        }
    }

    // ----------------

    static boolean isMathMLIntegrationPoint(Element e) {
        String nodeName = e.getNodeName();
        return Node.NAMESPACE_MATHML.equals(e.getNamespaceURI()) && ("mi".equals(nodeName) || //
                "mo".equals(nodeName) || //
                "mn".equals(nodeName) || //
                "ms".equals(nodeName) || //
                "mtext".equals(nodeName));
    }

    static boolean isHtmlIntegrationPoint(Element e) {
        String nodeName = e.getNodeName();
        String namespaceUri = e.getNamespaceURI();

        return (("annotation-xml".equals(nodeName) && Node.NAMESPACE_MATHML.equals(namespaceUri)) && //
                matchEncoding(e.getAttributes().get("encoding")))
                || //
                (Node.NAMESPACE_SVG.equals(namespaceUri) && (//
                "foreignObject".equals(nodeName) || //
                        "desc".equals(nodeName) || //
                "title".equals(nodeName)));
    }

    // TODO: this code is horrible
    private static boolean matchEncoding(Attribute target) {
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

    private static final String[] SPECIAL_ELEMENTS_MATHML = new String[] { "mi", "mo", "mn", "ms", "mtext", "annotation-xml" };

    private static final String[] SPECIAL_ELEMENTS_SVG = new String[] { "foreignObject", "desc", "title" };

    private static final HashSet<String> SPECIAL_ELEMENTS_HTML_SET_V2 = new HashSet<>();

    static {

        for (String s : new String[] { "address", "applet", "area", "article", "aside", "base", "basefont", "bgsound", "blockquote", "body", "br", "button", "caption", "center",
                "col", "colgroup", "dd", "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure", "footer", "form", "frame", "frameset", "h1", "h2", "h3",
                "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "iframe", "img", "input", "isindex", "li", "link", "listing", "main", "marquee", "menu", "menuitem",
                "meta", "nav", "noembed", "noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script", "section", "select", "source", "style", "summary",
                "table", "tbody", "td", "template", "textarea", "tfoot", "th", "thead", "title", "tr", "track", "ul", "wbr", "xmp" }) {
            SPECIAL_ELEMENTS_HTML_SET_V2.add(s);
        }
        Arrays.sort(SPECIAL_ELEMENTS_MATHML);
        Arrays.sort(SPECIAL_ELEMENTS_SVG);
    }

    static boolean isSpecialCategory(String nodeName, String nodeNameSpaceUri) {
        if (Node.NAMESPACE_HTML.equals(nodeNameSpaceUri)) {
            return SPECIAL_ELEMENTS_HTML_SET_V2.contains(nodeName);
        } else if (Node.NAMESPACE_MATHML.equals(nodeNameSpaceUri)) {
            return Arrays.binarySearch(SPECIAL_ELEMENTS_MATHML, nodeName) >= 0;
        } else if (Node.NAMESPACE_SVG.equals(nodeNameSpaceUri)) {
            return Arrays.binarySearch(SPECIAL_ELEMENTS_SVG, nodeName) >= 0;
        } else {
            return false;
        }
    }

    // ---------------------------------------------------------------------

    //

    static boolean isInCommonInScope(String tagName, String namespace) {
        if (Node.NAMESPACE_HTML.equals(namespace)) {
            switch (tagName.length()) {
            case 2:
                return "td".equals(tagName) || "th".equals(tagName);
            case 4:
                return "html".equals(tagName);
            case 5:
                return "table".equals(tagName);
            case 6:
                return "applet".equals(tagName) || "object".equals(tagName);
            case 7:
                return "caption".equals(tagName) || "marquee".equals(tagName);
            case 8:
                return "template".equals(tagName);
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
        if (!Node.NAMESPACE_HTML.equals(e.getNamespaceURI())) {
            return false;
        }
        switch (e.getNodeName()) {
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
    static final String[] NO_END_TAG = new String[] { "area", "base", "basefont", "bgsound", "br", "col", "embed", "frame", "hr", "img", "input", "keygen", "link", "menuitem",
            "meta", "param", "source", "track", "wbr" };
    static final String[] TEXT_NODE_PARENT = new String[] { "style", "script", "xmp", "iframe", "noembed", "noframes", "plaintext" };
    static {
        Arrays.sort(NO_END_TAG);
        Arrays.sort(TEXT_NODE_PARENT);
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
                "isindex",//
                "label",//
                "li",//
                "link",//
                "listing",//
                "main",//
                "marquee",//
                "menu",//
                "menuitem",//
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

    static final String[] IMPLIED_TAGS_THOROUGHLY = new String[] { "caption", "colgroup",//
            "dd", "dt", "li", "optgroup", "option", "p", "rb", "rp", "rt", "rtc", "tbody", "td",//
            "tfoot", "th", "thead", "tr", };

    static {
        Arrays.sort(IMPLIED_TAGS_THOROUGHLY);
    }

    static String join(Collection<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(' ');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
