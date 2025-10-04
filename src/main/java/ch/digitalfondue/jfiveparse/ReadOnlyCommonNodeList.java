package ch.digitalfondue.jfiveparse;

import java.util.AbstractList;
import java.util.function.IntFunction;

final class ReadOnlyCommonNodeList extends AbstractList<SelectableNode> {

    private final IntFunction<SelectableNode> get;
    private final int size;

    ReadOnlyCommonNodeList(IntFunction<SelectableNode> get, int size) {
        this.get = get;
        this.size = size;
    }

    @Override
    public SelectableNode get(int index) {
        return get.apply(index);
    }

    @Override
    public int size() {
        return size;
    }
}
