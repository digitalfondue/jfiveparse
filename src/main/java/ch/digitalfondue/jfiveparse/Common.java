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


    static final ResizableCharBuilder XML = new ResizableCharBuilder(new char[] {'x', 'm', 'l'});
    static final ResizableCharBuilder XML_STYLESHEET = new ResizableCharBuilder(new char[] {'x', 'm', 'l', '-', 's', 't', 'y', 'l', 'e', 's', 'h', 'e', 'e', 't'});

    static boolean matchCharsCaseInsensitive(char[] str, int[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (str[i] != toLowerCase(chars[i])) {
                return false;
            }
        }
        return true;
    }

    static final class InternedRes {
        String lowerCaseValue;
        boolean containsUpperCase;
        int id;
    }

    private static final class Interned {
        final char firstChar;
        final char[] raw;
        final String interned;
        final int id;

        Interned(String interned) {
            this(interned, 0);
        }

        Interned(String interned, int id) {
            this.interned = interned;
            this.raw = interned.toCharArray();
            this.firstChar = raw[0];
            this.id = id;
        }
    }

    // most commonly used attributes:
    private static final Interned[][] INTERNED_ATTRS = new Interned[][]{
            buildInterned("d", "x", "y"),  // length 1
            buildInterned("as", "id", "is"), // 2
            buildInterned("alt", "dir", "for", "low", "max", "min", "rel", "src"), // 3
            buildInterned("fill", "href", "lang", "name", "open", "role", "type"), // 4
            buildInterned("async", "class", "color", "defer", "label", "media", "sizes", "style", "title", "value", "width", "xmlns"), // 5
            buildInterned("accept", "action", "coords", "height", "hidden", "srcset", "stroke", "target"), // 6
            buildInterned("content", "loading", "onclick", "version", "viewbox"), // 7
            buildInterned("datetime", "decoding", "hreflang", "itemprop", "property", "tabindex"), // 8
            buildInterned("accesskey", "fill-rule", "integrity"), // 9
            buildInterned("aria-label"), // 10
            buildInterned("aria-hidden", "crossorigin"), // 11
            buildInterned(), // 12
            buildInterned(), // 13
            buildInterned(), // 14
            buildInterned("aria-labelledby") // 15
    };

    private static Interned[] buildInterned(String... v) {
        var res = new Interned[v.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new Interned(v[i]);
        }
        return res;
    }

    private static boolean matchCaseASCIIInsensitive(Interned interned, ResizableCharBuilder builder, InternedRes res) {
        var length = interned.raw.length;
        boolean match = true, containsUpperCase = false;
        for (int i = 0; i < length; i++) {
            var c = builder.at(i);
            containsUpperCase |= isUpperCaseASCIILetter(c);
            match &= toLowerCase(c) == interned.raw[i];
        }
        res.containsUpperCase = containsUpperCase;
        builder.containsUpperCase = containsUpperCase;
        return match;
    }

    static void withAttributeNameAsLower(ResizableCharBuilder resizableCharBuilder, InternedRes res) {
        matchInterned(resizableCharBuilder, res, INTERNED_ATTRS);
    }

    static void withTagNameAsLower(ResizableCharBuilder resizableCharBuilder, InternedRes res) {
        matchInterned(resizableCharBuilder, res, INTERNED_TAG_NAME);
    }

    static int tagNameToID(String s) {
        if (s == null) {
            return 0;
        }
        var size = s.length();
        var c = s.charAt(0);
        if (size < INTERNED_TAG_NAME.length + 1) {
            for (var interned : INTERNED_TAG_NAME[size - 1]) {
                if (interned.firstChar == c && interned.interned.equals(s)) {
                    return interned.id;
                }
            }
        }
        return 0;
    }

    private static void matchInterned(ResizableCharBuilder resizableCharBuilder, InternedRes res, Interned[][] internedToMatch) {
        int size = resizableCharBuilder.pos();
        if (size < 1) {
            res.lowerCaseValue = "";
            res.containsUpperCase = false;
            res.id = 0;
            return;
        }
        var firstCharLower = toLowerCase(resizableCharBuilder.at(0));
        if (size < internedToMatch.length + 1) {
            for (var interned : internedToMatch[size - 1]) {
                if (interned.firstChar == firstCharLower && matchCaseASCIIInsensitive(interned, resizableCharBuilder, res)) {
                    res.lowerCaseValue = interned.interned;
                    res.id = interned.id;
                    return;
                }
            }
        }
        res.lowerCaseValue = resizableCharBuilder.toLowerCase();
        res.containsUpperCase = resizableCharBuilder.containsUpperCase;
        res.id = 0;
    }

    // ------------

    static void adjustMathMLAttributes(Attributes attrs) {
        if (attrs == null) {
            return;
        }
        var attr = attrs.get("definitionurl");
        if (attr != null) {
            attr.name = "definitionURL";
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
        if (attrs == null || attrs.count == 0) {
            return;
        }
        for (int i = 0; i < attrs.count; i++) {
            var attr = attrs.attributeAt(i);
            var toReplace = SVG_ATTRIBUTES.get(attr.name);
            if (toReplace != null) {
                attr.name = toReplace;
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

        if (attrs == null || attrs.count == 0) {
            return;
        }
        for (int i = 0; i < attrs.count; i++) {
            var attr = attrs.attributeAt(i);
            var adjust = FOREIGN_ATTRIBUTES_TO_ADJUST.get(attr.name);
            if (adjust != null) {
                attr.prefix = adjust[0];
                attr.name = adjust[1];
                attr.namespace = adjust[2];
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
                e.hasAttributes() &&
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
    static final int ELEMENT_SEARCH_ID = 63;
    static final int ELEMENT_SECTION_ID = 64;
    static final int ELEMENT_SELECT_ID = 65;
    static final int ELEMENT_SOURCE_ID = 66;
    static final int ELEMENT_STYLE_ID = 67;
    static final int ELEMENT_SUMMARY_ID = 68;
    static final int ELEMENT_TABLE_ID = 69;
    static final int ELEMENT_TBODY_ID = 70;
    static final int ELEMENT_TD_ID = 71;
    static final int ELEMENT_TEMPLATE_ID = 72;
    static final int ELEMENT_TEXTAREA_ID = 73;
    static final int ELEMENT_TFOOT_ID = 74;
    static final int ELEMENT_TH_ID = 75;
    static final int ELEMENT_THEAD_ID = 76;
    static final int ELEMENT_TITLE_ID = 77;
    static final int ELEMENT_TR_ID = 78;
    static final int ELEMENT_TRACK_ID = 79;
    static final int ELEMENT_UL_ID = 80;
    static final int ELEMENT_WBR_ID = 81;
    static final int ELEMENT_XMP_ID = 82;
    // end this order is the SPECIAL_ELEMENTS_HTML container from 1 to 82
    static final int ELEMENT_OPTGROUP_ID = 83;
    static final int ELEMENT_OPTION_ID = 84;
    static final int ELEMENT_RB_ID = 85;
    static final int ELEMENT_RP_ID = 86;
    static final int ELEMENT_RT_ID = 87;
    static final int ELEMENT_RTC_ID = 88;
    //
    static final int ELEMENT_A_ID = 89;
    //
    static final int ELEMENT_DIALOG_ID = 90;
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
    static final int ELEMENT_NOBR_ID = 103;
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


    private static final Interned[][] INTERNED_TAG_NAME = new Interned[][]{
            new Interned[]{
                    new Interned("a", ELEMENT_A_ID),
                    new Interned("b", ELEMENT_B_ID),
                    new Interned("i", ELEMENT_I_ID),
                    new Interned("p", ELEMENT_P_ID),
                    new Interned("s", ELEMENT_S_ID),
                    new Interned("u", ELEMENT_U_ID),
            }, // 1
            new Interned[]{
                    new Interned("br", ELEMENT_BR_ID),
                    new Interned("dd", ELEMENT_DD_ID),
                    new Interned("dl", ELEMENT_DL_ID),
                    new Interned("dt", ELEMENT_DT_ID),
                    new Interned("em", ELEMENT_EM_ID),
                    new Interned("h1", ELEMENT_H1_ID),
                    new Interned("h2", ELEMENT_H2_ID),
                    new Interned("h3", ELEMENT_H3_ID),
                    new Interned("h4", ELEMENT_H4_ID),
                    new Interned("h5", ELEMENT_H5_ID),
                    new Interned("h6", ELEMENT_H6_ID),
                    new Interned("hr", ELEMENT_HR_ID),
                    new Interned("li", ELEMENT_LI_ID),
                    new Interned("ol", ELEMENT_OL_ID),
                    new Interned("rb", ELEMENT_RB_ID),
                    new Interned("rp", ELEMENT_RP_ID),
                    new Interned("rt", ELEMENT_RT_ID),
                    new Interned("td", ELEMENT_TD_ID),
                    new Interned("th", ELEMENT_TH_ID),
                    new Interned("tr", ELEMENT_TR_ID),
                    new Interned("tt", ELEMENT_TT_ID),
                    new Interned("ul", ELEMENT_UL_ID),
            }, // 2
            new Interned[]{
                    new Interned("big", ELEMENT_BIG_ID),
                    new Interned("col", ELEMENT_COL_ID),
                    new Interned("dir", ELEMENT_DIR_ID),
                    new Interned("div", ELEMENT_DIV_ID),
                    new Interned("img", ELEMENT_IMG_ID),
                    new Interned("nav", ELEMENT_NAV_ID),
                    new Interned("pre", ELEMENT_PRE_ID),
                    new Interned("rtc", ELEMENT_RTC_ID),
                    new Interned("sub", ELEMENT_SUB_ID),
                    new Interned("sup", ELEMENT_SUP_ID),
                    new Interned("svg", ELEMENT_SVG_ID),
                    new Interned("var", ELEMENT_VAR_ID),
                    new Interned("wbr", ELEMENT_WBR_ID),
                    new Interned("xmp", ELEMENT_XMP_ID),
            }, // 3
            new Interned[]{
                    new Interned("area", ELEMENT_AREA_ID),
                    new Interned("base", ELEMENT_BASE_ID),
                    new Interned("body", ELEMENT_BODY_ID),
                    new Interned("code", ELEMENT_CODE_ID),
                    new Interned("font", ELEMENT_FONT_ID),
                    new Interned("form", ELEMENT_FORM_ID),
                    new Interned("head", ELEMENT_HEAD_ID),
                    new Interned("html", ELEMENT_HTML_ID),
                    new Interned("link", ELEMENT_LINK_ID),
                    new Interned("main", ELEMENT_MAIN_ID),
                    new Interned("math", ELEMENT_MATH_ID),
                    new Interned("menu", ELEMENT_MENU_ID),
                    new Interned("meta", ELEMENT_META_ID),
                    new Interned("nobr", ELEMENT_NOBR_ID),
                    new Interned("ruby", ELEMENT_RUBY_ID),
                    new Interned("span", ELEMENT_SPAN_ID),
            }, // 4
            new Interned[]{
                    new Interned("aside", ELEMENT_ASIDE_ID),
                    new Interned("embed", ELEMENT_EMBED_ID),
                    new Interned("frame", ELEMENT_FRAME_ID),
                    new Interned("image", ELEMENT_IMAGE_ID),
                    new Interned("input", ELEMENT_INPUT_ID),
                    new Interned("param", ELEMENT_PARAM_ID),
                    new Interned("small", ELEMENT_SMALL_ID),
                    new Interned("style", ELEMENT_STYLE_ID),
                    new Interned("table", ELEMENT_TABLE_ID),
                    new Interned("tbody", ELEMENT_TBODY_ID),
                    new Interned("tfoot", ELEMENT_TFOOT_ID),
                    new Interned("thead", ELEMENT_THEAD_ID),
                    new Interned("title", ELEMENT_TITLE_ID),
                    new Interned("track", ELEMENT_TRACK_ID),
            }, // 5
            new Interned[]{
                    new Interned("applet", ELEMENT_APPLET_ID),
                    new Interned("button", ELEMENT_BUTTON_ID),
                    new Interned("center", ELEMENT_CENTER_ID),
                    new Interned("dialog", ELEMENT_DIALOG_ID),
                    new Interned("figure", ELEMENT_FIGURE_ID),
                    new Interned("footer", ELEMENT_FOOTER_ID),
                    new Interned("header", ELEMENT_HEADER_ID),
                    new Interned("hgroup", ELEMENT_HGROUP_ID),
                    new Interned("iframe", ELEMENT_IFRAME_ID),
                    new Interned("keygen", ELEMENT_KEYGEN_ID),
                    new Interned("object", ELEMENT_OBJECT_ID),
                    new Interned("option", ELEMENT_OPTION_ID),
                    new Interned("script", ELEMENT_SCRIPT_ID),
                    new Interned("search", ELEMENT_SEARCH_ID),
                    new Interned("select", ELEMENT_SELECT_ID),
                    new Interned("source", ELEMENT_SOURCE_ID),
                    new Interned("strike", ELEMENT_STRIKE_ID),
                    new Interned("strong", ELEMENT_STRONG_ID),
            }, // 6
            new Interned[]{
                    new Interned("address", ELEMENT_ADDRESS_ID),
                    new Interned("article", ELEMENT_ARTICLE_ID),
                    new Interned("bgsound", ELEMENT_BGSOUND_ID),
                    new Interned("caption", ELEMENT_CAPTION_ID),
                    new Interned("details", ELEMENT_DETAILS_ID),
                    new Interned("listing", ELEMENT_LISTING_ID),
                    new Interned("marquee", ELEMENT_MARQUEE_ID),
                    new Interned("noembed", ELEMENT_NOEMBED_ID),
                    new Interned("section", ELEMENT_SECTION_ID),
                    new Interned("summary", ELEMENT_SUMMARY_ID),
            }, // 7
            new Interned[]{
                    new Interned("basefont", ELEMENT_BASEFONT_ID),
                    new Interned("colgroup", ELEMENT_COLGROUP_ID),
                    new Interned("fieldset", ELEMENT_FIELDSET_ID),
                    new Interned("frameset", ELEMENT_FRAMESET_ID),
                    new Interned("noframes", ELEMENT_NOFRAMES_ID),
                    new Interned("noscript", ELEMENT_NOSCRIPT_ID),
                    new Interned("optgroup", ELEMENT_OPTGROUP_ID),
                    new Interned("template", ELEMENT_TEMPLATE_ID),
                    new Interned("textarea", ELEMENT_TEXTAREA_ID),
            }, // 8
            new Interned[]{
                    new Interned("plaintext", ELEMENT_PLAINTEXT_ID),
            },// 9
            new Interned[]{
                    new Interned("blockquote", ELEMENT_BLOCKQUOTE_ID),
                    new Interned("figcaption", ELEMENT_FIGCAPTION_ID),
            }
    };

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
