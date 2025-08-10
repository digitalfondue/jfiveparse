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

import java.util.ArrayList;
import java.util.HashMap;

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

    static boolean isUpperOrLowerCaseASCIILetter(int chr) {
        return isLowerCaseASCIILetter(chr) || isUpperCaseASCIILetter(chr);
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
        return isUpperOrLowerCaseASCIILetter(chr) || isASCIIDigit(chr);
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

        for (String lowerCaseAttr : new ArrayList<>(attrs.keySet())) {
            if (SVG_ATTRIBUTES.containsKey(lowerCaseAttr)) {
                AttributeNode attr = attrs.get(lowerCaseAttr);
                attr.name = SVG_ATTRIBUTES.get(lowerCaseAttr);
                attrs.put(attr);
                attrs.remove(lowerCaseAttr);
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

    static boolean isTabLfFfCrOrSpace(int chr) {
        return chr == Characters.TAB || chr == Characters.LF || chr == Characters.FF || chr == Characters.CR || chr == Characters.SPACE;
    }

    static boolean isStartTagNamed(int tokenType, int namedID, int tagNameID) {
        return tokenType == TreeConstructor.TT_START_TAG && namedID == tagNameID;
    }

    static boolean isEndTagNamed(int tokenType, int namedID, int tagNameID) {
        return tokenType == TreeConstructor.TT_END_TAG && namedID == tagNameID;
    }

    static int tagNameToID(String tagName) {
        if (tagName == null) {
            return 0;
        }
        return switch (tagName) {
            case "address" -> ELEMENT_ADDRESS_ID;
            case "applet" -> ELEMENT_APPLET_ID;
            case "area" -> ELEMENT_AREA_ID;
            case "article" -> ELEMENT_ARTICLE_ID;
            case "aside" -> ELEMENT_ASIDE_ID;
            case "base" -> ELEMENT_BASE_ID;
            case "basefont" -> ELEMENT_BASEFONT_ID;
            case "bgsound" -> ELEMENT_BGSOUND_ID;
            case "blockquote" -> ELEMENT_BLOCKQUOTE_ID;
            case "body" -> ELEMENT_BODY_ID;
            case "br" -> ELEMENT_BR_ID;
            case "button" -> ELEMENT_BUTTON_ID;
            case "caption" -> ELEMENT_CAPTION_ID;
            case "center" -> ELEMENT_CENTER_ID;
            case "col" -> ELEMENT_COL_ID;
            case "colgroup" -> ELEMENT_COLGROUP_ID;
            case "dd" -> ELEMENT_DD_ID;
            case "details" -> ELEMENT_DETAILS_ID;
            case "dir" -> ELEMENT_DIR_ID;
            case "div" -> ELEMENT_DIV_ID;
            case "dl" -> ELEMENT_DL_ID;
            case "dt" -> ELEMENT_DT_ID;
            case "embed" -> ELEMENT_EMBED_ID;
            case "fieldset" -> ELEMENT_FIELDSET_ID;
            case "figcaption" -> ELEMENT_FIGCAPTION_ID;
            case "figure" -> ELEMENT_FIGURE_ID;
            case "footer" -> ELEMENT_FOOTER_ID;
            case "form" -> ELEMENT_FORM_ID;
            case "frame" -> ELEMENT_FRAME_ID;
            case "frameset" -> ELEMENT_FRAMESET_ID;
            case "h1" -> ELEMENT_H1_ID;
            case "h2" -> ELEMENT_H2_ID;
            case "h3" -> ELEMENT_H3_ID;
            case "h4" -> ELEMENT_H4_ID;
            case "h5" -> ELEMENT_H5_ID;
            case "h6" -> ELEMENT_H6_ID;
            case "head" -> ELEMENT_HEAD_ID;
            case "header" -> ELEMENT_HEADER_ID;
            case "hgroup" -> ELEMENT_HGROUP_ID;
            case "hr" -> ELEMENT_HR_ID;
            case "html" -> ELEMENT_HTML_ID;
            case "iframe" -> ELEMENT_IFRAME_ID;
            case "img" -> ELEMENT_IMG_ID;
            case "input" -> ELEMENT_INPUT_ID;
            case "li" -> ELEMENT_LI_ID;
            case "link" -> ELEMENT_LINK_ID;
            case "listing" -> ELEMENT_LISTING_ID;
            case "main" -> ELEMENT_MAIN_ID;
            case "marquee" -> ELEMENT_MARQUEE_ID;
            case "menu" -> ELEMENT_MENU_ID;
            case "meta" -> ELEMENT_META_ID;
            case "nav" -> ELEMENT_NAV_ID;
            case "noembed" -> ELEMENT_NOEMBED_ID;
            case "noframes" -> ELEMENT_NOFRAMES_ID;
            case "noscript" -> ELEMENT_NOSCRIPT_ID;
            case "object" -> ELEMENT_OBJECT_ID;
            case "ol" -> ELEMENT_OL_ID;
            case "p" -> ELEMENT_P_ID;
            case "param" -> ELEMENT_PARAM_ID;
            case "plaintext" -> ELEMENT_PLAINTEXT_ID;
            case "pre" -> ELEMENT_PRE_ID;
            case "script" -> ELEMENT_SCRIPT_ID;
            case "section" -> ELEMENT_SECTION_ID;
            case "select" -> ELEMENT_SELECT_ID;
            case "source" -> ELEMENT_SOURCE_ID;
            case "style" -> ELEMENT_STYLE_ID;
            case "summary" -> ELEMENT_SUMMARY_ID;
            case "table" -> ELEMENT_TABLE_ID;
            case "tbody" -> ELEMENT_TBODY_ID;
            case "td" -> ELEMENT_TD_ID;
            case "template" -> ELEMENT_TEMPLATE_ID;
            case "textarea" -> ELEMENT_TEXTAREA_ID;
            case "tfoot" -> ELEMENT_TFOOT_ID;
            case "th" -> ELEMENT_TH_ID;
            case "thead" -> ELEMENT_THEAD_ID;
            case "title" -> ELEMENT_TITLE_ID;
            case "tr" -> ELEMENT_TR_ID;
            case "track" -> ELEMENT_TRACK_ID;
            case "ul" -> ELEMENT_UL_ID;
            case "wbr" -> ELEMENT_WBR_ID;
            case "xmp" -> ELEMENT_XMP_ID;
            //
            case "optgroup" -> ELEMENT_OPTGROUP_ID;
            case "option" -> ELEMENT_OPTION_ID;
            case "rb" -> ELEMENT_RB_ID;
            case "rp" -> ELEMENT_RP_ID;
            case "rt" -> ELEMENT_RT_ID;
            case "rtc" -> ELEMENT_RTC_ID;
            //
            case "a" -> ELEMENT_A_ID;
            //

            case "dialog" -> ELEMENT_DIALOG_ID;
            case "search" -> ELEMENT_SEARCH_ID;
            case "b" -> ELEMENT_B_ID;
            case "big" -> ELEMENT_BIG_ID;
            case "code" -> ELEMENT_CODE_ID;
            case "em" -> ELEMENT_EM_ID;
            case "font" -> ELEMENT_FONT_ID;
            case "i" -> ELEMENT_I_ID;
            case "s" -> ELEMENT_S_ID;
            case "small" -> ELEMENT_SMALL_ID;
            case "strike" -> ELEMENT_STRIKE_ID;
            case "strong" -> ELEMENT_STRONG_ID;
            case "tt" -> ELEMENT_TT_ID;
            case "u" -> ELEMENT_U_ID;
            case "nobr" -> ELEMENT_NO_BR_ID;
            case "keygen" -> ELEMENT_KEYGEN_ID;
            case "image" -> ELEMENT_IMAGE_ID;
            case "math" -> ELEMENT_MATH_ID;
            case "svg" -> ELEMENT_SVG_ID;
            case "ruby" -> ELEMENT_RUBY_ID;
            case "span" -> ELEMENT_SPAN_ID;
            case "sub" -> ELEMENT_SUB_ID;
            case "sup" -> ELEMENT_SUP_ID;
            case "var" -> ELEMENT_VAR_ID;
            default -> 0;
        };
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

    //

    static boolean isInCommonInScope(Element element) {
    	String tagName = element.nodeName;
        int tagNameID = element.nodeNameID;
    	int namespaceID = element.namespaceID;
        if (Node.NAMESPACE_HTML_ID == namespaceID) {
            return switch (tagNameID) {
                case ELEMENT_APPLET_ID, ELEMENT_CAPTION_ID, ELEMENT_HTML_ID, ELEMENT_MARQUEE_ID, ELEMENT_OBJECT_ID,
                     ELEMENT_TABLE_ID, ELEMENT_TEMPLATE_ID, ELEMENT_TD_ID, ELEMENT_TH_ID -> true;
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

    static boolean is(Element element, int nameID, int namespaceID) {
        return element.nodeNameID == nameID && element.namespaceID == namespaceID;
    }

    static boolean isHtmlNS(Element element, int nameID) {
        return element.nodeNameID == nameID && element.namespaceID == Node.NAMESPACE_HTML_ID;
    }

    /** /!\ beware when using this function!, the "from"-"to" must be carefully chosen! */
    static boolean isHtmlNSBetweenH1H6(Element element) {
        return element.nodeNameID >= Common.ELEMENT_H1_ID && element.nodeNameID <= Common.ELEMENT_H6_ID && element.namespaceID == Node.NAMESPACE_HTML_ID;
    }
}
