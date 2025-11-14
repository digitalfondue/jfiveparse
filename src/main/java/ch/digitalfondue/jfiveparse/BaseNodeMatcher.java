package ch.digitalfondue.jfiveparse;

@FunctionalInterface
interface BaseNodeMatcher<T extends SelectableNode<T>> {
    boolean match(T node, T base);
}
