package ua.dp.skillsup.lrulist;

import lrulist.LRUList;
import lrulist.SimpleLRUList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class LRUListTestForGetters {
  private static final int LRU_CAPACITY = 100;
  private static final int THREADS_COUNT = 8;

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  private LRUList<Integer, Integer> lruList;
  private CountDownLatch latch;

  @Test
  public void testGet() throws Exception {
    for (int k = 0; k < 100; k++) {
      latch = new CountDownLatch(1);
      lruList = new SimpleLRUList<Integer, Integer>(LRU_CAPACITY);

      for (int n = 0; n < 100; n++) {
        for (int i = 0; i < THREADS_COUNT; i++) {
          lruList.put(i * 100 + n, i * 100 + n);
        }
      }

      Assert.assertEquals(LRU_CAPACITY, listSize());

      final List<Future<Void>> futures = new ArrayList<Future<Void>>();
      for (int i = 0; i < THREADS_COUNT; i++) {
        futures.add(executorService.submit(new Getter(i)));
      }

      latch.countDown();
      for (Future<Void> future : futures) {
        future.get();
      }

      validateLRUListOrder();
    }
  }

  private void validateLRUListOrder() {

    List<List<Integer>> dataPartitions = new ArrayList<List<Integer>>();
    for (int i = 0; i < THREADS_COUNT; i++) {
      dataPartitions.add(new ArrayList<Integer>());
    }

    Iterator<LRUList.Entry<Integer, Integer>> entryIterator = lruList.iterator();
    while (entryIterator.hasNext()) {
      LRUList.Entry<Integer, Integer> entry = entryIterator.next();
      int index = entry.getKey() / 100;

      List<Integer> partition = dataPartitions.get(index);
      partition.add(entry.getKey());
    }

    int size = 0;
    for (List<Integer> partition : dataPartitions) {
      size += partition.size();
    }

    Assert.assertEquals(LRU_CAPACITY, size);


    for (int i = 0; i < dataPartitions.size(); i++) {
      Integer nextExpectedValue = -1;

      List<Integer> partition = dataPartitions.get(i);
      for (Integer item : partition) {
        if (nextExpectedValue == -1) {
          nextExpectedValue = item - 1;
        } else {
          Assert.assertEquals(nextExpectedValue, item);
          nextExpectedValue = item - 1;
        }

        Assert.assertEquals(i, item / 100);
      }
    }
  }


  private int listSize() {
    int count = 0;

    Iterator<LRUList.Entry<Integer, Integer>> iterator = lruList.iterator();
    while (iterator.hasNext()) {
      count++;
      iterator.next();
    }

    return count;
  }

  private final class Getter implements Callable<Void> {
    private final int order;


    public Getter(int order) {
      this.order = order;
    }

    @Override
    public Void call() throws Exception {
      latch.await();

      for (int i = (order + 1) * 100 - 1; i >= order * 100; i--) {
        Integer value = lruList.get(i);
        if (value != null) {
          Assert.assertEquals(i, value.intValue());
        }

        Thread.yield();
      }

      return null;
    }
  }
}
