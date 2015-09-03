package lsa.viewercloudpoints.filechooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import lsa.viewercloudpoints.R;

/**
 * Created by Luan Sala on 11/03/2015.
 */
public class MyArrayAdapter<T> extends ArrayAdapter<T> {
    private Context mContext;
    private int mResource;
    //private List<T> mObjects;


    public MyArrayAdapter(Context context, int resource, List<T> objects) {
        super(context,resource,objects);
        mContext  = context;
        mResource = resource;
        //mObjects  = objects;
    }

    public MyArrayAdapter(Context context, int resource, T[] objects) {
        super(context,resource,objects);
        mContext  = context;
        mResource = resource;
        //mObjects  = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(mResource, null);
        }

        final T object = getItem(position);
        ImageView image = (ImageView) view.findViewById(R.id.img1);
        TextView text1 = (TextView) view.findViewById(R.id.TextView01);
        //TextView text2 = (TextView) view.findViewById(R.id.TextView02); //Tirei o campo de infor-
        //macao pois por enquanto não precisa saber se é folder ou file, ja que tenho icone mostrando.

        if(object instanceof OptionFile) {
            if (((OptionFile) object).isDirectory()) {
                if( ((OptionFile) object).getInfo().equals("Shortcut") )
                    image.setImageResource(R.drawable.shortcut);
                else
                    image.setImageResource(R.drawable.folder);
            } else if (((OptionFile) object).getInfo().equals("..")) {
                image.setImageResource(R.drawable.parent_directory);
            } else image.setImageResource(R.drawable.file);
            //text1.setText( ((OptionFile) object).getName() );
            //text2.setText( ((OptionFile) object).getInfo() );
        }
        text1.setText( object.toString() );

        return view;
    }

}
