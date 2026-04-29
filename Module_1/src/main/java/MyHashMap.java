public class MyHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;

    private static final float LOAD_FACTOR = 0.75f;

    private Node<K, V>[] buckets;

    private int size;

    private int threshold;

    public MyHashMap() {
        this(DEFAULT_CAPACITY);
    }


    public MyHashMap(int initialCapacity) {
        this.buckets = (Node<K, V>[]) new Node[initialCapacity];
        this.threshold = (int) (initialCapacity * LOAD_FACTOR);
        this.size = 0;
    }

    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private int hash(K key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private int getBucketIndex(int hash) {
        return hash & (buckets.length - 1);
    }

    public V put(K key, V value) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }

        Node<K, V> newNode = new Node<>(hash, key, value, buckets[index]);
        buckets[index] = newNode;
        size++;

        if (size > threshold) {
            resize();
        }

        return null;
    }

    public V get(K key) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {
                return current.value;
            }
            current = current.next;
        }

        return null;
    }

    public V remove(K key) {
        int hash = hash(key);
        int index = getBucketIndex(hash);
        Node<K, V> current = buckets[index];
        Node<K, V> prev = null;

        while (current != null) {
            if (current.hash == hash &&
                    (current.key == key || (key != null && key.equals(current.key)))) {

                if (prev == null) {
                    buckets[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }

        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }


    public void clear() {
        buckets = (Node<K, V>[]) new Node[buckets.length];
        size = 0;
    }


    private void resize() {
        int newCapacity = buckets.length * 2;
        Node<K, V>[] oldBuckets = buckets;
        buckets = (Node<K, V>[]) new Node[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);

        for (Node<K, V> node : oldBuckets) {
            while (node != null) {
                Node<K, V> next = node.next;
                int newIndex = node.hash & (newCapacity - 1);
                node.next = buckets[newIndex];
                buckets[newIndex] = node;
                node = next;
            }
        }
    }

    public void printAll() {
        System.out.println("=== MyHashMap contents (size=" + size + ") ===");
        for (int i = 0; i < buckets.length; i++) {
            Node<K, V> node = buckets[i];
            if (node != null) {
                System.out.print("Bucket " + i + ": ");
                while (node != null) {
                    System.out.print("[" + node.key + "=" + node.value + "] -> ");
                    node = node.next;
                }
                System.out.println("null");
            }
        }
    }

    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        System.out.println("=== PUT ===");
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("one", 100);

        System.out.println("\n=== GET ===");
        System.out.println("get('one'): " + map.get("one"));
        System.out.println("get('two'): " + map.get("two"));
        System.out.println("get('three'): " + map.get("three"));
        System.out.println("get('four'): " + map.get("four"));

        System.out.println("\n=== REMOVE ===");
        System.out.println("remove('two'): " + map.remove("two"));
        System.out.println("get('two'): " + map.get("two"));
        System.out.println("size: " + map.size());

        System.out.println("\n=== NULL KEY ===");
        map.put(null, 999);
        System.out.println("get(null): " + map.get(null));
        System.out.println("size: " + map.size());

        map.printAll();
    }
}

