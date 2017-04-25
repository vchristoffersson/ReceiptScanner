package kandidat30.receiptscanner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements CameraFragment.OnSendListener,
        TextFragment.FragmentChangeListener, ImageFragment.OnFragmentInteractionListener, CameraFragment.OnHDRListener, CameraFragment.FragmentChangeListener{

    private CustomViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private TextFragment textFragment;
    private CameraFragment cameraFragment;
    private ProgressBar progressBar;

    private Handler progressHandler;

    private File directory;
    public static Bitmap image;
    private List<String> hdrAlgorithms;

    private static final int REQUEST_PERMISSIONS = 100;
    private static final int NUM_PAGES = 2;
    public static final int TEXT_PAGE = 1;
    public static final int CAM_PAGE = 0;
    private static final String APP_DIRECTORY = "ReceiptScanner";

    private final String DEBEVEC = "debevec";
    private final String MERTENS = "mertens";
    private final String ROBERTSON = "robertson";

    public static String token;
    public static String message;
    public static String log;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("log"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        progressBar = (ProgressBar)findViewById(R.id.scanBar);
        progressBar.setScaleY(4f);
        progressHandler = new Handler();

        token = FirebaseInstanceId.getInstance().getToken();
        db = new Database(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.requestPermissions(new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
    }

    private void initiateApp(){
        createDir();

        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == CAM_PAGE) {
                cameraFragment = new CameraFragment();
                Bundle args = new Bundle();
                args.putString("dir", directory.getPath());
                cameraFragment.setArguments(args);
                return cameraFragment;
            }
            else {
                textFragment = new TextFragment();
                Bundle args = new Bundle();
                args.putString("dir", directory.getPath());
                textFragment.setArguments(args);
                return textFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onSend(byte[] data) {
        new SendFilesTask().execute(new MediaPath(directory.getPath(), data, ""));
        Toast.makeText(this, "Video is being processed!", Toast.LENGTH_LONG).show();
    }

    private class SendFilesTask extends AsyncTask<MediaPath, Integer, Long> {

        private MediaPath image;

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Long doInBackground(MediaPath... params) {
            long totalSize = 0;
            image = Send.sendVideo(params[0]);

            return totalSize;
        }

        @Override
        protected void onPostExecute(Long result) {

            if(image != null) {
                textFragment.addImage(image.getPath());
                textFragment.notifyAdapter();
                textFragment.hideEmptyText();

                if(message != "") {
                    saveLog(image.getName(), message);
                    message = "";
                }

                Toast.makeText(getApplicationContext(), "Video process completed!", Toast.LENGTH_SHORT).show();
            }

            else {
                Toast.makeText(getApplicationContext(), "Video process failed!", Toast.LENGTH_SHORT).show();
            }

            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {
            progressHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }



    private void loadHDRSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean debevec = prefs.getBoolean("debevec", true);
        boolean robertson = prefs.getBoolean("robertson", true);

        hdrAlgorithms = new ArrayList<>();
        hdrAlgorithms.add(MERTENS);

        if(debevec) {
            hdrAlgorithms.add(DEBEVEC);
        }
        if(robertson) {
            hdrAlgorithms.add(ROBERTSON);
        }
    }

    @Override
    public void onHDRSend(List<Byte[]> data) {
        loadHDRSettings();

        new SendHDRTask().execute(data);
        Toast.makeText(this, "HDR is being processed!", Toast.LENGTH_LONG).show();
    }

    private class SendHDRTask extends AsyncTask<List<Byte[]>, Integer, Long> {

        private List<String> hdrNames;

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Long doInBackground(List<Byte[]>... params) {
            long totalSize = 0;
            String name = new SimpleDateFormat("yyyyMMdd__HHmmss_SSS").format(new Date());
            hdrNames = Send.sendImage(directory, name, params[0], hdrAlgorithms);

            return totalSize;
        }

        @Override
        protected void onPostExecute(Long result) {
            for(String s : hdrNames) {
                if(s != "")
                    textFragment.addImage(s);
            }

            textFragment.notifyAdapter();
            textFragment.hideEmptyText();

            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "HDR process completed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            progressHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }



    @Override
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }

    private void createDir() {
        directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + APP_DIRECTORY);
        if(!directory.exists() && !directory.isDirectory()) {
            directory.mkdirs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        Log.d("PERMISSION", "checkin if permission was granted");

        Map<String, Integer> permissionsMap = new HashMap<>();
        permissionsMap.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
        permissionsMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

        for (int i = 0; i < permissions.length; i++)
            permissionsMap.put(permissions[i], grantResults[i]);

        if (permissionsMap.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && permissionsMap.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Log.d("PERMISSION", "All permissions was granted");
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            initiateApp();
        } else {
            Log.d("PERMISSION", "All permissions was not granted");
            finishAffinity();
        }
    }

    private void saveLog(String name, String log) {
        boolean b = db.insertData(name, log);
        Log.d("booldata", b + " name:" + name);
        Cursor c = db.getAllData();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            log += message;
            MainActivity.message += message;
            cameraFragment.updateLogText(log);
        }
    };

    public static void clearLog() {
        log = "";
    }
}
