/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rx_cache.internal;

import io.rx_cache.ActionsList;
import io.rx_cache.ConfigProvider;
import io.rx_cache.EvictProvider;
import io.victoralbertos.jolyglot.GsonSpeaker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ActionsListTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private ProcessorProviders processProvider;

  @Before public void setUp() {
    processProvider = DaggerRxCacheComponent.builder()
        .rxCacheModule(new RxCacheModule(temporaryFolder.getRoot(),
            false, null, null, null, new GsonSpeaker()))
        .build().providers();
  }

  @Test public void Add_All() {
    checkInitialState();
    addAll(10);
  }

  @Test public void Add_All_First() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .addAllFirst(Arrays.asList(new Mock("11"), new Mock("12")))
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(12));
    assertThat(mocks.get(0).getMessage(), is("11"));
    assertThat(mocks.get(1).getMessage(), is("12"));
  }

  @Test public void Add_All_Last() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .addAllLast(Arrays.asList(new Mock("11"), new Mock("12")))
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(12));
    assertThat(mocks.get(10).getMessage(), is("11"));
    assertThat(mocks.get(11).getMessage(), is("12"));
  }

  @Test public void Add_First() {
    checkInitialState();

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .addFirst(new Mock("1"))
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(1));
    assertThat(mocks.get(0).getMessage(), is("1"));
  }

  @Test public void Add_Last() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .addLast(new Mock("11"))
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(11));
    assertThat(mocks.get(10).getMessage(), is("11"));
  }

  @Test public void Add() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .add(new ActionsList.Func2() {
          @Override public boolean call(int position, int count) {
            return position == 5;
          }
        }, new Mock("6_added"))
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(11));
    assertThat(mocks.get(5).getMessage(), is("6_added"));
  }

  @Test public void EvictFirst() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictFirst()
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(9));
    assertThat(mocks.get(0).getMessage(), is("1"));
  }

  @Test public void EvictFirstN() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictFirstN(4)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(6));
    assertThat(mocks.get(0).getMessage(), is("4"));
  }

  @Test public void EvictFirstExposingCount() {
    checkInitialState();
    addAll(10);

    //do not evict
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictFirst(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 10;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));

    //evict
    testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictFirst(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 9;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(9));
    assertThat(mocks.get(0).getMessage(), is("1"));
  }

  @Test public void EvictFirstNExposingCount() {
    checkInitialState();
    addAll(10);

    //do not evict
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictFirstN(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 10;
          }
        }, 5)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));

    //evict
    testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictFirstN(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 9;
          }
        }, 5)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(5));
    assertThat(mocks.get(0).getMessage(), is("5"));
    assertThat(mocks.get(1).getMessage(), is("6"));
  }

  @Test public void EvictLast() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictLast()
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(9));
    assertThat(mocks.get(8).getMessage(), is("8"));
  }

  @Test public void EvictLastN() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictLastN(4)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(6));
    assertThat(mocks.get(0).getMessage(), is("0"));
  }

  @Test public void EvictLastExposingCount() {
    checkInitialState();
    addAll(10);

    //do not evict
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictLast(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 10;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));

    //evict
    testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictLast(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 9;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(9));
    assertThat(mocks.get(8).getMessage(), is("8"));
  }

  @Test public void EvictLastNExposingCount() {
    checkInitialState();
    addAll(10);

    //do not evict
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictLastN(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 10;
          }
        }, 5)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));

    //evict
    testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictLastN(new ActionsList.Func1Count() {
          @Override public boolean call(int count) {
            return count > 9;
          }
        }, 5)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(5));
    assertThat(mocks.get(0).getMessage(), is("0"));
    assertThat(mocks.get(1).getMessage(), is("1"));
  }

  @Test public void EvictExposingElementCurrentIteration() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evict(new ActionsList.Func1Element<Mock>() {
          @Override public boolean call(Mock element) {
            return element.getMessage().equals("3");
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(9));
    assertThat(mocks.get(3).getMessage(), is("4"));
  }

  @Test public void EvictExposingCountAndPositionAndElementCurrentIteration() {
    checkInitialState();
    addAll(10);

    //do not evict
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evict(new ActionsList.Func3<Mock>() {
          @Override public boolean call(int position, int count, Mock element) {
            return count > 10 && element.getMessage().equals("3");
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));
    assertThat(mocks.get(3).getMessage(), is("3"));

    //evict
    testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evict(new ActionsList.Func3<Mock>() {
          @Override public boolean call(int position, int count, Mock element) {
            return count > 9 && element.getMessage().equals("3");
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(9));
    assertThat(mocks.get(3).getMessage(), is("4"));
  }

  @Test public void EvictIterable() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictIterable(new ActionsList.Func3<Mock>() {
          @Override public boolean call(int position, int count, Mock element) {
            return element.getMessage().equals("2") || element.getMessage().equals("3");
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(8));
    assertThat(mocks.get(2).getMessage(), is("4"));
    assertThat(mocks.get(3).getMessage(), is("5"));
  }

  @Test public void EvictAll() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictAll()
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(0));
  }

  @Test public void EvictAllKeepingFirstN() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictAllKeepingFirstN(3)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(3));
    assertThat(mocks.get(0).getMessage(), is("0"));
  }

  @Test public void EvictAllKeepingLastN() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .evictAllKeepingLastN(7)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(7));
    assertThat(mocks.get(0).getMessage(), is("3"));
  }

  @Test public void UpdateExposingElementCurrentIteration() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .update(new ActionsList.Func1Element<Mock>() {
          @Override public boolean call(Mock element) {
            return element.getMessage().equals("5");
          }
        }, new ActionsList.Replace<Mock>() {
          @Override public Mock call(Mock element) {
            element.setMessage("5_updated");
            return element;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));
    assertThat(mocks.get(5).getMessage(), is("5_updated"));
  }

  @Test public void UpdateExposingCountAndPositionAndElementCurrentIteration() {
    checkInitialState();
    addAll(10);

    //do not evict
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .update(new ActionsList.Func3<Mock>() {
          @Override public boolean call(int position, int count, Mock element) {
            return count > 10 && element.getMessage().equals("5");
          }
        }, new ActionsList.Replace<Mock>() {
          @Override public Mock call(Mock element) {
            element.setMessage("5_updated");
            return element;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));
    assertThat(mocks.get(5).getMessage(), is("5"));

    //evict
    testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .update(new ActionsList.Func3<Mock>() {
          @Override public boolean call(int position, int count, Mock element) {
            return count > 9 && element.getMessage().equals("5");
          }
        }, new ActionsList.Replace<Mock>() {
          @Override public Mock call(Mock element) {
            element.setMessage("5_updated");
            return element;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));
    assertThat(mocks.get(5).getMessage(), is("5_updated"));
  }

  @Test public void UpdateIterableExposingElementCurrentIteration() {
    checkInitialState();
    addAll(10);

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .updateIterable(new ActionsList.Func1Element<Mock>() {
          @Override public boolean call(Mock element) {
            return element.getMessage().equals("5") || element.getMessage().equals("6");
          }
        }, new ActionsList.Replace<Mock>() {
          @Override public Mock call(Mock element) {
            element.setMessage("5_or_6_updated");
            return element;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));
    assertThat(mocks.get(5).getMessage(), is("5_or_6_updated"));
    assertThat(mocks.get(6).getMessage(), is("5_or_6_updated"));
  }

  @Test public void UpdateIterableExposingCountAndPositionAndElementCurrentIteration() {
    checkInitialState();
    addAll(10);

    //do not evict
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .updateIterable(new ActionsList.Func3<Mock>() {
          @Override public boolean call(int position, int count, Mock element) {
            return count > 10 && (element.getMessage().equals("5") || element.getMessage()
                .equals("6"));
          }
        }, new ActionsList.Replace<Mock>() {
          @Override public Mock call(Mock element) {
            element.setMessage("5_or_6_updated");
            return element;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));
    assertThat(mocks.get(5).getMessage(), is("5"));
    assertThat(mocks.get(6).getMessage(), is("6"));

    //evict
    testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .updateIterable(new ActionsList.Func3<Mock>() {
          @Override public boolean call(int position, int count, Mock element) {
            return count > 9 && (element.getMessage().equals("5") || element.getMessage()
                .equals("6"));
          }
        }, new ActionsList.Replace<Mock>() {
          @Override public Mock call(Mock element) {
            element.setMessage("5_or_6_updated");
            return element;
          }
        })
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();

    mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(10));
    assertThat(mocks.get(5).getMessage(), is("5_or_6_updated"));
    assertThat(mocks.get(6).getMessage(), is("5_or_6_updated"));
  }

  private Observable<List<Mock>> cache() {
    ConfigProvider configProvider = new ConfigProvider("mocks",
        null, null, false,
        false, false, null, null, Observable.<List<Mock>>just(new ArrayList<Mock>()),
        new EvictProvider(false));

    return ((ProcessorProvidersBehaviour) processProvider).getData(configProvider);
  }

  private ActionsList.Evict<Mock> evict() {
    return new ActionsList.Evict<Mock>() {
      @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {
        ConfigProvider configProvider = new ConfigProvider("mocks",
            null, null, false,
            false, false, null, null, elements, new EvictProvider(true));

        return ((ProcessorProvidersBehaviour) processProvider).getData(configProvider);
      }
    };
  }

  private void checkInitialState() {
    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();
    cache().subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent();

    if (testSubscriber.getOnNextEvents().isEmpty()) return;

    List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
    assertThat(mocks.size(), is(0));
  }

  private void addAll(int count) {
    List<Mock> mocks = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      mocks.add(new Mock(String.valueOf(i)));
    }

    TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

    ActionsList.with(evict(), cache())
        .addAll(new ActionsList.Func2() {
          @Override public boolean call(int position, int count) {
            return position == count;
          }
        }, mocks)
        .toObservable()
        .subscribe(testSubscriber);

    testSubscriber.awaitTerminalEvent();
    assertThat(testSubscriber.getOnNextEvents().get(0).size(),
        is(count));
  }

  public interface ProvidersActions {
    Observable<List<Mock>> mocks(Observable<List<Mock>> mocks, EvictProvider evictProvider);
  }
}
