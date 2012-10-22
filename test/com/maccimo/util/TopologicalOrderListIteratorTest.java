package com.maccimo.util;

import org.junit.Test;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

import static org.junit.Assert.*;


/**
 * @author Maxim Degtyarev <mdegtyarev@gmail.com>
 */
public class TopologicalOrderListIteratorTest {

    @Test(expected = NullPointerException.class)
    public void testNullRef() {
        TopologicalOrderListIterator<Integer> iterator = new TopologicalOrderListIterator<Integer>(null, null);
    }

    @Test(expected = CircularDependencyException.class)
    public void testCircularDependency() {
        testTopologicalIterator(
            null,
            new String[] { "Foo", "Bar", "Baz" },
            new int[] {2}, null, new int[] {0}
        );
    }

    @Test(expected = UnsatisfiedDependencyException.class)
    public void testUnsatisfiedDependency() {
        testTopologicalIterator(
            null,
            new String[] { "Foo", "Bar", "Baz" },
            new int[] {3}, null, null
        );
    }

    @Test
    public void testNoDependencies() {
        testTopologicalIterator(
                new String[] { "Foo", "Bar", "Baz" },
                new String[] { "Foo", "Bar", "Baz" },
                null, null, null
        );
    }

    @Test
    public void testOneDependency() {
        testTopologicalIterator(
                new String[] { "Bar", "Baz", "Foo" },
                new String[] { "Foo", "Bar", "Baz" },
                new int[] {2}, null, null
        );
    }

    private <T> void testTopologicalIterator(T[] expectedOrder, T[] items, int[]... dependencies) {
        List<T> list;
        TestDependencyProvider provider;

        if (expectedOrder != null) {
            assertEquals("Count of items in expectedOrder[] and items[] should be equal", expectedOrder.length, items.length);
        }
        assertEquals("Count of items and dependencies should be equal", items.length, dependencies.length);

        list = new ArrayList<T>(items.length);
        list.addAll(Arrays.asList(items));

        provider = new TestDependencyProvider();
        for(int i = 0; i < dependencies.length; i++) {
            if (dependencies[i] != null) {
                provider.addDependencies(i, dependencies[i]);
            }
        }

        TopologicalOrderListIterator<T> iterator = new TopologicalOrderListIterator<T>(list, provider);

        assertTrue("Shouldn't get there since expectedOrder is NULL", expectedOrder != null);

        int index = 0;
        int count = expectedOrder.length;

        while (iterator.hasNext()) {
            assertTrue(String.format("Iterator provide too many items. Expected %d, provided %d", index, count), index < count);
            T nextValue = iterator.next();
            assertEquals(String.format("Items differ at step %d", index), expectedOrder[index++], nextValue);
        }

        assertEquals("Iterator provide too few items.", count, index);
    }

    private static class TestDependencyProvider implements DependencyProvider {

        private final Map<Integer, int[]> itemDependencies;

        public TestDependencyProvider() {
            this.itemDependencies = new HashMap<Integer, int[]>();
        }

        public void addDependencies(int itemIndex, int[] dependencies) {
            if (dependencies == null) {
                if (itemDependencies.containsKey(itemIndex)) {
                    itemDependencies.remove(itemIndex);
                }
            } else {
                itemDependencies.put(itemIndex, dependencies);
            }
        }

        @Override
        public int[] dependencies(int item) {
            if (itemDependencies.containsKey(item)) {
                return itemDependencies.get(item);
            }
            return null;
        }
    }


    // TODO: Check list with duplications
}
