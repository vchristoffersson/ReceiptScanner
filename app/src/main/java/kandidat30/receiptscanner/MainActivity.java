package kandidat30.receiptscanner;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements FragmentCam.OnSendListener, TextFragment.FragmentChangeListener, ImageFragment.OnFragmentInteractionListener{

    private CustomViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private TextFragment textFragment;
    private FragmentCam cameraFragment;
    private File directory;

    public static LruCache<String, Bitmap> imageMap;

    private static final int NUM_PAGES = 2;
    public static final int TEXT_PAGE = 1;
    public static final int CAM_PAGE = 0;
    private static final String APP_DIRECTORY = "ReceiptScanner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int size = 70 * 1024 * 1024;
        imageMap = new LruCache<>(size);

        super.onCreate(savedInstanceState);

        createDir();

        setContentView(R.layout.activity_main);


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
                cameraFragment = new FragmentCam();
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
                imageMap.put(image.getName(), image.getImage());

                textFragment.addImage(image.getPath());
                textFragment.notifyAdapter();
                textFragment.hideEmptyText();

                Toast.makeText(getApplicationContext(), "Video process completed!", Toast.LENGTH_SHORT).show();
            }

            else {
                Toast.makeText(getApplicationContext(), "Video process failed!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {

        }
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();;
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

}
