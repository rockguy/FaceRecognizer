package Support;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import Models.NamedPhoto;
import vinnik.facerecognizer.R;

/**
 * Created by vinnik on 06.03.2017.
 */

public class MyListOfPersonAdapter extends BaseAdapter {
    Context context;
    LayoutInflater mInflater;
    List<NamedPhoto> namedPhotos;

    public MyListOfPersonAdapter(Context context, List<NamedPhoto> namedPhotos) {
        this.context = context;
        this.namedPhotos = namedPhotos;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return namedPhotos.size();
    }

    @Override
    public Object getItem(int position) {
        return namedPhotos.get(position);
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
        tv.setText(namedPhotos.get(position).name);

        final ImageView image = (ImageView) root.findViewById(R.id.best_face_image);
        image.setImageBitmap(Bitmap.createScaledBitmap(namedPhotos.get(position).bitmap, 120, 120, false));
        return root;
    }
}
