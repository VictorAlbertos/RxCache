package victoralbertos.io.android_test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import io.rx_cache.internal.RxCache;
import rx.Observable;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Providers providers = new RxCache.Builder()
                .persistence(getApplicationContext().getFilesDir())
                .using(Providers.class);

        providers.getStrings(Observable.just("1")).subscribe(new Action1<String>() {
            @Override public void call(String value) {
                Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
            }
        });

        providers.getStrings(Observable.<String>just(null)).subscribe(new Action1<String>() {
            @Override public void call(String value) {
                Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
