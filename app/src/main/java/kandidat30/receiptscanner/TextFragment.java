package kandidat30.receiptscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextFragment extends Fragment{

    private OnFragmentInteractionListener mListener;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private SwipeRefreshLayout refreshLayout;
    private CustomListAdapter adapter;
    private ListView textView;
    private List<String> images;

    public TextFragment() {

    }

    public interface FragmentChangeListener
    {
        void replaceFragment(Fragment fragment);
    }

    public static TextFragment newInstance(String param1, String param2) {
        TextFragment fragment = new TextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_text, container, false);
      //  ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
       // imageView.setImageBitmap(getLatestImage());

        images = getLatestImage();

        adapter = new CustomListAdapter(getContext(), images);
        textView = (ListView)view.findViewById(R.id.textView);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CustomViewPager mPager = (CustomViewPager) getActivity().findViewById(R.id.pager);
                mPager.setPagingEnabled(false);

                Fragment fr = new ImageFragment();
                Bundle args = new Bundle();
                args.putInt("pos", position);
                fr.setArguments(args);
                FragmentChangeListener fc=(FragmentChangeListener)getActivity();
                fc.replaceFragment(fr);
            }
        });

        refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                notifyAdapter();
                refreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        CustomViewPager mPager = (CustomViewPager) getActivity().findViewById(R.id.pager);
        mPager.setPagingEnabled(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private List<String> getLatestImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        List<String> files = new ArrayList<String>();// list of file paths

        File[] listFile;

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + R.string.directory);

        if (file.isDirectory())
        {
            listFile = file.listFiles();

            for (int i = 0; i < listFile.length; i++)
            {
                files.add(listFile[i].getName());

                Bitmap bitmap = BitmapFactory.decodeFile(listFile[i].getAbsolutePath(), options);

                MainActivity.imageList.add(bitmap);
            }
        }

        return files;
    }

    public void notifyAdapter() {
        adapter.notifyDataSetChanged();
    }

    public void addImage(String name) {
        images.add(name);
    }

}
