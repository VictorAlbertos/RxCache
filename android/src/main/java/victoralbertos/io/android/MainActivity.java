package victoralbertos.io.android;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.internal.RxCache;
import rx.Observable;

/**
 * Created by victor on 21/01/16.
 */
public class MainActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create integration test for max mg limit and clearing expired data


       final RxProviders rxProviders = new RxCache.Builder()
                .setMaxMBPersistenceCache(50)
                .persistence(getApplicationContext().getFilesDir())
                .using(RxProviders.class);

        /* for (int i = 0; i < 1000; i++) {
            String key = System.currentTimeMillis() + i + "";
            rxProviders.getMocksEphemeralPaginate(createObservableMocks(100), new DynamicKey(key))
                    .subscribe();
        }*/

    }

    private Observable<List<Mock>> createObservableMocks(int size) {
        List<Mock> mocks = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            mocks.add(new Mock("Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, " +
                    "making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, " +
                    "consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes " +
                    "from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the " +
                    "theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32."));
        }
        return Observable.just(mocks);
    }
}
