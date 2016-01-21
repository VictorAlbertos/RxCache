package victoralbertos.io.android;

import android.app.Activity;
import android.os.Bundle;

import io.rx_cache.internal.RxCache;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by victor on 21/01/16.
 */
public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RxProviders rxProviders = new RxCache.Builder().persistence(getApplicationContext().getFilesDir()).using(RxProviders.class);
        rxProviders.getMessage(Observable.just("message")).subscribe(new Action1<String>() {
            @Override public void call(String message) {
                assert message.equals(message);
            }
        });

    }
}
