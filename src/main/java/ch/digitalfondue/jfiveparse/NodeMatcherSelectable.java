package ch.digitalfondue.jfiveparse;

@FunctionalInterface
public interface NodeMatcherSelectable<T extends SelectableNode> {
    boolean match(T node, T base);
}
