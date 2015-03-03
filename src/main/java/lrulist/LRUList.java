package lrulist;

import java.util.Iterator;

/**
 * Basic implementation of thread safe LRU list with limited size.
 * LRU list contains only 2 methods to operate with it.
 * <ol>
 *   <li>
 *     <code>get()</code>  which returns value which is hold by LRU list or <code>null</code> if given value does not exist.
 *     Given method put key-value pair to the head of LRU list.
 *   </li>
 *   <li>
 *     <code>put()</code> add new key-value pair or entry to the LRU list. LRU list has limited capacity and if capacity
 *     limit of LRU list is reached earliest entries which where touched or added by get/put methods of LRU list are kept but the
 *     rest of them are evicted.
 *   </li>
 * </ol>
 */
public interface LRUList<K, V> {
  /**
   * Returns value which is hold by LRU list or <code>null</code> if given value does not exist
   * Given method put key-value pair to the head of LRU list.
   *
   * @param key Key associated with value which is looked for.
   * @return value which is hold by LRU list or <code>null</code> if given value does not exist.
   */
  V get(K key);

  /**
   * Add new key-value pair or entry to the LRU list. LRU list has limited capacity and if capacity
   * limit of LRU list is reached earliest entries which where touched or added by get/put methods of LRU list are kept but the
   * rest of them are evicted.
   * If key already exist in LRU list value associated with this list will be hold but not replaced.
   * @param key Key is going to be added.
   * @param value  Value is going to be added.
   * @return If key already exists then value associated with given key will be returned and old value will not be replaced by new one,
   *         otherwise new value will be returned.
   */
  V put(K key, V value);

  /**
   * @return Iterator for LRU list entries which iterate for earliest to latest entries.
   */
  Iterator<Entry<K, V>> iterator();

  interface Entry<K, V> {
    K getKey();
    V getValue();
  }
}
