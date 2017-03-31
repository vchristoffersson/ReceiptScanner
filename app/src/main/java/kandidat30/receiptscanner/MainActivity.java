package kandidat30.receiptscanner;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements FragmentCam.OnSendListener{

    private CustomViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private Fragment cameraFragment;
    private Fragment textFragment;
    private FragmentCam cam;

    private static final int NUM_PAGES = 2;
    public static final int TEXT_PAGE = 1;
    public static final int CAM_PAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        Log.d("opencv", "oncreate");
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }
        Log.d("opencv", "after cv stuff");
        */
        super.onCreate(savedInstanceState);

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

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == CAM_PAGE) {
                cam = new FragmentCam();
                return cam;
            }
            else {
                textFragment = new TextFragment();
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
        new SendFilesTask().execute(new MediaPath(cam.getDirectory().getPath(), data));
    }

    private class SendFilesTask extends AsyncTask<MediaPath, Integer, Long> {
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected Long doInBackground(MediaPath... params) {
            long totalSize = 0;
            Send.sendVideo(params[0]);

            return totalSize;
        }

        @Override
        protected void onPostExecute(Long result) {

        }

        @Override
        protected void onPreExecute() {

        }
    }

}
