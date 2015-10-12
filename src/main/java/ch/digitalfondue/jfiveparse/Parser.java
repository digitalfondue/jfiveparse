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

import java.io.Reader;
import java.util.List;
import java.util.Set;

/**
 * HTML5 parser.
 */
public class Parser {

    private final boolean scriptingFlag;
    private final boolean transformEntities;

    public Parser() {
        scriptingFlag = true;
        transformEntities = true;
    }

    public Parser(Set<Option> options) {
        this.scriptingFlag = !options.contains(Option.SCRIPTING_DISABLED);
        this.transformEntities = !options.contains(Option.DONT_TRANSFORM_ENTITIES);
    }

    /**
     * Parse a document.
     * 
     * @param input
     * @return
     */
    public Document parse(String input) {
        return parse(new ProcessedInputStream.StringProcessedInputStream(input));
    }

    /**
     * Parse a document.
     * 
     * Can launch a {@link ParserException} if the reader launch a IOException.
     * 
     * @param input
     * @return
     */
    public Document parse(Reader input) {
        return parse(new ProcessedInputStream.ReaderProcessedInputStream(input));
    }

    public List<Node> parseFragment(Element node, String input) {
        return parseFragment(new ProcessedInputStream.StringProcessedInputStream(input), node);
    }

    public List<Node> parseFragment(Element node, Reader input) {
        return parseFragment(new ProcessedInputStream.ReaderProcessedInputStream(input), node);
    }

    /***
     * Parse a fragment.
     * 
     * Implement the steps described at
     * https://html.spec.whatwg.org/multipage/syntax
     * .html#html-fragment-parsing-algorithm
     * 
     * @param node
     * @param input
     * @return
     */
    private List<Node> parseFragment(ProcessedInputStream is, Element node) {

        // 1 when creating a tree constructor, a document is automatically
        // created (good idea? y/n?)
        TreeConstructor tokenHandler = new TreeConstructor();
        //
        tokenHandler.setHtmlFragmentParsing(true);
        tokenHandler.setScriptingFlag(scriptingFlag);
        Tokenizer tokenizer = new Tokenizer(tokenHandler, transformEntities);
        tokenHandler.setTokenizer(tokenizer);

        String namespace = node.getNamespaceURI();
        String name = node.getNodeName();

        // 4
        if (Node.NAMESPACE_HTML.equals(namespace)) {

            if ("title".equals(name) || "textarea".equals(name)) {
                tokenizer.setState(TokenizerState.RCDATA_STATE);
            } else if ("style".equals(name) || "xmp".equals(name) || "iframe".equals(name) || "noembed".equals(name) || "noframes".equals(name)) {
                tokenizer.setState(TokenizerState.RAWTEXT_STATE);
            } else if ("script".equals(name)) {
                tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
            } else if ("noscript".equals(name) && scriptingFlag) {
                tokenizer.setState(TokenizerState.RAWTEXT_STATE);
            } else if ("plaintext".equals(name)) {
                tokenizer.setState(TokenizerState.PLAINTEXT_STATE);
            } else {
                tokenizer.setState(TokenizerState.DATA_STATE);
            }
        } else {
            tokenizer.setState(TokenizerState.DATA_STATE);
        }

        // 5
        Element root = TreeConstructor.buildElement("html", Node.NAMESPACE_HTML, TreeConstructor.emptyAttrs());

        tokenHandler.setContext(node);

        // 6
        tokenHandler.getDocument().appendChild(root);
        // 7
        tokenHandler.addToOpenElements(root);

        // 8
        if (node.is("template", Node.NAMESPACE_HTML)) {
            tokenHandler.pushIntoStackTemplatesInsertionMode(TreeConstructionInsertionMode.IN_TEMPLATE);
        }

        // 9
        tokenizer.setStartToken(node.getNodeName(), node.getAttributes().copy());

        // 10
        tokenHandler.resetInsertionModeAppropriately();

        // 11
        tokenHandler.setForm(getFirstFormElementFrom(node));
        //

        tokenizer.tokenize(is);

        // 14
        return root.getChildNodes();
    }

    private static Element getFirstFormElementFrom(Node element) {
        while (element != null) {
            if (element instanceof Element && ((Element) element).is("form", Node.NAMESPACE_HTML)) {
                return (Element) element;
            }
            element = element.parentNode;
        }
        return null;
    }

    private Document parse(ProcessedInputStream is) {
        TreeConstructor tokenHandler = new TreeConstructor();
        tokenHandler.setScriptingFlag(scriptingFlag);
        Tokenizer tokenizer = new Tokenizer(tokenHandler, transformEntities);
        tokenHandler.setTokenizer(tokenizer);
        tokenizer.tokenize(is);

        return tokenHandler.getDocument();
    }

}
