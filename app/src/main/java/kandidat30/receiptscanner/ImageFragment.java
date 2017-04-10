package kandidat30.receiptscanner;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class ImageFragment extends Fragment{

    private static final String ARG_PARAM1 = "pos";

    private String name;
    private OnFragmentInteractionListener mListener;

    private TextView ocrText;
    private TextView swipeText;
    private ProgressBar progressBar;

    private Bitmap bitmap;
    private boolean isOCR = false;

    public ImageFragment() {
    }

    public static ImageFragment newInstance(String param1) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
       // progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                return gesture.onTouchEvent(event);
            }
        });

        ocrText = (TextView)view.findViewById(R.id.ocrView);
        swipeText = (TextView)view.findViewById(R.id.slideText);

        TabHost tabHost = (TabHost)view.findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.imageTab);
        tabSpec.setIndicator("image");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.textTab);
        tabSpec.setIndicator("ocr");
        tabHost.addTab(tabSpec);

        setTabColor(tabHost);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabId.equals("tag1")) {
                    isOCR = false;
                }
                else if(tabId.equals("tag2")) {
                    isOCR = true;
                }
            }
        });

        bitmap = MainActivity.image;

        ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageBitmap(bitmap);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        CustomViewPager mPager = (CustomViewPager) getActivity().findViewById(R.id.pager);
        mPager.setPagingEnabled(true);
        getActivity().getSupportFragmentManager().popBackStack();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setTabColor(TabHost tabHost) {
        for(int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.WHITE);
        }
    }

    private class ReceiveTextTask extends AsyncTask<Bitmap, Integer, Long> {

        private String text;

        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected Long doInBackground(Bitmap... params) {
            long totalSize = 0;
            text = Send.getReceiptData(params[0]);

            return totalSize;
        }

        @Override
        protected void onPostExecute(Long result) {
            progressBar.setVisibility(View.INVISIBLE);

            if(text != "" && text != "!") {
                Toast.makeText(getContext(), "OCR process completed!", Toast.LENGTH_SHORT).show();
                setOCRText(text);
            }

            else {
                swipeText.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "OCR process failed!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {

        }
    }

    private void setOCRText(String text) {
        ocrText.setText(text);
    }

    private final GestureDetector gesture = new GestureDetector(getActivity(),
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                    final int SWIPE_MIN_DISTANCE = 220;
                    final int SWIPE_MAX_OFF_PATH = 250;
                    final int SWIPE_THRESHOLD_VELOCITY = 600;

                    try {
                        if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                            return false;

                        if (e1.getY() - e2.getY() < SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY && isOCR) {
                            ocrText.setText("");
                            swipeText.setVisibility(View.INVISIBLE);
                            progressBar.setVisibility(View.VISIBLE);

                            new ReceiveTextTask().execute(bitmap);
                            Toast.makeText(getContext(), "Image is being processed!", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });
}
