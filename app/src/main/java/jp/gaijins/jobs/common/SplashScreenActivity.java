package jp.gaijins.jobs.common;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;

import java.util.concurrent.TimeUnit;

import jp.gaijins.jobs.R;
import jp.gaijins.jobs.feed.HomeActivity;
import jp.gaijins.jobs.gtm.ContainerHolderSingleton;

/**
 * Created by nayak.vishal on 2015/12/10.
 *
 * Displays simple splash screen while GTM container is loading. Once the container is loaded,
 * launches the {@link jp.gaijins.jobs.feed.HomeActivity}.
 */
public class SplashScreenActivity extends Activity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private static final long TIMEOUT_FOR_CONTAINER_OPEN_MILLISECONDS = 2000;
    private static final String JOBS_CONTAINER_ID = "GTM-KS9L5Z";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splashscreen);

        TagManager tagManager = TagManager.getInstance(this);

        // Modify the log level of the logger to print out not only
        // warning and error messages, but also verbose, debug, info messages.
        tagManager.setVerboseLoggingEnabled(true);

        PendingResult<ContainerHolder> pending =
                tagManager.loadContainerPreferNonDefault(JOBS_CONTAINER_ID, R.raw.gtm_ks9l5z_json);

        // The onResult method will be called as soon as one of the following happens:
        //     1. a saved container is loaded
        //     2. if there is no saved container, a network container is loaded
        //     3. the 2-second timeout occurs
        pending.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                ContainerHolderSingleton.setContainerHolder(containerHolder);
                if (!containerHolder.getStatus().isSuccess()) {
                    Log.e(TAG, "failure loading gtm container");
                    Toast.makeText(getApplicationContext(), R.string.load_error, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                ContainerHolderSingleton.setContainerHolder(containerHolder);
                containerHolder.setContainerAvailableListener(new ContainerLoadedCallback());
                startHomeActivity();
            }
        }, TIMEOUT_FOR_CONTAINER_OPEN_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private void startHomeActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    private static class ContainerLoadedCallback implements ContainerHolder.ContainerAvailableListener {
        @Override
        public void onContainerAvailable(ContainerHolder containerHolder, String containerVersion) {
            ContainerHolderSingleton.setContainerHolder(containerHolder);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_splashscreen);
    }
}
