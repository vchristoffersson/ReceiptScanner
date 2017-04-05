package kandidat30.receiptscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends Fragment {

    private static final String ARG_PARAM1 = "pos";

    private String name;
    private OnFragmentInteractionListener mListener;
    private TextView ocrText;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ImageFragment.
     */
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

        ocrText = (TextView)view.findViewById(R.id.ocrView);

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

            }
        });

        final Bitmap bitmap = MainActivity.imageMap.get(name);

        ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageBitmap(bitmap);

        Button ocrButton = (Button)view.findViewById(R.id.ocrButton);
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mCallback.onReceive(bitmap);
                new ReceiveTextTask().execute(bitmap);
                Toast.makeText(getContext(), "Image is being processed!", Toast.LENGTH_LONG).show();

            }
        });

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
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

            if(text != "") {
                Toast.makeText(getContext(), "OCR process completed!", Toast.LENGTH_SHORT).show();
                setOCRText(text);
            }

            else {
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

}
