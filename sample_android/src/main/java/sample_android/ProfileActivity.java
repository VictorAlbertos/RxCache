package sample_android;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import io.rx_cache.Reply;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sample_data.entities.User;
import victoralbertos.io.rxjavacache.R;

/**
 * Created by victor on 09/01/16.
 */
public class ProfileActivity extends BaseActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        getUserLogged(false);
    }

    private void showLogin() {
        findViewById(R.id.ll_logged).setVisibility(View.GONE);
        findViewById(R.id.ll_login).setVisibility(View.VISIBLE);

        findViewById(R.id.bt_login).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String userName = ((EditText) findViewById(R.id.et_name)).getText().toString();

                subscription.unsubscribe();
                subscription = getRepository().loginUser(userName)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Reply<User>>() {
                            @Override public void onCompleted() {}

                            @Override public void onError(Throwable e) {
                                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override public void onNext(Reply<User> user) {
                                showLogged(user);
                            }
                        });
            }
        });
    }

    private void showLogged(Reply<User> userReply) {
        findViewById(R.id.ll_login).setVisibility(View.GONE);
        findViewById(R.id.ll_logged).setVisibility(View.VISIBLE);

        User user = userReply.getData();

        Picasso.with(this).load(user.getAvatarUrl())
                .centerCrop()
                .fit()
                .into((ImageView) findViewById(R.id.iv_avatar));

        ((TextView)findViewById(R.id.tv_name)).setText(user.getLogin());
        ((TextView)findViewById(R.id.tv_source)).setText("Loaded from: " + userReply.getSource().name());

        findViewById(R.id.bt_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserLogged(true);
            }
        });

        findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                subscription.unsubscribe();
                subscription = getRepository().logoutUser()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String feedback) {
                                Toast.makeText(ProfileActivity.this, feedback, Toast.LENGTH_LONG).show();
                                showLogin();
                            }
                        });
            }
        });
    }

    private void getUserLogged(boolean update) {
        subscription.unsubscribe();
        subscription = getRepository().getLoggedUser(update)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Reply<User>>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        showLogin();
                    }

                    @Override public void onNext(Reply<User> user) {
                        showLogged(user);
                    }
                });
    }
}
