package kandidat30.receiptscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private final List<String> images;
    private ImageView imageView;
    private String path;
    private Context context;

    private LayoutInflater mInflater;

    public CustomListAdapter(Context context, List<String> images, String path) {
        mInflater = LayoutInflater.from(context);
        this.images = images;
        this.path = path;
        this.context = context;
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
        nameText.setText(images.get(position));

        ImageButton deleteBtn = (ImageButton)convertView.findViewById(R.id.deleteSave);
        deleteBtn.setFocusable(false);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                handleDialog(position);
            }
        });

        return convertView;
    }

    private void removeFile(String name) {

        String absolute = path + "/" + name;

        File f = new File(absolute);
        f.delete();
    }

    private void handleDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        builder.setTitle("Are you sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = images.get(position);
                images.remove(position);
                MainActivity.image = null;
                removeFile(name);
                Database db = new Database(context);
                db.deleteSave(name);
                notifyDataSetChanged();

                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
