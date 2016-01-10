package sample_android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import rx.Subscription;
import rx.subscriptions.Subscriptions;
import sample_data.Repository;
import victoralbertos.io.rxjavacache.R;

/**
 * Created by victor on 09/01/16.
 */
public class BaseActivity extends AppCompatActivity {
    protected Subscription subscription = Subscriptions.empty();

    protected Repository getRepository() {
        return ((SampleAndroidApp)getApplication()).getRepository();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recreate_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_recreate_activity) {
            recreate();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override protected void onPause() {
        super.onPause();
        subscription.unsubscribe();
    }
}
