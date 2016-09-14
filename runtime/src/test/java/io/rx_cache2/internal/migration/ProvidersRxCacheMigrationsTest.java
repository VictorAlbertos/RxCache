package io.rx_cache2.internal.migration;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.rx_cache2.Migration;
import io.rx_cache2.SchemeMigration;
import io.rx_cache2.internal.Jolyglot$;
import io.rx_cache2.internal.RxCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ProvidersRxCacheMigrationsTest {
  @ClassRule static public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test public void _1_Populate_Mocks() {
    populateMocks();
  }

  @Test public void _2_When_Migrations_Working_Request_Will_Be_Hold_Until_Finish() {
    int countFiles = temporaryFolder.getRoot().listFiles().length;
    assert countFiles > 0;

    ProvidersMigrations providersMigrations = new RxCache.Builder()
        .persistence(temporaryFolder.getRoot(), Jolyglot$.newInstance())
        .using(ProvidersMigrations.class);

    TestObserver<List<Mock1>> testObserver =
        providersMigrations.getMocks(Observable.just(Arrays.asList(new Mock1()))).test();
    testObserver.awaitTerminalEvent();
    testObserver.assertNoErrors();
    testObserver.assertValueCount(1);
  }

  private void populateMocks() {
    Providers providers = new RxCache.Builder()
        .persistence(temporaryFolder.getRoot(), Jolyglot$.newInstance())
        .using(Providers.class);

    TestObserver<List<Mock1>> testObserver = providers.getMocks(getMocks()).test();
    testObserver.awaitTerminalEvent();

    assertThat(testObserver.values().get(0).size(), is(SIZE_MOCKS));
  }

  private static int SIZE_MOCKS = 1000;

  private Observable<List<Mock1>> getMocks() {
    List<Mock1> mocks = new ArrayList<>();

    for (int i = 0; i < SIZE_MOCKS; i++) {
      mocks.add(new Mock1());
    }

    return Observable.just(mocks);
  }

  private interface Providers {
    Observable<List<Mock1>> getMocks(Observable<List<Mock1>> mocks);
  }

  @SchemeMigration({@Migration(version = 1, evictClasses = Mock1.class)})
  private interface ProvidersMigrations {
    Observable<List<Mock1>> getMocks(Observable<List<Mock1>> mocks);
  }

  public static class Mock1 {
    private final String payload = "Lorem Ipsum is simply dummy text of the printing and " +
        "typesetting industry. Lorem Ipsum has been the industry's standard dummy text " +
        "ever since the 1500s, when an unknown printer took a galley of type and scrambled " +
        "it to make a type specimen book. It has survived not only five centuries";

    public Mock1() {

    }

    public String getPayload() {
      return payload;
    }
  }
}
