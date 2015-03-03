package ua.dp.skillsup.lrulist;

import lrulist.LRUList;
import lrulist.SimpleLRUList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class LRUListTestForAdders {
  private static final int LRU_CAPACITY = 100;
  private static final int THREADS_COUNT = 8;

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  private LRUList<Integer, Integer> lruList;
  private CountDownLatch latch;

  @Test
  public void testAdd() throws Exception {
    for (int k = 0; k < 100; k++) {
      latch = new CountDownLatch(1);
      lruList = new SimpleLRUList<Integer, Integer>(LRU_CAPACITY);

      final List<Future<Void>> futures = new ArrayList<Future<Void>>();
      for (int i = 0; i < THREADS_COUNT; i++) {
        futures.add(executorService.submit(new Adder(i)));
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
          nextExpectedValue = item + 1;
        } else {
          Assert.assertEquals(nextExpectedValue, item);
          nextExpectedValue = item + 1;
        }

        Assert.assertEquals(i, item / 100);
      }

      if (nextExpectedValue > -1)
        Assert.assertEquals(nextExpectedValue - 1, i * 100 + 99);
    }
  }


  private final class Adder implements Callable<Void> {
    private final int order;


    public Adder(int order) {
      this.order = order;
    }

    @Override
    public Void call() throws Exception {
      latch.await();

      for (int i = order * 100; i < (order + 1) * 100; i++) {
        lruList.put(i, i);
        Thread.yield();
      }

      return null;
    }
  }


}
