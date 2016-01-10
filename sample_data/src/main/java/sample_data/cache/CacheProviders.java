package sample_data.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rx_cache.DynamicKey;
import io.rx_cache.InvalidateCache;
import io.rx_cache.Invalidator;
import io.rx_cache.InvalidatorDynamicKey;
import io.rx_cache.LifeCache;
import io.rx_cache.Loader;
import io.rx_cache.Reply;
import rx.Observable;
import sample_data.entities.Repo;
import sample_data.entities.User;

/**
 * Created by victor on 04/01/16.
 */
public interface CacheProviders {

    @LifeCache(duration = 2, timeUnit = TimeUnit.MINUTES)
    Observable<Reply<List<Repo>>> getRepos(@Loader Observable<List<Repo>> repos, @DynamicKey String userName,
                                           @InvalidateCache InvalidatorDynamicKey invalidatorDynamicKey);

    @LifeCache(duration = 2, timeUnit = TimeUnit.MINUTES)
    Observable<Reply<List<User>>> getUsers(@DynamicKey int page, @Loader Observable<List<User>> repos,
                                           @InvalidateCache Invalidator invalidator);

    Observable<Reply<User>> getCurrentUser(@Loader Observable<User> getUser, @InvalidateCache Invalidator invalidator);
}
