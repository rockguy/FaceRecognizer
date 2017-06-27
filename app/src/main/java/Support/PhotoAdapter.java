package Support;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import Models.PhotoDetail;
import vinnik.facerecognizer.R;

/**
 * Created by vinnik on 18.04.2017.
 */

public class PhotoAdapter extends BaseAdapter {

    List<PhotoDetail> photoDetails;
    LayoutInflater mInflater;

    public PhotoAdapter(Context context, List<PhotoDetail> photoDetails) {
        this.photoDetails = photoDetails;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return photoDetails.size();
    }

    @Override
    public Object getItem(int i) {
        return photoDetails.get(i);
    }

    @Override
    public long getItemId(int i) {
        return photoDetails.get(i).Id;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View root = view;
        if (root == null) {
            root = mInflater.inflate(R.layout.my_photo_item, parent, false);
        }

        TextView tv = (android.widget.TextView) root.findViewById(R.id.image_text_item);
        tv.setText(Integer.toString(i + 1));

        final ImageView image = (ImageView) root.findViewById(R.id.image_item);
        image.setImageBitmap(BitmapFactory.decodeFile(photoDetails.get(i).filePath));
        return root;
    }
}
