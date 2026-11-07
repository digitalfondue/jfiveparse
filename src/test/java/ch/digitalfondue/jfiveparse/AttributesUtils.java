package ch.digitalfondue.jfiveparse;

import java.util.LinkedHashSet;
import java.util.Set;

class AttributesUtils {

    public static Set<String> keySet(Attributes attributes) {
        var res = new LinkedHashSet<String>();
        attributes.forEach(an -> {
            res.add(an.getName());
        });
        return res;
    }

    public static int size(Attributes attributes) {
        var size = 0;
        for (var ignored : attributes) {
            size++;
        }
        return size;
    }
}
