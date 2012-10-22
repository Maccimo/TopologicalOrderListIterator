package com.maccimo.util;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author Maxim Degtyarev <mdegtyarev@gmail.com>
 * @version 1.0
 */
public class TopologicalOrderListIterator<E> implements ListIterator<E> {

    private final List<E> list;
    private final int listSize;

    private int nextIndex;
    private final int[] indexMapping;

    public TopologicalOrderListIterator(List<E> list, DependencyProvider provider) {
        this.list = list;
        this.listSize = list.size();
        this.nextIndex = 0;
        this.indexMapping = buildIndexMapping(provider);
    }

    /**
     *
     * @throws NoSuchElementException
     * @return Next list element in topological order.
     */
    @Override
    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        return list.get(indexMapping[nextIndex++]);
    }

    /**
     * @throws NoSuchElementException
     * @return Previous list element in topological order
     */
    @Override
    public E previous() {
        if (!hasPrevious())
            throw new NoSuchElementException();

        return list.get(indexMapping[--nextIndex]);
    }

    @Override
    public boolean hasNext() {
        return (this.nextIndex < this.listSize);
    }

    @Override
    public boolean hasPrevious() {
        return (this.nextIndex > 0) && (this.nextIndex < this.listSize);
    }

    @Override
    public int nextIndex() {
        if (nextIndex < listSize)
            return indexMapping[nextIndex];
        return listSize;
    }

    @Override
    public int previousIndex() {
        if (nextIndex > 0)
            return indexMapping[nextIndex - 1];
        return -1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(E e) {
        throw new UnsupportedOperationException();
    }

    private int[] buildIndexMapping(DependencyProvider provider) {
        int[] result = new int[listSize];
        boolean[] unprocessed = new boolean[listSize];
        boolean[][] adjacency = new boolean[listSize][listSize];

        for(int i = 0; i < listSize; i++) {
            result[i] = -1;
            unprocessed[i] = true;
            for(int j = 0; j < listSize; j++) {
                adjacency[i][j] = false;
            }
        }

        for(int i = 0; i < listSize; i++) {
            int[] dependencies = provider.dependencies(i);
            if (dependencies != null) {
                for(int j = 0; j < dependencies.length; j++) {
                    int k = dependencies[j];

                    if ((k < 0)||(k >= listSize)) {
                        throw new UnsatisfiedDependencyException();
                    } else {
                        adjacency[i][k] = true;
                    }
                }
            }
        }

        boolean done;
        int cursor = 0;
        do {
            done = true;
            for(int i = 0; i < listSize; i++) {
                if (unprocessed[i]) {
                    boolean hasDependencies = false;
                    for(boolean item : adjacency[i]) {
                        hasDependencies = hasDependencies || item;
                    }

                    if (!hasDependencies) {
                        done = false;
                        for(int k = 0; k < listSize; k++) {
                            adjacency[k][i] = false;
                        }

                        result[cursor++] = i;
                        unprocessed[i] = false;
                    }
                }
            }
        } while (!done);

        if (cursor != listSize) {
            throw new CircularDependencyException();
        }

        return result;        
    }

}
