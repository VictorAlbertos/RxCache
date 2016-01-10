package sample_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.Reply;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sample_data.Repository;
import sample_data.entities.User;
import victoralbertos.io.rxjavacache.R;

/**
 * Created by victor on 09/01/16.
 */
public class UsersActivity extends BaseActivity {
    private UsersAdapter usersAdapter;
    private List<UserWithSource> users = new ArrayList<>();
    private SwipeRefreshLayout srl_users;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity);
        setUpRecyclerView();
    }

    private boolean isLoading;
    private void setUpRecyclerView() {
        usersAdapter = new UsersAdapter();

        RecyclerView rv_users = (RecyclerView) findViewById(R.id.rv_users);
        rv_users.setAdapter(usersAdapter);
        rv_users.setLayoutManager(new GridLayoutManager(this, 2));

        Paginate.Callbacks callbacks = new Paginate.Callbacks() {
            @Override public void onLoadMore() {
                requestUsers(false);
            }

            @Override public boolean isLoading() {
                return isLoading;
            }

            @Override public boolean hasLoadedAllItems() {
                return false;
            }
        };

        Paginate.with(rv_users, callbacks)
                .setLoadingTriggerThreshold(Repository.USERS_PER_PAGE)
                .build();

        srl_users = (SwipeRefreshLayout) findViewById(R.id.srl_users);
        srl_users.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                requestUsers(true);
            }
        });
    }

    private void requestUsers(final boolean pullToRefresh) {
        isLoading = true;

        int lastUserId = 1;
        if (!users.isEmpty()) lastUserId = users.get(users.size()-1).getUser().getId();
        if (pullToRefresh) lastUserId = 1;

        subscription.unsubscribe();
        subscription = getRepository().getUsers(lastUserId, pullToRefresh)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Reply<List<User>>>() {
                    @Override public void call(Reply<List<User>> reply) {
                        if (pullToRefresh) users.clear();

                        for (User user : reply.getData()) {
                            users.add(new UserWithSource(user, reply.getSource().name()));
                        }

                        usersAdapter.notifyDataSetChanged();
                        isLoading = false;
                        srl_users.setRefreshing(false);
                    }
                });
    }

    public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_view, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(ViewHolder holder, int position) {
            final UserWithSource userWithSource = users.get(position);
            holder.tv_name.setText(userWithSource.getUser().getLogin());
            holder.tv_source.setText(userWithSource.getSource());

            Picasso.with(holder.iv_avatar.getContext()).load(userWithSource.getUser().getAvatarUrl())
                    .centerCrop()
                    .fit()
                    .into(holder.iv_avatar);

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ReposUserActivity.selectedUser = userWithSource.getUser();
                    startActivity(new Intent(UsersActivity.this, ReposUserActivity.class));
                }
            });
        }

        @Override public int getItemCount() {
            return users.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            View root;
            ImageView iv_avatar;
            TextView tv_name, tv_source;

            public ViewHolder(View root) {
                super(root);
                this.root = root;
                iv_avatar = (ImageView) root.findViewById(R.id.iv_avatar);
                tv_name = (TextView) root.findViewById(R.id.tv_name);
                tv_source = (TextView) root.findViewById(R.id.tv_source);
            }
        }
    }
}