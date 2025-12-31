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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeConstructionTest {

    boolean scripting;
    TreeConstruction treeTest;

    public void initTreeConstructionTest(String path, TreeConstruction test, boolean scripting) {
        this.treeTest = test;
        this.scripting = scripting;
    }

    @MethodSource("data")
    @ParameterizedTest(name = "{0}:{index}:{2}")
    public void check(String path, TreeConstruction test, boolean scripting) {
        initTreeConstructionTest(path, test, scripting);
        Set<Option> options = EnumSet.noneOf(Option.class);
        if(!scripting) {
            options.add(Option.SCRIPTING_DISABLED);
        }
        Parser parser = new Parser(options);
        if (treeTest.isDocumentFragment) {
            List<Node> nodes = parser.parseFragment(new Element(treeTest.documentFragmentElement, treeTest.documentFragmentNamespace, null), treeTest.data);
            String rendered = renderNodes(nodes);
            assertEquals(treeTest.document, rendered);
        } else {
            Document document = parser.parse(treeTest.data);
            String rendered = renderDocument(document);
            assertEquals(treeTest.document, rendered);
        }
    }

    public static List<Object[]> data() throws IOException {
        List<Object[]> data = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("src/test/resources/html5lib-tests/tree-construction"), "*.dat")) {
            for (Path p : ds) {
                String file = Files.readString(p);
                String[] testsAsString = file.split("\n\n#data\n");
                for (String t : testsAsString) {
                    TreeConstruction treeTest = parse(t);
                    if (treeTest.scriptingFlag == null) {
                        data.add(new Object[] { p.getFileName().toString(), treeTest, false });
                        data.add(new Object[] { p.getFileName().toString(), treeTest, true });
                    } else if (treeTest.scriptingFlag) {
                        data.add(new Object[] { p.getFileName().toString(), treeTest, true });
                    } else {
                        data.add(new Object[] { p.getFileName().toString(), treeTest, false });
                    }
                }
            }
        }
        data.sort((o1, o2) -> Comparator.<Object[], String>comparing(o -> (String) o[0]).thenComparing(o -> (boolean) o[2]).compare(o1, o2));
        return data;


    }

    static TreeConstruction parse(String test) {

        String data;
        int dataStartIndex = 0;
        if (test.startsWith("#data\n")) {
            dataStartIndex = "#data\n".length();
        }

        int dataEnd = test.indexOf("\n#errors\n");
        
        int scriptOffEnd = test.indexOf("\n#script-off\n");
        if(scriptOffEnd > -1) {
        	dataEnd = Math.min(dataEnd, scriptOffEnd);
        }

        int documentFragmentEnd = test.indexOf("\n#document-fragment\n");
        if (documentFragmentEnd > -1) {
            dataEnd = Math.min(dataEnd, documentFragmentEnd);
        }
        
        if (dataEnd == -1) {
            dataEnd = test.indexOf("#errors\n");
        }
        data = test.substring(dataStartIndex, dataEnd);

        int documentStart = test.indexOf("\n#document\n", dataEnd);

        // seems to work :D
        String document = test.substring(documentStart + "\n#document\n".length()).trim();

        TreeConstruction t = new TreeConstruction();
        t.data = data;
        t.document = document;

        if (test.contains("\n#document-fragment\n")) {
            t.isDocumentFragment = true;

            String startFragment = test.substring(test.indexOf("\n#document-fragment\n") + "\n#document-fragment\n".length());
            String fragment = startFragment.substring(0, startFragment.indexOf('\n')).trim();
            if (fragment.startsWith("svg ")) {
                t.documentFragmentElement = fragment.substring("svg ".length());
                t.documentFragmentNamespace = Node.NAMESPACE_SVG;
            } else if (fragment.startsWith("math ")) {
                t.documentFragmentElement = fragment.substring("math ".length());
                t.documentFragmentNamespace = Node.NAMESPACE_MATHML;
            } else {
                t.documentFragmentElement = fragment;
            }
        }

        if (test.contains("\n#script-on\n")) {
            t.scriptingFlag = true;
        }
        if (test.contains("\n#script-off\n")) {
            t.scriptingFlag = false;
        }

        return t;

    }

    public static class TreeConstruction {
        String data;
        List<String> errors;
        String documentFragmentElement;
        String documentFragmentNamespace = Node.NAMESPACE_HTML;
        boolean isDocumentFragment;
        Boolean scriptingFlag;
        String document;
    }

    static String renderDocument(Document document) {
        StringBuilder sb = new StringBuilder();

        for (Node node : document.getChildNodes()) {
            renderNode(node, 0, sb);
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    static String renderNodes(List<Node> nodes) {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            renderNode(node, 0, sb);
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static void renderNode(Node node, int depth, StringBuilder sb) {

        int spaces = depth == 0 ? 1 : depth * 2 + 1;
        sb.append("|").append(" ".repeat(spaces));

        int depthsForTemplatesChilds = 0;

        if (node instanceof Element elem) {

            // TODO: at the moment, it's not really clear if we want to handle at tree construction time the
            //       selectedcontent clone behaviour. CHECK, where we should put this logic...
            // this works only if: selected content is a child of a button, which must be the first child of an option
            var hasSelectedContent = elem.getAllNodesMatching(Selector.parseSelector("button:first-child > selectedcontent:only-child"), true);
            if ("SELECT".equals(elem.getTagName()) && !hasSelectedContent.isEmpty()) {
                var toClone = elem.getAllNodesMatching(Selector.parseSelector("option[selected]"), true);
                if (toClone.isEmpty()) {
                    toClone = elem.getAllNodesMatching(Selector.parseSelector("option:first-of-type"), true);
                }
                if (!toClone.isEmpty()) {
                    var selectedContent = hasSelectedContent.get(0);
                    var childNodes = new ArrayList<>(toClone.get(0).cloneNode(true).getChildNodes());
                    for (var c : childNodes) {
                        selectedContent.appendChild(c);
                    }
                }
            }

            sb.append("<");
            if (Node.NAMESPACE_MATHML.equals(elem.getNamespaceURI())) {
                sb.append("math ");
            } else if (Node.NAMESPACE_SVG.equals(elem.getNamespaceURI())) {
                sb.append("svg ");
            }

            sb.append(elem.getNodeName()).append(">");

            Set<String> attributesName = new TreeSet<>(elem.getAttributes().keySet());
            if (!attributesName.isEmpty()) {
                sb.append("\n");
                for (String k : attributesName) {
                    sb.append("|").append(" ".repeat(spaces + 2));

                    AttributeNode attribute = elem.getAttributes().get(k);
                    if (attribute.getNamespace() != null) {
                        if (Node.NAMESPACE_XLINK.equals(attribute.getNamespace())) {
                            sb.append("xlink ");
                        } else if (Node.NAMESPACE_XML.equals(attribute.getNamespace())) {
                            sb.append("xml ");
                        } else if (Node.NAMESPACE_XMLNS.equals(attribute.getNamespace())) {
                            sb.append("xmlns ");
                        }
                    }

                    sb.append(k).append("=\"").append(attribute.getValue()).append("\"\n");
                }
                sb.deleteCharAt(sb.length() - 1);
            }

            if (Common.isHtmlNS(elem, Common.ELEMENT_TEMPLATE_ID)) {
                sb.append("\n|").append(" ".repeat(spaces + 2)).append("content");
                depthsForTemplatesChilds += 1;
            }

        } else if (node instanceof Text) {
            sb.append("\"").append(((Text) node).getData()).append("\"");
        } else if (node instanceof Comment) {
            sb.append("<!-- ").append(((Comment) node).getData()).append(" -->");
        } else if (node instanceof DocumentType dt) {

            sb.append("<!DOCTYPE ");
            sb.append(dt.getName());
            if (isNotBlank(dt.getPublicId()) || isNotBlank(dt.getSystemId())) {
                sb.append(" ");
                sb.append("\"").append(dt.getPublicId()).append("\"");
                sb.append(" ");
                sb.append("\"").append(dt.getSystemId()).append("\"");
            }
            sb.append(">");
        }

        sb.append("\n");

        for (Node childNode : node.getChildNodes()) {
            renderNode(childNode, depth + 1 + depthsForTemplatesChilds, sb);
        }
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

}
