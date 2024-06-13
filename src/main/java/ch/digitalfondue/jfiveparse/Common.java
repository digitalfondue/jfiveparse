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
        return Node.NAMESPACE_MATHML_ID == e.namespaceID && ("mi".equals(nodeName) || //
                "mo".equals(nodeName) || //
                "mn".equals(nodeName) || //
                "ms".equals(nodeName) || //
                "mtext".equals(nodeName));
    }

    static boolean isHtmlIntegrationPoint(Element e) {
        String nodeName = e.nodeName;
        byte namespaceID = e.namespaceID;

        return ((Node.NAMESPACE_MATHML_ID == namespaceID && "annotation-xml".equals(nodeName)) && //
                matchEncoding(e.getAttributes().get("encoding")))
                || //
                (Node.NAMESPACE_SVG_ID == namespaceID && (//
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

    // private static final HashSet<String> SPECIAL_ELEMENTS_HTML_SET_V2 = new LinkedHashSet<>();

    static byte tagNameToID(String tagName) {
        if (tagName == null) {
            return 0;
        }
        switch (tagName) {
            case "address": return ELEMENT_ADDRESS_ID;
            case "applet": return ELEMENT_APPLET_ID;
            case "area": return ELEMENT_AREA_ID;
            case "article": return ELEMENT_ARTICLE_ID;
            case "aside": return ELEMENT_ASIDE_ID;
            case "base": return ELEMENT_BASE_ID;
            case "basefont": return ELEMENT_BASEFONT_ID;
            case "bgsound": return ELEMENT_BGSOUND_ID;
            case "blockquote": return ELEMENT_BLOCKQUOTE_ID;
            case "body": return ELEMENT_BODY_ID;
            case "br": return ELEMENT_BR_ID;
            case "button": return ELEMENT_BUTTON_ID;
            case "caption": return ELEMENT_CAPTION_ID;
            case "center": return ELEMENT_CENTER_ID;
            case "col": return ELEMENT_COL_ID;
            case "colgroup": return ELEMENT_COLGROUP_ID;
            case "dd": return ELEMENT_DD_ID;
            case "details": return ELEMENT_DETAILS_ID;
            case "dir": return ELEMENT_DIR_ID;
            case "div": return ELEMENT_DIV_ID;
            case "dl": return ELEMENT_DL_ID;
            case "dt": return ELEMENT_DT_ID;
            case "embed": return ELEMENT_EMBED_ID;
            case "fieldset": return ELEMENT_FIELDSET_ID;
            case "figcaption": return ELEMENT_FIGCAPTION_ID;
            case "figure": return ELEMENT_FIGURE_ID;
            case "footer": return ELEMENT_FOOTER_ID;
            case "form": return ELEMENT_FORM_ID;
            case "frame": return ELEMENT_FRAME_ID;
            case "frameset": return ELEMENT_FRAMESET_ID;
            case "h1": return ELEMENT_H1_ID;
            case "h2": return ELEMENT_H2_ID;
            case "h3": return ELEMENT_H3_ID;
            case "h4": return ELEMENT_H4_ID;
            case "h5": return ELEMENT_H5_ID;
            case "h6": return ELEMENT_H6_ID;
            case "head": return ELEMENT_HEAD_ID;
            case "header": return ELEMENT_HEADER_ID;
            case "hgroup": return ELEMENT_HGROUP_ID;
            case "hr": return ELEMENT_HR_ID;
            case "html": return ELEMENT_HTML_ID;
            case "iframe": return ELEMENT_IFRAME_ID;
            case "img": return ELEMENT_IMG_ID;
            case "input": return ELEMENT_INPUT_ID;
            case "li": return ELEMENT_LI_ID;
            case "link": return ELEMENT_LINK_ID;
            case "listing": return ELEMENT_LISTING_ID;
            case "main": return ELEMENT_MAIN_ID;
            case "marquee": return ELEMENT_MARQUEE_ID;
            case "menu": return ELEMENT_MENU_ID;
            case "meta": return ELEMENT_META_ID;
            case "nav": return ELEMENT_NAV_ID;
            case "noembed": return ELEMENT_NOEMBED_ID;
            case "noframes": return ELEMENT_NOFRAMES_ID;
            case "noscript": return ELEMENT_NOSCRIPT_ID;
            case "object": return ELEMENT_OBJECT_ID;
            case "ol": return ELEMENT_OL_ID;
            case "p": return ELEMENT_P_ID;
            case "param": return ELEMENT_PARAM_ID;
            case "plaintext": return ELEMENT_PLAINTEXT_ID;
            case "pre": return ELEMENT_PRE_ID;
            case "script": return ELEMENT_SCRIPT_ID;
            case "section": return ELEMENT_SECTION_ID;
            case "select": return ELEMENT_SELECT_ID;
            case "source": return ELEMENT_SOURCE_ID;
            case "style": return ELEMENT_STYLE_ID;
            case "summary": return ELEMENT_SUMMARY_ID;
            case "table": return ELEMENT_TABLE_ID;
            case "tbody": return ELEMENT_TBODY_ID;
            case "td": return ELEMENT_TD_ID;
            case "template": return ELEMENT_TEMPLATE_ID;
            case "textarea": return ELEMENT_TEXTAREA_ID;
            case "tfoot": return ELEMENT_TFOOT_ID;
            case "th": return ELEMENT_TH_ID;
            case "thead": return ELEMENT_THEAD_ID;
            case "title": return ELEMENT_TITLE_ID;
            case "tr": return ELEMENT_TR_ID;
            case "track": return ELEMENT_TRACK_ID;
            case "ul": return ELEMENT_UL_ID;
            case "wbr": return ELEMENT_WBR_ID;
            case "xmp": return ELEMENT_XMP_ID;
            default:return 0;
        }
    }
/*
    static {
        Collections.addAll(SPECIAL_ELEMENTS_HTML_SET_V2, "address", "applet", "area", "article", "aside", "base", "basefont", "bgsound", "blockquote", "body", "br", "button", "caption", "center",
                "col", "colgroup", "dd", "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure", "footer", "form", "frame", "frameset", "h1", "h2", "h3",
                "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "iframe", "img", "input", "li", "link", "listing", "main", "marquee", "menu",
                "meta", "nav", "noembed", "noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script", "section", "select", "source", "style", "summary",
                "table", "tbody", "td", "template", "textarea", "tfoot", "th", "thead", "title", "tr", "track", "ul", "wbr", "xmp");
    }
*/
    // this order is the SPECIAL_ELEMENTS_HTML container from 1 to 81
    static final byte ELEMENT_ADDRESS_ID = 1;
    static final byte ELEMENT_APPLET_ID = 2;
    static final byte ELEMENT_AREA_ID = 3;
    static final byte ELEMENT_ARTICLE_ID = 4;
    static final byte ELEMENT_ASIDE_ID = 5;
    static final byte ELEMENT_BASE_ID = 6;
    static final byte ELEMENT_BASEFONT_ID = 7;
    static final byte ELEMENT_BGSOUND_ID = 8;
    static final byte ELEMENT_BLOCKQUOTE_ID = 9;
    static final byte ELEMENT_BODY_ID = 10;
    static final byte ELEMENT_BR_ID = 11;
    static final byte ELEMENT_BUTTON_ID = 12;
    static final byte ELEMENT_CAPTION_ID = 13;
    static final byte ELEMENT_CENTER_ID = 14;
    static final byte ELEMENT_COL_ID = 15;
    static final byte ELEMENT_COLGROUP_ID = 16;
    static final byte ELEMENT_DD_ID = 17;
    static final byte ELEMENT_DETAILS_ID = 18;
    static final byte ELEMENT_DIR_ID = 19;
    static final byte ELEMENT_DIV_ID = 20;
    static final byte ELEMENT_DL_ID = 21;
    static final byte ELEMENT_DT_ID = 22;
    static final byte ELEMENT_EMBED_ID = 23;
    static final byte ELEMENT_FIELDSET_ID = 24;
    static final byte ELEMENT_FIGCAPTION_ID = 25;
    static final byte ELEMENT_FIGURE_ID = 26;
    static final byte ELEMENT_FOOTER_ID = 27;
    static final byte ELEMENT_FORM_ID = 28;
    static final byte ELEMENT_FRAME_ID = 29;
    static final byte ELEMENT_FRAMESET_ID = 30;
    static final byte ELEMENT_H1_ID = 31;
    static final byte ELEMENT_H2_ID = 32;
    static final byte ELEMENT_H3_ID = 33;
    static final byte ELEMENT_H4_ID = 34;
    static final byte ELEMENT_H5_ID = 35;
    static final byte ELEMENT_H6_ID = 36;
    static final byte ELEMENT_HEAD_ID = 37;
    static final byte ELEMENT_HEADER_ID = 38;
    static final byte ELEMENT_HGROUP_ID = 39;
    static final byte ELEMENT_HR_ID = 40;
    static final byte ELEMENT_HTML_ID = 41;
    static final byte ELEMENT_IFRAME_ID = 42;
    static final byte ELEMENT_IMG_ID = 43;
    static final byte ELEMENT_INPUT_ID = 44;
    static final byte ELEMENT_LI_ID = 45;
    static final byte ELEMENT_LINK_ID = 46;
    static final byte ELEMENT_LISTING_ID = 47;
    static final byte ELEMENT_MAIN_ID = 48;
    static final byte ELEMENT_MARQUEE_ID = 49;
    static final byte ELEMENT_MENU_ID = 50;
    static final byte ELEMENT_META_ID = 51;
    static final byte ELEMENT_NAV_ID = 52;
    static final byte ELEMENT_NOEMBED_ID = 53;
    static final byte ELEMENT_NOFRAMES_ID = 54;
    static final byte ELEMENT_NOSCRIPT_ID = 55;
    static final byte ELEMENT_OBJECT_ID = 56;
    static final byte ELEMENT_OL_ID = 57;
    static final byte ELEMENT_P_ID = 58;
    static final byte ELEMENT_PARAM_ID = 59;
    static final byte ELEMENT_PLAINTEXT_ID = 60;
    static final byte ELEMENT_PRE_ID = 61;
    static final byte ELEMENT_SCRIPT_ID = 62;
    static final byte ELEMENT_SECTION_ID = 63;
    static final byte ELEMENT_SELECT_ID = 64;
    static final byte ELEMENT_SOURCE_ID = 65;
    static final byte ELEMENT_STYLE_ID = 66;
    static final byte ELEMENT_SUMMARY_ID = 67;
    static final byte ELEMENT_TABLE_ID = 68;
    static final byte ELEMENT_TBODY_ID = 69;
    static final byte ELEMENT_TD_ID = 70;
    static final byte ELEMENT_TEMPLATE_ID = 71;
    static final byte ELEMENT_TEXTAREA_ID = 72;
    static final byte ELEMENT_TFOOT_ID = 73;
    static final byte ELEMENT_TH_ID = 74;
    static final byte ELEMENT_THEAD_ID = 75;
    static final byte ELEMENT_TITLE_ID = 76;
    static final byte ELEMENT_TR_ID = 77;
    static final byte ELEMENT_TRACK_ID = 78;
    static final byte ELEMENT_UL_ID = 79;
    static final byte ELEMENT_WBR_ID = 80;
    static final byte ELEMENT_XMP_ID = 81;
    // end this order is the SPECIAL_ELEMENTS_HTML container from 1 to 81


    /*
    public static void main(String[] args) {

        for (var s : SPECIAL_ELEMENTS_HTML_SET_V2) {
            System.err.println("case \"" + s + "\": return ELEMENT_" + s.toUpperCase(Locale.ROOT)+"_ID;");
        }
        int i = 0;
        for (var s : SPECIAL_ELEMENTS_HTML_SET_V2) {
            i++;
            System.err.println("static final byte ELEMENT_" + s.toUpperCase(Locale.ROOT)+"_ID = "+i+";");
        }
    }*/

    static boolean isSpecialCategory(Element element) {
    	String nodeName = element.nodeName;
        byte nodeNameID = element.nodeNameID;
    	byte nodeNameSpaceId = element.namespaceID;
        if (Node.NAMESPACE_HTML_ID == nodeNameSpaceId) {
            return nodeNameID >= ELEMENT_ADDRESS_ID && nodeNameID <= ELEMENT_XMP_ID;
        } else if (Node.NAMESPACE_MATHML_ID == nodeNameSpaceId) {
            return isSpecialElementsMathML(nodeName);
        } else if (Node.NAMESPACE_SVG_ID == nodeNameSpaceId) {
            return isSpecialElementsSVG(nodeName);
        } else {
            return false;
        }
    }

    // ---------------------------------------------------------------------

    //

    static boolean isInCommonInScope(Element element) {
    	String tagName = element.nodeName;
        byte tagNameID = element.nodeNameID;
    	byte namespaceID = element.namespaceID;
        if (Node.NAMESPACE_HTML_ID == namespaceID) {
            switch (tagNameID) {
                case ELEMENT_TD_ID:
                case ELEMENT_TH_ID:
                case ELEMENT_HTML_ID:
                case ELEMENT_TABLE_ID:
                case ELEMENT_APPLET_ID:
                case ELEMENT_OBJECT_ID:
                case ELEMENT_CAPTION_ID:
                case ELEMENT_MARQUEE_ID:
                case ELEMENT_TEMPLATE_ID:
                    return true;
                default:
                    return false;
            }
        } else if (Node.NAMESPACE_MATHML_ID == namespaceID) {
            return isInCommonInScopeMathMl(tagName);
        } else if (Node.NAMESPACE_SVG_ID == namespaceID) {
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
        if (Node.NAMESPACE_HTML_ID != e.namespaceID) {
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
                "abbr",
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
                "search",//
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

    static boolean is(Element element, String name, byte namespaceID) {
        return element.namespaceID == namespaceID && element.nodeName.equals(name);
    }
    
    static boolean isHtmlNS(Element element, String name) {
    	return element.namespaceID == Node.NAMESPACE_HTML_ID && element.nodeName.equals(name);
    }

    static boolean isHtmlNS(Element element, byte nameID) {
        return element.nodeNameID == nameID && element.namespaceID == Node.NAMESPACE_HTML_ID;
    }

    /** /!\ beware when using this function!, the from to must be carefully chosed! */
    static boolean isHtmlNSBetween(Element element, byte nameIDFrom, byte nameIDto) {
        return element.nodeNameID >= nameIDFrom && element.nodeNameID <= nameIDto && element.namespaceID == Node.NAMESPACE_HTML_ID;
    }
}
