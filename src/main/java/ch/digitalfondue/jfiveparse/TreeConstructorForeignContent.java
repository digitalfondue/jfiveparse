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

import static ch.digitalfondue.jfiveparse.Common.adjustForeignAttributes;
import static ch.digitalfondue.jfiveparse.Common.adjustMathMLAttributes;
import static ch.digitalfondue.jfiveparse.Common.adjustSVGAttributes;
import static ch.digitalfondue.jfiveparse.Common.isHtmlIntegrationPoint;
import static ch.digitalfondue.jfiveparse.Common.isMathMLIntegrationPoint;
import static ch.digitalfondue.jfiveparse.TreeConstructor.CHARACTER;
import static ch.digitalfondue.jfiveparse.TreeConstructor.COMMENT;
import static ch.digitalfondue.jfiveparse.TreeConstructor.DOCTYPE;
import static ch.digitalfondue.jfiveparse.TreeConstructor.END_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.START_TAG;

import java.util.HashMap;
import java.util.Map;

class TreeConstructorForeignContent {

    static void foreignContent(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && treeConstructor.getChr() == Characters.NULL) {
            treeConstructor.emitParseError();
            treeConstructor.insertCharacter(Characters.REPLACEMENT_CHARACTER);
        } else if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == CHARACTER) {
            treeConstructor.insertCharacter();
            treeConstructor.framesetOkToFalse();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (tokenType == START_TAG && (("b".equals(tagName) || //
                "big".equals(tagName) || //
                "blockquote".equals(tagName) || //
                "body".equals(tagName) || //
                "br".equals(tagName) || //
                "center".equals(tagName) || //
                "code".equals(tagName) || //
                "dd".equals(tagName) || //
                "div".equals(tagName) || //
                "dl".equals(tagName) || //
                "dt".equals(tagName) || //
                "em".equals(tagName) || //
                "embed".equals(tagName) || //
                "h1".equals(tagName) || //
                "h2".equals(tagName) || //
                "h3".equals(tagName) || //
                "h4".equals(tagName) || //
                "h5".equals(tagName) || //
                "h6".equals(tagName) || //
                "head".equals(tagName) || //
                "hr".equals(tagName) || //
                "i".equals(tagName) || //
                "img".equals(tagName) || //
                "li".equals(tagName) || //
                "listing".equals(tagName) || //
                "menu".equals(tagName) || //
                "meta".equals(tagName) || //
                "nobr".equals(tagName) || //
                "ol".equals(tagName) || //
                "p".equals(tagName) || //
                "pre".equals(tagName) || //
                "ruby".equals(tagName) || //
                "s".equals(tagName) || //
                "small".equals(tagName) || //
                "span".equals(tagName) || //
                "strong".equals(tagName) || //
                "strike".equals(tagName) || //
                "sub".equals(tagName) || //
                "sup".equals(tagName) || //
                "table".equals(tagName) || //
                "tt".equals(tagName) || //
                "u".equals(tagName) || //
                "ul".equals(tagName) || //
                "var".equals(tagName)) || //
                ("font".equals(tagName) && (treeConstructor.hasAttribute("color") || //
                        treeConstructor.hasAttribute("face") || //
                /*    */treeConstructor.hasAttribute("size"))))) {
            treeConstructor.emitParseError();
            if (treeConstructor.isHtmlFragmentParsing()) {
                anyOtherStartTag(tagName, treeConstructor);
            } else {
                treeConstructor.popCurrentNode();
                while (true) {
                    Element cur = treeConstructor.getCurrentNode();
                    if (Node.NAMESPACE_HTML.equals(cur.getNamespaceURI()) || isMathMLIntegrationPoint(cur) || isHtmlIntegrationPoint(cur)) {
                        break;
                    }
                    treeConstructor.popCurrentNode();
                }
                treeConstructor.dispatch();
            }
        } else if (tokenType == START_TAG) {
            anyOtherStartTag(tagName, treeConstructor);
        } else if (tokenType == END_TAG && treeConstructor.getCurrentNode().is("script", Node.NAMESPACE_SVG)) {
            // we don't execute scripts
            treeConstructor.popCurrentNode();
        } else if (tokenType == END_TAG) {

            Element node = treeConstructor.getCurrentNode();
            if (!tagName.equals(convertToAsciiLowerCase(node.getNodeName()))) {
                treeConstructor.emitParseError();
            }

            int idx = treeConstructor.openElementsSize() - 1;

            while (true) {
                if (node == treeConstructor.openElementAt(0)) {
                    return;
                }

                if (tagName.equals(convertToAsciiLowerCase(node.getNodeName()))) {
                    while (node != treeConstructor.popCurrentNode()) {
                    }
                    return;
                }

                idx--;
                node = treeConstructor.openElementAt(idx);
                if (Node.NAMESPACE_HTML.equals(node.getNamespaceURI())) {
                    treeConstructor.insertionModeInHtmlContent();
                    return;
                }
            }
        }
    }

    private static void anyOtherStartTag(String tagName, TreeConstructor treeConstructor) {
        Element currentNode = treeConstructor.getAdjustedCurrentNode();
        if (Node.NAMESPACE_MATHML.equals(currentNode.getNamespaceURI())) {
            adjustMathMLAttributes(treeConstructor.getAttributes());
        }
        if (Node.NAMESPACE_SVG.equals(currentNode.getNamespaceURI())) {

            if (SVG_ELEMENT_CASE_CORRECTION.containsKey(tagName)) {
                tagName = SVG_ELEMENT_CASE_CORRECTION.get(tagName);
            }
            adjustSVGAttributes(treeConstructor.getAttributes());
        }

        adjustForeignAttributes(treeConstructor.getAttributes());

        treeConstructor.insertElementToken(tagName, currentNode.getNamespaceURI(), treeConstructor.getAttributes());

        if (treeConstructor.isSelfClosing()) {
            // we don't execute scripts
            treeConstructor.popCurrentNode();
        }
    }

    // TODO: not optimized at all
    private static String convertToAsciiLowerCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append((char) Common.toLowerCase(c));
        }

        return sb.toString();
    }

    private static final Map<String, String> SVG_ELEMENT_CASE_CORRECTION = new HashMap<>();
    static {
        SVG_ELEMENT_CASE_CORRECTION.put("altglyph", "altGlyph");
        SVG_ELEMENT_CASE_CORRECTION.put("altglyphdef", "altGlyphDef");
        SVG_ELEMENT_CASE_CORRECTION.put("altglyphitem", "altGlyphItem");
        SVG_ELEMENT_CASE_CORRECTION.put("animatecolor", "animateColor");
        SVG_ELEMENT_CASE_CORRECTION.put("animatemotion", "animateMotion");
        SVG_ELEMENT_CASE_CORRECTION.put("animatetransform", "animateTransform");
        SVG_ELEMENT_CASE_CORRECTION.put("clippath", "clipPath");
        SVG_ELEMENT_CASE_CORRECTION.put("feblend", "feBlend");
        SVG_ELEMENT_CASE_CORRECTION.put("fecolormatrix", "feColorMatrix");
        SVG_ELEMENT_CASE_CORRECTION.put("fecomponenttransfer", "feComponentTransfer");
        SVG_ELEMENT_CASE_CORRECTION.put("fecomposite", "feComposite");
        SVG_ELEMENT_CASE_CORRECTION.put("feconvolvematrix", "feConvolveMatrix");
        SVG_ELEMENT_CASE_CORRECTION.put("fediffuselighting", "feDiffuseLighting");
        SVG_ELEMENT_CASE_CORRECTION.put("fedisplacementmap", "feDisplacementMap");
        SVG_ELEMENT_CASE_CORRECTION.put("fedistantlight", "feDistantLight");
        SVG_ELEMENT_CASE_CORRECTION.put("fedropshadow", "feDropShadow");
        SVG_ELEMENT_CASE_CORRECTION.put("feflood", "feFlood");
        SVG_ELEMENT_CASE_CORRECTION.put("fefunca", "feFuncA");
        SVG_ELEMENT_CASE_CORRECTION.put("fefuncb", "feFuncB");
        SVG_ELEMENT_CASE_CORRECTION.put("fefuncg", "feFuncG");
        SVG_ELEMENT_CASE_CORRECTION.put("fefuncr", "feFuncR");
        SVG_ELEMENT_CASE_CORRECTION.put("fegaussianblur", "feGaussianBlur");
        SVG_ELEMENT_CASE_CORRECTION.put("feimage", "feImage");
        SVG_ELEMENT_CASE_CORRECTION.put("femerge", "feMerge");
        SVG_ELEMENT_CASE_CORRECTION.put("femergenode", "feMergeNode");
        SVG_ELEMENT_CASE_CORRECTION.put("femorphology", "feMorphology");
        SVG_ELEMENT_CASE_CORRECTION.put("feoffset", "feOffset");
        SVG_ELEMENT_CASE_CORRECTION.put("fepointlight", "fePointLight");
        SVG_ELEMENT_CASE_CORRECTION.put("fespecularlighting", "feSpecularLighting");
        SVG_ELEMENT_CASE_CORRECTION.put("fespotlight", "feSpotLight");
        SVG_ELEMENT_CASE_CORRECTION.put("fetile", "feTile");
        SVG_ELEMENT_CASE_CORRECTION.put("feturbulence", "feTurbulence");
        SVG_ELEMENT_CASE_CORRECTION.put("foreignobject", "foreignObject");
        SVG_ELEMENT_CASE_CORRECTION.put("glyphref", "glyphRef");
        SVG_ELEMENT_CASE_CORRECTION.put("lineargradient", "linearGradient");
        SVG_ELEMENT_CASE_CORRECTION.put("radialgradient", "radialGradient");
        SVG_ELEMENT_CASE_CORRECTION.put("textpath", "textPath");

    }
}
