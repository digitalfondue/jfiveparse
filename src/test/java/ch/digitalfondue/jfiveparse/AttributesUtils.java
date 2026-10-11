package ch.digitalfondue.jfiveparse;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

class AttributesUtils {

    public static Set<String> keySet(Attributes attributes) {
        return attributes.attributes == null ? Set.of() : attributes.attributes
                .stream()
                .map(AttributeNode::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
