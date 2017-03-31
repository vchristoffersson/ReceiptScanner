package kandidat30.receiptscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CustomListAdapter extends BaseAdapter {
    private final List<String> images;
    private ImageView imageView;
    private String path;

    private LayoutInflater mInflater;

    public CustomListAdapter(Context context, List<String> images, String path) {
        mInflater = LayoutInflater.from(context);
        this.images = images;
        this.path = path;
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


    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.rowlayout, null);
        String key = images.get(position);

        imageView = (ImageView) convertView.findViewById(R.id.icon);
        TextView nameText = (TextView) convertView.findViewById(R.id.nameText);
        TextView dateText = (TextView) convertView.findViewById(R.id.dateText);

        nameText.setText(images.get(position));
        dateText.setText("Position " + (position + 1));

        ImageButton deleteBtn = (ImageButton)convertView.findViewById(R.id.deleteSave);
        deleteBtn.setFocusable(false);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = images.get(position);
                images.remove(position);
                MainActivity.imageList.remove(position);
                removeFile(name);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    private void removeFile(String name) {

        String absolute = path + "/" + name;

        File f = new File(absolute);
        f.delete();
    }

}
