package ch.digitalfondue.jfiveparse;

interface BaseNodesVisitor<T> {
    void start(T node);

    default void end(T node) {}

    default boolean complete() {
        return false;
    }
}
