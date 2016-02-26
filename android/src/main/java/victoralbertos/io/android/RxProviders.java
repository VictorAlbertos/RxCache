package victoralbertos.io.android;

import rx.Observable;

/**
 * Created by victor on 21/01/16.
 */
public interface RxProviders {
    Observable<String> getMessage(Observable<String> message);
}
