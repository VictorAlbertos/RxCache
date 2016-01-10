package sample_data;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.rx_cache.Invalidator;
import io.rx_cache.InvalidatorDynamicKey;
import io.rx_cache.Reply;
import io.rx_cache.internal.RxCache;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.functions.Func1;
import sample_data.cache.CacheProviders;
import sample_data.entities.Repo;
import sample_data.entities.User;
import sample_data.net.RestApi;

public class Repository {
    public static final int USERS_PER_PAGE = 25;

    public static Repository init(File cacheDir) {
        return new Repository(cacheDir);
    }

    private final CacheProviders cacheProviders;
    private final RestApi restApi;

    public Repository(File cacheDir) {
        cacheProviders = new RxCache.Builder()
                .persistence(cacheDir)
                .using(CacheProviders.class);

        restApi = new Retrofit.Builder()
                .baseUrl(RestApi.URL_BASE)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RestApi.class);
    }

    public Observable<Reply<List<User>>> getUsers(int idLastUserQueried, final boolean update) {
        return cacheProviders.getUsers(idLastUserQueried, restApi.getUsers(idLastUserQueried, USERS_PER_PAGE), new Invalidator() {
            @Override
            public boolean invalidate() {
                return update;
            }
        });
    }

    public Observable<Reply<List<Repo>>> getRepos(final String userName, final boolean update) {
        return cacheProviders.getRepos(restApi.getRepos(userName), userName, new InvalidatorDynamicKey() {
            @Override
            public Object dynamicKey() {
                return userName;
            }

            @Override
            public boolean invalidate() {
                return update;
            }
        });
    }

    public Observable<Reply<User>> loginUser(final String userName) {
        return restApi.getUser(userName).map(new Func1<Response<User>, Observable<Reply<User>>>() {
            @Override public Observable<Reply<User>> call(Response<User> userResponse) {

                if (!userResponse.isSuccess()) {
                    try {
                        ResponseError responseError = new Gson().fromJson(userResponse.errorBody().string(), ResponseError.class);
                        throw new RuntimeException(responseError.getMessage());
                    } catch (JsonParseException | IOException exception) {
                        throw new RuntimeException(exception.getMessage());
                    }
                }

                return cacheProviders.getCurrentUser(Observable.just(userResponse.body()), new Invalidator() {
                    @Override public boolean invalidate() {
                        return true;
                    }
                });
            }
        }).flatMap(new Func1<Observable<Reply<User>>, Observable<Reply<User>>>() {
            @Override public Observable<Reply<User>> call(Observable<Reply<User>> replyObservable) {
                return replyObservable;
            }
        }).map(new Func1<Reply<User>, Reply<User>>() {
            @Override public Reply<User> call(Reply<User> userReply) {
                return userReply;
            }
        });
    }

    public Observable<String> logoutUser() {
        return cacheProviders.getCurrentUser(Observable.<User>just(null), new Invalidator() {
            @Override public boolean invalidate() {
                return true;
            }
        }).map(new Func1<Reply<User>, String>() {
            @Override public String call(Reply<User> user) {
                return "Logout";
            }
        }).onErrorReturn(new Func1<Throwable, String>() {
            @Override public String call(Throwable throwable) {
                return "Logout";
            }
        });
    }

    public Observable<Reply<User>> getLoggedUser(boolean invalidate) {
        Observable<Reply<User>> cachedUser = cacheProviders.getCurrentUser(Observable.<User>just(null), new Invalidator() {
            @Override public boolean invalidate() {
                return false;
            }
        });

        Observable<Reply<User>> freshUser = cachedUser.flatMap(new Func1<Reply<User>, Observable<Reply<User>>>() {
            @Override public Observable<Reply<User>> call(Reply<User> userReply) {
                return loginUser(userReply.getData().getLogin());
            }
        });

        if (invalidate) return freshUser;
        else return cachedUser;
    }

    private static class ResponseError {
        private final String message;

        public ResponseError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
