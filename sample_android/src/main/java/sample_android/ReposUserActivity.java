package sample_android;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.Reply;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sample_data.entities.Repo;
import sample_data.entities.User;
import victoralbertos.io.rxjavacache.R;

/**
 * Created by victor on 09/01/16.
 */
public class ReposUserActivity extends BaseActivity {
    public static User selectedUser;
    private ReposAdapter reposAdapter;
    private List<RepoWithSource> repos = new ArrayList<>();
    private SwipeRefreshLayout srl_repos;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repos_activity);

        TextView tv_header = (TextView) findViewById(R.id.tv_header);
        tv_header.setText("Repos user " + selectedUser.getLogin());

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        reposAdapter = new ReposAdapter();

        RecyclerView rv_repos = (RecyclerView) findViewById(R.id.rv_repos);
        rv_repos.setAdapter(reposAdapter);
        rv_repos.setLayoutManager(new LinearLayoutManager(this));

        srl_repos = (SwipeRefreshLayout) findViewById(R.id.srl_repos);
        srl_repos.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                requestRepos(true);
            }
        });

        requestRepos(false);
    }

    private void requestRepos(final boolean pullToRefresh) {
        subscription.unsubscribe();
        subscription = getRepository().getRepos(selectedUser.getLogin(), pullToRefresh)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Reply<List<Repo>>>() {
                    @Override public void call(Reply<List<Repo>> reply) {
                        if (pullToRefresh) repos.clear();

                        for (Repo repo : reply.getData()) {
                            repos.add(new RepoWithSource(repo, reply.getSource().name()));
                        }

                        reposAdapter.notifyDataSetChanged();
                        srl_repos.setRefreshing(false);
                    }
                });
    }

    public class ReposAdapter extends RecyclerView.Adapter<ReposAdapter.ViewHolder> {

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repo_view, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(ViewHolder holder, int position) {
            final RepoWithSource repoWithSource = repos.get(position);
            holder.tv_name.setText(repoWithSource.getRepo().getName());
            holder.tv_source.setText(repoWithSource.getSource());
        }

        @Override public int getItemCount() {
            return repos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name, tv_source;

            public ViewHolder(View root) {
                super(root);
                tv_name = (TextView) root.findViewById(R.id.tv_name);
                tv_source = (TextView) root.findViewById(R.id.tv_source);
            }
        }
    }
}
