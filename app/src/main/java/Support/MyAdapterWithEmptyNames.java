package Support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import Models.Person;
import vinnik.facerecognizer.R;

/**
 * Created by vinnik on 02.02.2017.
 */

public class MyAdapterWithEmptyNames extends BaseAdapter {
    Context context;
    List<Person> data;
    LayoutInflater mInflater;

    public MyAdapterWithEmptyNames(Context context, List<Person> data) {
        this.context = context;
        this.data = data;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View root = convertView;
        if (root == null) {
            root = mInflater.inflate(R.layout.my_list_item, parent, false);
        }

        final TextView tv = (TextView) root.findViewById(R.id.best_face_owner);
        tv.setText(data.get(position).toString());

        final ImageView image = (ImageView) root.findViewById(R.id.best_face_image);
        image.setImageBitmap(data.get(position).Faces.where().findAll().first().getBitmap());
        return root;
    }
}
