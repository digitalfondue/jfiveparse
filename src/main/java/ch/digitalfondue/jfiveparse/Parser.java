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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The parser. Instantiate for using the parser. The instance is thread-safe.
 */
public class Parser {

    private final boolean scriptingFlag;
    private final boolean transformEntities;
    private final boolean disableIgnoreTokenInBodyStartTag;

    /**
     * Instantiate a parser with the default configuration.
     * <ul>
     * <li>The scripting flag is set to true</li>
     * <li>The entities are parsed and transformed</li>
     * </ul>
     */
    public Parser() {
        scriptingFlag = true;
        transformEntities = true;
        disableIgnoreTokenInBodyStartTag = false;
    }

    /**
     * Instantiate a parser. Currently, the following {@link Option} affect the
     * behaviour of the parser:
     * <ul>
     * <li>{@link Option#SCRIPTING_DISABLED}</li>
     * <li>{@link Option#DONT_TRANSFORM_ENTITIES}</li>
     * </ul>
     * 
     * @param options
     */
    public Parser(Set<Option> options) {
        this.scriptingFlag = !options.contains(Option.SCRIPTING_DISABLED);
        this.transformEntities = !options.contains(Option.DONT_TRANSFORM_ENTITIES);
        this.disableIgnoreTokenInBodyStartTag = options.contains(Option.DISABLE_IGNORE_TOKEN_IN_BODY_START_TAG);
    }

    /**
     * Parse. This method is thread-safe.
     * 
     * @param input
     *            the {@link String} to parse
     * @return the parsed {@link Document}
     */
    public Document parse(String input) {
        return parse(new ProcessedInputStream.StringProcessedInputStream(input));
    }

    /**
     * Parse. Can launch a {@link ParserException} if the reader launch a
     * IOException.
     * 
     * @param input
     *            the {@link String} to parse
     * @return the parsed {@link Document}
     */
    public Document parse(Reader input) {
        return parse(new ProcessedInputStream.ReaderProcessedInputStream(input));
    }

    /***
     * Parse a fragment.
     * 
     * Implement the steps described at <a href="https://html.spec.whatwg.org/multipage/syntax.html#html-fragment-parsing-algorithm">https://html.spec.whatwg.org/multipage/syntax.html#html-fragment-parsing-algorithm</a>
     * 
     * @param node
     *            the context node
     * @param input
     * @return
     */
    public List<Node> parseFragment(Element node, String input) {
        return parseFragment(new ProcessedInputStream.StringProcessedInputStream(input), node);
    }

    /**
     * @see #parseFragment(Element, String)
     * 
     * @param node
     * @param input
     * @return
     */
    public List<Node> parseFragment(Element node, Reader input) {
        return parseFragment(new ProcessedInputStream.ReaderProcessedInputStream(input), node);
    }

    private List<Node> parseFragment(ProcessedInputStream is, Element node) {

        // 1 when creating a tree constructor, a document is automatically
        // created (good idea? y/n?)
        TreeConstructor tokenHandler = new TreeConstructor(disableIgnoreTokenInBodyStartTag);
        //
        tokenHandler.setHtmlFragmentParsing(true);
        tokenHandler.setScriptingFlag(scriptingFlag);
        Tokenizer tokenizer = new Tokenizer(tokenHandler, transformEntities);
        tokenHandler.setTokenizer(tokenizer);

        String namespace = node.namespaceURI;
        String name = node.nodeName;

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
        Element root = TreeConstructor.buildElement("html", "html", Node.NAMESPACE_HTML, null);

        tokenHandler.setContext(node);

        // 6
        tokenHandler.getDocument().appendChild(root);
        // 7
        tokenHandler.addToOpenElements(root);

        // 8
        if (Common.isHtmlNS(node, "template")) {
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
        return new ArrayList<>(root.getChildNodes());
    }

    private static Element getFirstFormElementFrom(Node element) {
        while (element != null) {
            if (element instanceof Element && Common.isHtmlNS((Element) element, "form")) {
                return (Element) element;
            }
            element = element.parentNode;
        }
        return null;
    }

    private Document parse(ProcessedInputStream is) {
        TreeConstructor tokenHandler = new TreeConstructor(disableIgnoreTokenInBodyStartTag);
        tokenHandler.setScriptingFlag(scriptingFlag);
        Tokenizer tokenizer = new Tokenizer(tokenHandler, transformEntities);
        tokenHandler.setTokenizer(tokenizer);
        tokenizer.tokenize(is);

        return tokenHandler.getDocument();
    }

}
