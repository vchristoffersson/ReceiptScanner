package kandidat30.receiptscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextFragment extends Fragment{

    private OnFragmentInteractionListener mListener;
    private static final String ARG_PARAM1 = "dir";
    private String dir;

    private SwipeRefreshLayout refreshLayout;
    private CustomListAdapter adapter;
    private ListView listView;
    private TextView emptyView;
    private Button camviewButton;

    private List<String> images;
    private String path;

    public TextFragment() {

    }

    public interface FragmentChangeListener
    {
        void replaceFragment(Fragment fragment);
    }

    public static TextFragment newInstance(String param1) {
        TextFragment fragment = new TextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dir = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_text, container, false);

        images = getLatestImage();
        emptyView = (TextView)view.findViewById(R.id.empty);

  /*      if(images.isEmpty()) {
            showEmptyText();
        }*/

        camviewButton = (Button)view.findViewById(R.id.camViewButton);
        camviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager mPager = (ViewPager) getActivity().findViewById(R.id.pager);
                mPager.setCurrentItem(MainActivity.CAM_PAGE);
            }
        });

        final SwipeDetector swipeDetector = new SwipeDetector();

        adapter = new CustomListAdapter(getContext(), images, path);
        listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnTouchListener(swipeDetector);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String name = images.get(position);
                MainActivity.image = getBitmap(name);

                CustomViewPager mPager = (CustomViewPager) getActivity().findViewById(R.id.pager);
                mPager.setPagingEnabled(false);

                Fragment fr = new ImageFragment();
                Bundle args = new Bundle();
                args.putString("pos", name);
                fr.setArguments(args);
                FragmentChangeListener fc = (FragmentChangeListener) getActivity();
                fc.replaceFragment(fr);
            }
        });

        refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(images.isEmpty()) {
                    showEmptyText();
                }

                notifyAdapter();
                refreshLayout.setRefreshing(false);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listView == null || listView.getChildCount() == 0) ?
                                0 : listView.getChildAt(0).getTop();
                refreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
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

    private Bitmap getBitmap(String name) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String path = dir + File.separator + name;
        File file = new File(path);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        return bitmap;
    }

    private List<String> getLatestImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        List<String> files = new ArrayList<String>();// list of file paths

        File[] listFile;

        File file = new File(dir);

        path = dir + File.separator;

        if (file.isDirectory())
        {
            listFile = file.listFiles();

            for (int i = 0; i < listFile.length; i++)
            {
                String name = listFile[i].getName();

                files.add(name);

              //  Bitmap bitmap = BitmapFactory.decodeFile(listFile[i].getAbsolutePath(), options);

              //  MainActivity.imageMap.put(name, bitmap);
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

    public void hideEmptyText() {
        emptyView.setVisibility(View.INVISIBLE);
    }

    public void showEmptyText() {
        emptyView.setVisibility(View.VISIBLE);
    }

}
