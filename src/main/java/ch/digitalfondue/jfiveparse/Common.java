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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.IntFunction;

/**
 * Dumping ground for static functions and constants.
 */
final class Common {

    static int toLowerCase(int chr) {
        if (isUpperCaseASCIILetter(chr)) {
            return chr + 0x20;
        } else {
            return chr;
        }
    }

    // convert only if necessary
    static String convertToAsciiLowerCase(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (isUpperCaseASCIILetter(c)) {
                char[] chars = s.toCharArray();
                chars[i] = (char) (c + 0x20);
                for (int j = i + 1; j < len; j++) {
                    if (isUpperCaseASCIILetter(chars[j])) {
                        chars[j] = (char) (chars[j] + 0x20);
                    }
                }
                return new String(chars);
            }
        }
        return s;
    }

    static boolean isUpperOrLowerCaseASCIILetter(int chr) {
        return (chr >= Characters.LATIN_SMALL_LETTER_A && chr <= Characters.LATIN_SMALL_LETTER_Z) || isUpperCaseASCIILetter(chr);
    }

    static boolean isUpperCaseASCIILetter(int chr) {
        return chr >= Characters.LATIN_CAPITAL_LETTER_A && chr <= Characters.LATIN_CAPITAL_LETTER_Z;
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
        return isUpperOrLowerCaseASCIILetter(chr) || isASCIIDigit(chr);
    }

    static final char[] SYSTEM = new char[] {'s', 'y', 's', 't', 'e', 'm'}; //system
    static final char[] PUBLIC = new char[] {'p', 'u', 'b', 'l', 'i', 'c'}; //public
    static final char[] DOCTYPE = new char[] {'d', 'o', 'c', 't', 'y', 'p', 'e'}; //doctype


    static final char[] XML = new char[] {'x', 'm', 'l'};
    static final char[] XML_STYLESHEET = new char[] {'x', 'm', 'l', '-', 's', 't', 'y', 'l', 'e', 's', 'h', 'e', 'e', 't'};

    static boolean matchCharsCaseInsensitive(char[] str, int[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (str[i] != toLowerCase(chars[i])) {
                return false;
            }
        }
        return true;
    }

    // ------------

    static void adjustMathMLAttributes(Attributes attrs) {
        if (attrs != null && attrs.containsKey("definitionurl")) {
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
        if (attrs == null || attrs.isEmpty()) {
            return;
        }
        ArrayList<String> toAdjust = null;
        for (AttributeNode attr : attrs) {
            if (SVG_ATTRIBUTES.containsKey(attr.name)) {
                if (toAdjust == null) {
                    toAdjust = new ArrayList<>(4);
                }
                toAdjust.add(attr.name);
            }
        }
        if (toAdjust != null) {
            for (String lowerCaseAttr : toAdjust) {
                AttributeNode attr = attrs.get(lowerCaseAttr);
                attrs.remove(lowerCaseAttr);
                attr.name = SVG_ATTRIBUTES.get(lowerCaseAttr);
                attrs.put(attr);
            }
        }
    }

    private static final HashMap<String, String[]> FOREIGN_ATTRIBUTES_TO_ADJUST = new HashMap<>();
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

        if (attrs == null || attrs.isEmpty()) {
            return;
        }

        ArrayList<String> toAdjust = null;
        for (AttributeNode attr : attrs) {
            if (FOREIGN_ATTRIBUTES_TO_ADJUST.containsKey(attr.name)) {
                if (toAdjust == null) {
                    toAdjust = new ArrayList<>(4);
                }
                toAdjust.add(attr.name);
            }
        }

        if (toAdjust != null) {
            for (String lowerCaseAttr : toAdjust) {
                String[] adj = FOREIGN_ATTRIBUTES_TO_ADJUST.get(lowerCaseAttr);
                AttributeNode attr = attrs.get(lowerCaseAttr);
                attrs.remove(lowerCaseAttr);
                attr.prefix = adj[0];
                attr.name = adj[1];
                attr.namespace = adj[2];
                attrs.put(attr);
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
        int namespaceID = e.namespaceID;

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

    // isWhiteSpace
    // tab
    // newline
    // formfeed
    // carriage return
    // space
    static boolean isTabLfFfCrOrSpace(int chr) {
        return chr == Characters.TAB || chr == Characters.LF || chr == Characters.FF || chr == Characters.CR || chr == Characters.SPACE;
    }

    static boolean isStartTagNamed(int tokenType, int namedID, int tagNameID) {
        return tokenType == TreeConstructor.TT_START_TAG && namedID == tagNameID;
    }

    static boolean isEndTagNamed(int tokenType, int namedID, int tagNameID) {
        return tokenType == TreeConstructor.TT_END_TAG && namedID == tagNameID;
    }

    // this order is the SPECIAL_ELEMENTS_HTML container from 1 to 81
    static final int ELEMENT_ADDRESS_ID = 1;
    static final int ELEMENT_APPLET_ID = 2;
    static final int ELEMENT_AREA_ID = 3;
    static final int ELEMENT_ARTICLE_ID = 4;
    static final int ELEMENT_ASIDE_ID = 5;
    static final int ELEMENT_BASE_ID = 6;
    static final int ELEMENT_BASEFONT_ID = 7;
    static final int ELEMENT_BGSOUND_ID = 8;
    static final int ELEMENT_BLOCKQUOTE_ID = 9;
    static final int ELEMENT_BODY_ID = 10;
    static final int ELEMENT_BR_ID = 11;
    static final int ELEMENT_BUTTON_ID = 12;
    static final int ELEMENT_CAPTION_ID = 13;
    static final int ELEMENT_CENTER_ID = 14;
    static final int ELEMENT_COL_ID = 15;
    static final int ELEMENT_COLGROUP_ID = 16;
    static final int ELEMENT_DD_ID = 17;
    static final int ELEMENT_DETAILS_ID = 18;
    static final int ELEMENT_DIR_ID = 19;
    static final int ELEMENT_DIV_ID = 20;
    static final int ELEMENT_DL_ID = 21;
    static final int ELEMENT_DT_ID = 22;
    static final int ELEMENT_EMBED_ID = 23;
    static final int ELEMENT_FIELDSET_ID = 24;
    static final int ELEMENT_FIGCAPTION_ID = 25;
    static final int ELEMENT_FIGURE_ID = 26;
    static final int ELEMENT_FOOTER_ID = 27;
    static final int ELEMENT_FORM_ID = 28;
    static final int ELEMENT_FRAME_ID = 29;
    static final int ELEMENT_FRAMESET_ID = 30;
    static final int ELEMENT_H1_ID = 31;
    static final int ELEMENT_H2_ID = 32;
    static final int ELEMENT_H3_ID = 33;
    static final int ELEMENT_H4_ID = 34;
    static final int ELEMENT_H5_ID = 35;
    static final int ELEMENT_H6_ID = 36;
    static final int ELEMENT_HEAD_ID = 37;
    static final int ELEMENT_HEADER_ID = 38;
    static final int ELEMENT_HGROUP_ID = 39;
    static final int ELEMENT_HR_ID = 40;
    static final int ELEMENT_HTML_ID = 41;
    static final int ELEMENT_IFRAME_ID = 42;
    static final int ELEMENT_IMG_ID = 43;
    static final int ELEMENT_INPUT_ID = 44;
    static final int ELEMENT_LI_ID = 45;
    static final int ELEMENT_LINK_ID = 46;
    static final int ELEMENT_LISTING_ID = 47;
    static final int ELEMENT_MAIN_ID = 48;
    static final int ELEMENT_MARQUEE_ID = 49;
    static final int ELEMENT_MENU_ID = 50;
    static final int ELEMENT_META_ID = 51;
    static final int ELEMENT_NAV_ID = 52;
    static final int ELEMENT_NOEMBED_ID = 53;
    static final int ELEMENT_NOFRAMES_ID = 54;
    static final int ELEMENT_NOSCRIPT_ID = 55;
    static final int ELEMENT_OBJECT_ID = 56;
    static final int ELEMENT_OL_ID = 57;
    static final int ELEMENT_P_ID = 58;
    static final int ELEMENT_PARAM_ID = 59;
    static final int ELEMENT_PLAINTEXT_ID = 60;
    static final int ELEMENT_PRE_ID = 61;
    static final int ELEMENT_SCRIPT_ID = 62;
    static final int ELEMENT_SECTION_ID = 63;
    static final int ELEMENT_SELECT_ID = 64;
    static final int ELEMENT_SOURCE_ID = 65;
    static final int ELEMENT_STYLE_ID = 66;
    static final int ELEMENT_SUMMARY_ID = 67;
    static final int ELEMENT_TABLE_ID = 68;
    static final int ELEMENT_TBODY_ID = 69;
    static final int ELEMENT_TD_ID = 70;
    static final int ELEMENT_TEMPLATE_ID = 71;
    static final int ELEMENT_TEXTAREA_ID = 72;
    static final int ELEMENT_TFOOT_ID = 73;
    static final int ELEMENT_TH_ID = 74;
    static final int ELEMENT_THEAD_ID = 75;
    static final int ELEMENT_TITLE_ID = 76;
    static final int ELEMENT_TR_ID = 77;
    static final int ELEMENT_TRACK_ID = 78;
    static final int ELEMENT_UL_ID = 79;
    static final int ELEMENT_WBR_ID = 80;
    static final int ELEMENT_XMP_ID = 81;
    // end this order is the SPECIAL_ELEMENTS_HTML container from 1 to 81
    static final int ELEMENT_OPTGROUP_ID = 82;
    static final int ELEMENT_OPTION_ID = 83;
    static final int ELEMENT_RB_ID = 84;
    static final int ELEMENT_RP_ID = 85;
    static final int ELEMENT_RT_ID = 86;
    static final int ELEMENT_RTC_ID = 87;
    //
    static final int ELEMENT_A_ID = 88;
    //
    static final int ELEMENT_DIALOG_ID = 89;
    static final int ELEMENT_SEARCH_ID = 90;
    static final int ELEMENT_B_ID = 91;
    static final int ELEMENT_BIG_ID = 92;
    static final int ELEMENT_CODE_ID = 93;
    static final int ELEMENT_EM_ID = 94;
    static final int ELEMENT_FONT_ID = 95;
    static final int ELEMENT_I_ID = 96;
    static final int ELEMENT_S_ID = 97;
    static final int ELEMENT_SMALL_ID = 98;
    static final int ELEMENT_STRIKE_ID = 99;
    static final int ELEMENT_STRONG_ID = 100;
    static final int ELEMENT_TT_ID = 101;
    static final int ELEMENT_U_ID = 102;
    static final int ELEMENT_NO_BR_ID = 103;
    static final int ELEMENT_KEYGEN_ID = 104;
    static final int ELEMENT_IMAGE_ID = 105;
    static final int ELEMENT_MATH_ID = 106;
    static final int ELEMENT_SVG_ID = 107;
    static final int ELEMENT_RUBY_ID = 108;
    //
    static final int ELEMENT_SPAN_ID = 109;
    static final int ELEMENT_SUB_ID = 110;
    static final int ELEMENT_SUP_ID = 111;
    static final int ELEMENT_VAR_ID = 112;

    // must have the same exact index
    static final String[] CANONICAL_TAG_NAMES = new String[]{
            null,
            "address", // 1
            "applet",
            "area",
            "article",
            "aside",
            "base",
            "basefont",
            "bgsound",
            "blockquote",
            "body",
            "br",
            "button",
            "caption",
            "center",
            "col",
            "colgroup",
            "dd",
            "details",
            "dir",
            "div",
            "dl",
            "dt",
            "embed",
            "fieldset",
            "figcaption",
            "figure",
            "footer",
            "form",
            "frame",
            "frameset",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "head",
            "header",
            "hgroup",
            "hr",
            "html",
            "iframe",
            "img",
            "input",
            "li",
            "link",
            "listing",
            "main",
            "marquee",
            "menu",
            "meta",
            "nav",
            "noembed",
            "noframes",
            "noscript",
            "object",
            "ol",
            "p",
            "param",
            "plaintext",
            "pre",
            "script",
            "section",
            "select",
            "source",
            "style",
            "summary",
            "table",
            "tbody",
            "td",
            "template",
            "textarea",
            "tfoot",
            "th",
            "thead",
            "title",
            "tr",
            "track",
            "ul",
            "wbr",
            "xmp",
            "optgroup",
            "option",
            "rb",
            "rp",
            "rt",
            "rtc",
            "a",
            "dialog",
            "search",
            "b",
            "big",
            "code",
            "em",
            "font",
            "i",
            "s",
            "small",
            "strike",
            "strong",
            "tt",
            "u",
            "nobr",
            "keygen",
            "image",
            "math",
            "svg",
            "ruby",
            "span",
            "sub",
            "sup",
            "var"
    };

    private static boolean equalsIgnoreCase(char[] buff, int pos, String expected) {
        if (pos != expected.length()) {
            return false;
        }
        for (int i = 0; i < pos; i++) {
            if ((char) toLowerCase(buff[i]) != expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    static int lookupTagNameID(ResizableCharBuilder builder) {
        int pos = builder.pos();
        if (pos < 1 || pos > 10) {
            return 0;
        }
        char[] buff = builder.getBuff();
        char c0 = (char) toLowerCase(buff[0]);
        switch (pos) {
            case 1:
                switch (c0) {
                    case 'a': return ELEMENT_A_ID;
                    case 'b': return ELEMENT_B_ID;
                    case 'i': return ELEMENT_I_ID;
                    case 'p': return ELEMENT_P_ID;
                    case 's': return ELEMENT_S_ID;
                    case 'u': return ELEMENT_U_ID;
                    default: return 0;
                }
            case 2:
                switch (c0) {
                    case 'b': return equalsIgnoreCase(buff, pos, "br") ? ELEMENT_BR_ID : 0;
                    case 'd': {
                        char c1 = (char) toLowerCase(buff[1]);
                        switch (c1) {
                            case 'd': return ELEMENT_DD_ID;
                            case 'l': return ELEMENT_DL_ID;
                            case 't': return ELEMENT_DT_ID;
                            default: return 0;
                        }
                    }
                    case 'e': return equalsIgnoreCase(buff, pos, "em") ? ELEMENT_EM_ID : 0;
                    case 'h': {
                        char c1 = (char) toLowerCase(buff[1]);
                        switch (c1) {
                            case '1': return ELEMENT_H1_ID;
                            case '2': return ELEMENT_H2_ID;
                            case '3': return ELEMENT_H3_ID;
                            case '4': return ELEMENT_H4_ID;
                            case '5': return ELEMENT_H5_ID;
                            case '6': return ELEMENT_H6_ID;
                            case 'r': return ELEMENT_HR_ID;
                            default: return 0;
                        }
                    }
                    case 'l': return equalsIgnoreCase(buff, pos, "li") ? ELEMENT_LI_ID : 0;
                    case 'r': {
                        char c1 = (char) toLowerCase(buff[1]);
                        switch (c1) {
                            case 'b': return ELEMENT_RB_ID;
                            case 'p': return ELEMENT_RP_ID;
                            case 't': return ELEMENT_RT_ID;
                            default: return 0;
                        }
                    }
                    case 't': {
                        char c1 = (char) toLowerCase(buff[1]);
                        switch (c1) {
                            case 'd': return ELEMENT_TD_ID;
                            case 'h': return ELEMENT_TH_ID;
                            case 'r': return ELEMENT_TR_ID;
                            case 't': return ELEMENT_TT_ID;
                            default: return 0;
                        }
                    }
                    case 'o': return equalsIgnoreCase(buff, pos, "ol") ? ELEMENT_OL_ID : 0;
                    case 'u': return equalsIgnoreCase(buff, pos, "ul") ? ELEMENT_UL_ID : 0;
                    default: return 0;
                }
            case 3:
                switch (c0) {
                    case 'b': return equalsIgnoreCase(buff, pos, "big") ? ELEMENT_BIG_ID : 0;
                    case 'c': return equalsIgnoreCase(buff, pos, "col") ? ELEMENT_COL_ID : 0;
                    case 'd': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'i') {
                            char c2 = (char) toLowerCase(buff[2]);
                            if (c2 == 'r') return ELEMENT_DIR_ID;
                            if (c2 == 'v') return ELEMENT_DIV_ID;
                        }
                        return 0;
                    }
                    case 'i': return equalsIgnoreCase(buff, pos, "img") ? ELEMENT_IMG_ID : 0;
                    case 'n': return equalsIgnoreCase(buff, pos, "nav") ? ELEMENT_NAV_ID : 0;
                    case 'p': return equalsIgnoreCase(buff, pos, "pre") ? ELEMENT_PRE_ID : 0;
                    case 'r': return equalsIgnoreCase(buff, pos, "rtc") ? ELEMENT_RTC_ID : 0;
                    case 's': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'u') {
                            char c2 = (char) toLowerCase(buff[2]);
                            if (c2 == 'b') return ELEMENT_SUB_ID;
                            if (c2 == 'p') return ELEMENT_SUP_ID;
                        } else if (c1 == 'v' && (char) toLowerCase(buff[2]) == 'g') {
                            return ELEMENT_SVG_ID;
                        }
                        return 0;
                    }
                    case 'v': return equalsIgnoreCase(buff, pos, "var") ? ELEMENT_VAR_ID : 0;
                    case 'w': return equalsIgnoreCase(buff, pos, "wbr") ? ELEMENT_WBR_ID : 0;
                    case 'x': return equalsIgnoreCase(buff, pos, "xmp") ? ELEMENT_XMP_ID : 0;
                    default: return 0;
                }
            case 4:
                switch (c0) {
                    case 'a': return equalsIgnoreCase(buff, pos, "area") ? ELEMENT_AREA_ID : 0;
                    case 'b': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'a') return equalsIgnoreCase(buff, pos, "base") ? ELEMENT_BASE_ID : 0;
                        if (c1 == 'o') return equalsIgnoreCase(buff, pos, "body") ? ELEMENT_BODY_ID : 0;
                        return 0;
                    }
                    case 'c': return equalsIgnoreCase(buff, pos, "code") ? ELEMENT_CODE_ID : 0;
                    case 'f': {
                        char c2 = (char) toLowerCase(buff[2]);
                        if (c2 == 'r') return equalsIgnoreCase(buff, pos, "form") ? ELEMENT_FORM_ID : 0;
                        if (c2 == 'n') return equalsIgnoreCase(buff, pos, "font") ? ELEMENT_FONT_ID : 0;
                        return 0;
                    }
                    case 'h': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'e') return equalsIgnoreCase(buff, pos, "head") ? ELEMENT_HEAD_ID : 0;
                        if (c1 == 't') return equalsIgnoreCase(buff, pos, "html") ? ELEMENT_HTML_ID : 0;
                        return 0;
                    }
                    case 'l': return equalsIgnoreCase(buff, pos, "link") ? ELEMENT_LINK_ID : 0;
                    case 'm': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'a') {
                            char c2 = (char) toLowerCase(buff[2]);
                            if (c2 == 'i') return equalsIgnoreCase(buff, pos, "main") ? ELEMENT_MAIN_ID : 0;
                            if (c2 == 't') return equalsIgnoreCase(buff, pos, "math") ? ELEMENT_MATH_ID : 0;
                        } else if (c1 == 'e') {
                            char c2 = (char) toLowerCase(buff[2]);
                            if (c2 == 'n') return equalsIgnoreCase(buff, pos, "menu") ? ELEMENT_MENU_ID : 0;
                            if (c2 == 't') return equalsIgnoreCase(buff, pos, "meta") ? ELEMENT_META_ID : 0;
                        }
                        return 0;
                    }
                    case 'n': return equalsIgnoreCase(buff, pos, "nobr") ? ELEMENT_NO_BR_ID : 0;
                    case 'r': return equalsIgnoreCase(buff, pos, "ruby") ? ELEMENT_RUBY_ID : 0;
                    case 's': return equalsIgnoreCase(buff, pos, "span") ? ELEMENT_SPAN_ID : 0;
                    default: return 0;
                }
            case 5:
                switch (c0) {
                    case 'a': return equalsIgnoreCase(buff, pos, "aside") ? ELEMENT_ASIDE_ID : 0;
                    case 'e': return equalsIgnoreCase(buff, pos, "embed") ? ELEMENT_EMBED_ID : 0;
                    case 'f': return equalsIgnoreCase(buff, pos, "frame") ? ELEMENT_FRAME_ID : 0;
                    case 'i': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'n') return equalsIgnoreCase(buff, pos, "input") ? ELEMENT_INPUT_ID : 0;
                        if (c1 == 'm') return equalsIgnoreCase(buff, pos, "image") ? ELEMENT_IMAGE_ID : 0;
                        return 0;
                    }
                    case 'p': return equalsIgnoreCase(buff, pos, "param") ? ELEMENT_PARAM_ID : 0;
                    case 's': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'm') return equalsIgnoreCase(buff, pos, "small") ? ELEMENT_SMALL_ID : 0;
                        if (c1 == 't') return equalsIgnoreCase(buff, pos, "style") ? ELEMENT_STYLE_ID : 0;
                        return 0;
                    }
                    case 't': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'a') return equalsIgnoreCase(buff, pos, "table") ? ELEMENT_TABLE_ID : 0;
                        if (c1 == 'b') return equalsIgnoreCase(buff, pos, "tbody") ? ELEMENT_TBODY_ID : 0;
                        if (c1 == 'f') return equalsIgnoreCase(buff, pos, "tfoot") ? ELEMENT_TFOOT_ID : 0;
                        if (c1 == 'h') return equalsIgnoreCase(buff, pos, "thead") ? ELEMENT_THEAD_ID : 0;
                        if (c1 == 'i') return equalsIgnoreCase(buff, pos, "title") ? ELEMENT_TITLE_ID : 0;
                        if (c1 == 'r') return equalsIgnoreCase(buff, pos, "track") ? ELEMENT_TRACK_ID : 0;
                        return 0;
                    }
                    default: return 0;
                }
            case 6:
                switch (c0) {
                    case 'a': return equalsIgnoreCase(buff, pos, "applet") ? ELEMENT_APPLET_ID : 0;
                    case 'b': return equalsIgnoreCase(buff, pos, "button") ? ELEMENT_BUTTON_ID : 0;
                    case 'c': return equalsIgnoreCase(buff, pos, "center") ? ELEMENT_CENTER_ID : 0;
                    case 'd': return equalsIgnoreCase(buff, pos, "dialog") ? ELEMENT_DIALOG_ID : 0;
                    case 'f': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'i') return equalsIgnoreCase(buff, pos, "figure") ? ELEMENT_FIGURE_ID : 0;
                        if (c1 == 'o') return equalsIgnoreCase(buff, pos, "footer") ? ELEMENT_FOOTER_ID : 0;
                        return 0;
                    }
                    case 'h': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'e') return equalsIgnoreCase(buff, pos, "header") ? ELEMENT_HEADER_ID : 0;
                        if (c1 == 'g') return equalsIgnoreCase(buff, pos, "hgroup") ? ELEMENT_HGROUP_ID : 0;
                        return 0;
                    }
                    case 'i': return equalsIgnoreCase(buff, pos, "iframe") ? ELEMENT_IFRAME_ID : 0;
                    case 'k': return equalsIgnoreCase(buff, pos, "keygen") ? ELEMENT_KEYGEN_ID : 0;
                    case 'o': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'b') return equalsIgnoreCase(buff, pos, "object") ? ELEMENT_OBJECT_ID : 0;
                        if (c1 == 'p') return equalsIgnoreCase(buff, pos, "option") ? ELEMENT_OPTION_ID : 0;
                        return 0;
                    }
                    case 's': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'c') return equalsIgnoreCase(buff, pos, "script") ? ELEMENT_SCRIPT_ID : 0;
                        if (c1 == 'e') {
                            char c2 = (char) toLowerCase(buff[2]);
                            if (c2 == 'a') return equalsIgnoreCase(buff, pos, "search") ? ELEMENT_SEARCH_ID : 0;
                            if (c2 == 'l') return equalsIgnoreCase(buff, pos, "select") ? ELEMENT_SELECT_ID : 0;
                        }
                        if (c1 == 'o') return equalsIgnoreCase(buff, pos, "source") ? ELEMENT_SOURCE_ID : 0;
                        if (c1 == 't') {
                            char c3 = (char) toLowerCase(buff[3]);
                            if (c3 == 'i') return equalsIgnoreCase(buff, pos, "strike") ? ELEMENT_STRIKE_ID : 0;
                            if (c3 == 'o') return equalsIgnoreCase(buff, pos, "strong") ? ELEMENT_STRONG_ID : 0;
                        }
                        return 0;
                    }
                    default: return 0;
                }
            case 7:
                switch (c0) {
                    case 'a': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'd') return equalsIgnoreCase(buff, pos, "address") ? ELEMENT_ADDRESS_ID : 0;
                        if (c1 == 'r') return equalsIgnoreCase(buff, pos, "article") ? ELEMENT_ARTICLE_ID : 0;
                        return 0;
                    }
                    case 'b': return equalsIgnoreCase(buff, pos, "bgsound") ? ELEMENT_BGSOUND_ID : 0;
                    case 'c': return equalsIgnoreCase(buff, pos, "caption") ? ELEMENT_CAPTION_ID : 0;
                    case 'd': return equalsIgnoreCase(buff, pos, "details") ? ELEMENT_DETAILS_ID : 0;
                    case 'l': return equalsIgnoreCase(buff, pos, "listing") ? ELEMENT_LISTING_ID : 0;
                    case 'm': return equalsIgnoreCase(buff, pos, "marquee") ? ELEMENT_MARQUEE_ID : 0;
                    case 'n': return equalsIgnoreCase(buff, pos, "noembed") ? ELEMENT_NOEMBED_ID : 0;
                    case 's': {
                        char c1 = (char) toLowerCase(buff[1]);
                        if (c1 == 'e') return equalsIgnoreCase(buff, pos, "section") ? ELEMENT_SECTION_ID : 0;
                        if (c1 == 'u') return equalsIgnoreCase(buff, pos, "summary") ? ELEMENT_SUMMARY_ID : 0;
                        return 0;
                    }
                    default: return 0;
                }
            case 8:
                switch (c0) {
                    case 'b': return equalsIgnoreCase(buff, pos, "basefont") ? ELEMENT_BASEFONT_ID : 0;
                    case 'c': return equalsIgnoreCase(buff, pos, "colgroup") ? ELEMENT_COLGROUP_ID : 0;
                    case 'f': {
                        int c1 = toLowerCase(buff[1]);
                        if (c1 == 'i') return equalsIgnoreCase(buff, pos, "fieldset") ? ELEMENT_FIELDSET_ID : 0;
                        if (c1 == 'r') return equalsIgnoreCase(buff, pos, "frameset") ? ELEMENT_FRAMESET_ID : 0;
                        return 0;
                    }
                    case 'n': {
                        char c2 = (char) toLowerCase(buff[2]);
                        if (c2 == 'f') return equalsIgnoreCase(buff, pos, "noframes") ? ELEMENT_NOFRAMES_ID : 0;
                        if (c2 == 's') return equalsIgnoreCase(buff, pos, "noscript") ? ELEMENT_NOSCRIPT_ID : 0;
                        return 0;
                    }
                    case 'o': return equalsIgnoreCase(buff, pos, "optgroup") ? ELEMENT_OPTGROUP_ID : 0;
                    case 't': {
                        char c2 = (char) toLowerCase(buff[2]);
                        if (c2 == 'm') return equalsIgnoreCase(buff, pos, "template") ? ELEMENT_TEMPLATE_ID : 0;
                        if (c2 == 'x') return equalsIgnoreCase(buff, pos, "textarea") ? ELEMENT_TEXTAREA_ID : 0;
                        return 0;
                    }
                    default: return 0;
                }
            case 9:
                return equalsIgnoreCase(buff, pos, "plaintext") ? ELEMENT_PLAINTEXT_ID : 0;
            case 10:
                switch (c0) {
                    case 'b': return equalsIgnoreCase(buff, pos, "blockquote") ? ELEMENT_BLOCKQUOTE_ID : 0;
                    case 'f': return equalsIgnoreCase(buff, pos, "figcaption") ? ELEMENT_FIGCAPTION_ID : 0;
                    default: return 0;
                }
            default:
                return 0;
        }
    }

    static boolean isSpecialCategory(Element element) {
    	String nodeName = element.nodeName;
        int nodeNameID = element.nodeNameID;
    	int nodeNamespaceId = element.namespaceID;
        if (Node.NAMESPACE_HTML_ID == nodeNamespaceId) {
            return nodeNameID >= ELEMENT_ADDRESS_ID && nodeNameID <= ELEMENT_XMP_ID;
        } else if (Node.NAMESPACE_MATHML_ID == nodeNamespaceId) {
            return isInCommonInScopeMathMl(nodeName);
        } else if (Node.NAMESPACE_SVG_ID == nodeNamespaceId) {
            return isInCommonInScopeSVG(nodeName);
        } else {
            return false;
        }
    }

    // ---------------------------------------------------------------------

    // see https://html.spec.whatwg.org/multipage/parsing.html#has-an-element-in-the-specific-scope

    static boolean isInCommonInScope(Element element) {
    	String tagName = element.nodeName;
        int tagNameID = element.nodeNameID;
    	int namespaceID = element.namespaceID;
        if (Node.NAMESPACE_HTML_ID == namespaceID) {
            return switch (tagNameID) {
                case ELEMENT_APPLET_ID,
                     ELEMENT_CAPTION_ID,
                     ELEMENT_HTML_ID,
                     ELEMENT_TABLE_ID,
                     ELEMENT_TD_ID,
                     ELEMENT_TH_ID,
                     ELEMENT_MARQUEE_ID,
                     ELEMENT_OBJECT_ID,
                     ELEMENT_SELECT_ID,
                     ELEMENT_TEMPLATE_ID -> true;
                default -> false;
            };
        } else if (Node.NAMESPACE_MATHML_ID == namespaceID) {
            return isInCommonInScopeMathMl(tagName);
        } else if (Node.NAMESPACE_SVG_ID == namespaceID) {
            return isInCommonInScopeSVG(tagName);
        }
        return false;
    }

    //

    // "foreignObject", "desc", "title"
    // also valid for isSpecialElementsSVG
    private static boolean isInCommonInScopeSVG(String tagName) {
        return switch (tagName) {
            case "foreignObject", "desc", "title" -> true;
            default -> false;
        };
    }

    // "mi", "mo", "mn", "ms", "mtext", "annotation-xml"
    // also valid for isSpecialElementsMathML
    private static boolean isInCommonInScopeMathMl(String tagName) {
        return switch (tagName) {
            case "mi", "mo", "mn", "ms", "mtext", "annotation-xml" -> true;
            default -> false;
        };
    }

    // ---------------

    // https://html.spec.whatwg.org/multipage/parsing.html#closing-elements-that-have-implied-end-tags
    static boolean isImpliedTag(Element e) {
        if (Node.NAMESPACE_HTML_ID != e.namespaceID) {
            return false;
        }
        return switch (e.nodeNameID) {
            case ELEMENT_DD_ID, ELEMENT_DT_ID, ELEMENT_LI_ID, ELEMENT_OPTGROUP_ID, ELEMENT_OPTION_ID, ELEMENT_P_ID,
                 ELEMENT_RB_ID, ELEMENT_RP_ID, ELEMENT_RT_ID, ELEMENT_RTC_ID -> true;
            default -> false;
        };
    }

    // ---------------

    static boolean isImpliedTagsThoroughly(int nodeNameId) {
        return switch (nodeNameId) {
            case ELEMENT_CAPTION_ID, ELEMENT_COLGROUP_ID, ELEMENT_DD_ID, ELEMENT_DT_ID, ELEMENT_LI_ID,
                 ELEMENT_OPTGROUP_ID, ELEMENT_OPTION_ID, ELEMENT_P_ID, ELEMENT_RB_ID, ELEMENT_RP_ID, ELEMENT_RT_ID,
                 ELEMENT_RTC_ID, ELEMENT_TBODY_ID, ELEMENT_TD_ID, ELEMENT_TFOOT_ID, ELEMENT_TH_ID, ELEMENT_THEAD_ID,
                 ELEMENT_TR_ID -> true;
            default -> false;
        };
    }

    static boolean isScriptSVGNS(Element element) {
        return element.nodeNameID == Common.ELEMENT_SCRIPT_ID && element.namespaceID == Node.NAMESPACE_SVG_ID;
    }

    static boolean isHtmlNS(Element element, int nameID) {
        return element.nodeNameID == nameID && element.namespaceID == Node.NAMESPACE_HTML_ID;
    }

    /** /!\ beware when using this function!, the "from"-"to" must be carefully chosen! */
    static boolean isHtmlNSBetweenH1H6(Element element) {
        return element.nodeNameID >= Common.ELEMENT_H1_ID && element.nodeNameID <= Common.ELEMENT_H6_ID && element.namespaceID == Node.NAMESPACE_HTML_ID;
    }

    static final class NodeList<T> extends AbstractList<T> {

        private final IntFunction<T> nodeFetcher;
        private final int size;

        public NodeList(int size, IntFunction<T> nodeFetcher) {
            this.size = size;
            this.nodeFetcher = nodeFetcher;
        }

        @Override
        public T get(int index) {
            return nodeFetcher.apply(index);
        }

        @Override
        public int size() {
            return size;
        }
    }
}
