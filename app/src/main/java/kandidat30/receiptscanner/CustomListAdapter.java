package kandidat30.receiptscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class CustomListAdapter extends BaseAdapter {
    private final List<String> images;
    private ProgressBar loader;
    private ImageView imageView;

    private LayoutInflater mInflater;

    public CustomListAdapter(Context context, List<String> images) {
        mInflater = LayoutInflater.from(context);
        this.images = images;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return images.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return images.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.rowlayout, null);
        String key = images.get(position);

        imageView = (ImageView) convertView.findViewById(R.id.icon);
        TextView nameText = (TextView) convertView.findViewById(R.id.nameText);
        TextView dateText = (TextView) convertView.findViewById(R.id.dateText);
        loader = (ProgressBar) convertView.findViewById(R.id.loader);

        nameText.setText(images.get(position));
        dateText.setText("Position " + (position + 1));

        return convertView;
    }

}
