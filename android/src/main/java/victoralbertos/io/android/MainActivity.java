package victoralbertos.io.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictProvider;
import io.rx_cache2.Reply;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by victor on 21/01/16.
 */
/**
 * TODO reappear steps:
 * monitor Paging request!
 * 1.onclick PageReFresh btn;
 * 2.onclick LoadMore btn:
 * i get some log:
 *
 * 08-14 16:17:35.297 25835-25854/victoralbertos.io.android D/okhttp_getUsers: lastIdQueried:1,source:CLOUD
 * 08-14 16:17:37.990 25835-25853/victoralbertos.io.android D/okhttp_getUsers: lastIdQueried:20,source:CLOUD (loadmore has no data.so request from cloud is normal)
 *
 * 3.onclick PageReFresh btn;
 * according to descriptions from readme-- EvictProvider allows to explicitly evict all the data associated with the provider.
 * so the loadmore should request from cloud no matter EvictProvider.evict is true or no.
 *
 * but there are logs after onclick loadmore btn:
 * D/okhttp_getUsers: lastIdQueried:20,source:PERSISTENCE
 *
 * so the is my pr reason.
 *
 * Thanks you read my pr!
 */
public class MainActivity extends Activity {
    String TAG="Rxtest";
    private Providers mProvider;
    private RxCache rxCache;
    private int lastid=1;
    private TextView mtv;
    private File externalCacheDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        mtv = (TextView) findViewById(R.id.tv);
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);//ouput request basic message
        OkHttpClient.Builder builder = new OkHttpClient.Builder().addNetworkInterceptor(httpLoggingInterceptor);

        Retrofit.Builder client = new Retrofit.Builder().baseUrl("https://api.github.com/").client(builder.build());
        client.addConverterFactory(GsonConverterFactory.create(new Gson()));
        client.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        File cacheDir = getFilesDir();
        new RxCache.Builder()
                .persistence(cacheDir, new GsonSpeaker())
                .using(Providers.class);
        Retrofit mretrofit = client.build();
        mProvider = mretrofit.create(Providers.class);
        externalCacheDir = getExternalCacheDir();
        Log.d("Rxcache","externalCacheDir:"+ externalCacheDir.toString() );
        rxCache = new RxCache.Builder().persistence(externalCacheDir, new GsonSpeaker(new Gson()));

    }
    public void ClearCache(View view){
        if (externalCacheDir!=null) {
            deleteFile(externalCacheDir);
            File[] files = externalCacheDir.listFiles();
            if (files==null){
                Toast.makeText(this,"delete:-1",Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this,"delete:"+files.length,Toast.LENGTH_LONG).show();
        }
    }
    public void anotherProvider(View view){
        if (lastid==1){
            Toast.makeText(this,"Please onclick PageRefresh btn first",Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "LoadMore: ");
        Observable.just(mProvider.getUsers(lastid,10))
                .flatMap(new Function<Observable<List<User>>, ObservableSource<List<User>>>() {
                    @Override
                    public ObservableSource<List<User>> apply(Observable<List<User>> listObservable) throws Exception {
                        return rxCache.using(CommonCache.class).getUsers2(listObservable,new DynamicKey(lastid),new EvictProvider(false))
                                .map(new Function<Reply<List<User>>, List<User>>() {
                                    @Override
                                    public List<User> apply(final Reply<List<User>> listReply) throws Exception {
                                        Log.d("okhttp_getUsers","lastIdQueried:"+lastid+",source:"+listReply.getSource());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mtv.setText("lastIdQueried:"+lastid+",source:"+listReply.getSource());
                                            }
                                        });
                                        return listReply.getData();
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<User>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<User> users) {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("okhttp_Requesterror",e.toString());

            }

            @Override
            public void onComplete() {

            }
        });
    }
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                boolean delete = f.delete();
                Log.d(TAG, "delete: "+delete);
            }
        } else if (file.exists()) {
        }
    }
    public void PageReFresh(View view){
        lastid=1;
        Log.d(TAG, "PageReFresh: ");
        Observable.just(mProvider.getUsers(lastid,10))
                .flatMap(new Function<Observable<List<User>>, ObservableSource<List<User>>>() {
            @Override
            public ObservableSource<List<User>> apply(Observable<List<User>> listObservable) throws Exception {
                return rxCache.using(CommonCache.class).getUsers(listObservable,new DynamicKey(lastid),new EvictProvider(true))
                        .map(new Function<Reply<List<User>>, List<User>>() {
                            @Override
                            public List<User> apply(final Reply<List<User>> listReply) throws Exception {
                                Log.d("okhttp_getUsers","lastIdQueried:"+lastid+",source:"+listReply.getSource());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mtv.setText("lastIdQueried:"+lastid+",source:"+listReply.getSource());
                                    }
                                });

                                return listReply.getData();
                            }
                        });
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<User>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<User> users) {
                lastid = users.get(users.size() - 1).getId();
            }

            @Override
            public void onError(Throwable e) {
                Log.d("okhttp_Requesterror",e.toString());

            }

            @Override
            public void onComplete() {

            }
        });


    }


    public void LoadMore(View view){
        if (lastid==1){
            Toast.makeText(this,"Please onclick PageRefresh btn first",Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "LoadMore: ");
        Observable.just(mProvider.getUsers(lastid,10))
                .flatMap(new Function<Observable<List<User>>, ObservableSource<List<User>>>() {
                    @Override
                    public ObservableSource<List<User>> apply(Observable<List<User>> listObservable) throws Exception {
                        return rxCache.using(CommonCache.class).getUsers(listObservable,new DynamicKey(lastid),new EvictProvider(false))
                                .map(new Function<Reply<List<User>>, List<User>>() {
                                    @Override
                                    public List<User> apply(final Reply<List<User>> listReply) throws Exception {
                                        Log.d("okhttp_getUsers","lastIdQueried:"+lastid+",source:"+listReply.getSource());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mtv.setText("lastIdQueried:"+lastid+",source:"+listReply.getSource());
                                            }
                                        });
                                        return listReply.getData();
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<User>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<User> users) {

            }

            @Override
            public void onError(Throwable e) {
                Log.d("okhttp_Requesterror",e.toString());

            }

            @Override
            public void onComplete() {

            }
        });
    }

}
