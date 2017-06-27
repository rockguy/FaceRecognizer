package Support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
 * Created by vinnik on 06.03.2017.
 */

public class MyListOfPersonAdapter extends BaseAdapter {
    Context context;
    LayoutInflater mInflater;
    List<PhotoDetail> namedPhotos;

    public MyListOfPersonAdapter(Context context, List<PhotoDetail> namedPhotos) {
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
        tv.setText(namedPhotos.get(position).LongName);

        final ImageView image = (ImageView) root.findViewById(R.id.best_face_image);

        image.setImageBitmap(getCroppedBitmap(Bitmap.createScaledBitmap(namedPhotos.get(position).bitmap, 120, 120, false)));
        return root;
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
