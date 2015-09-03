package lsa.viewercloudpoints.filechooser;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by Luan Sala on 03/04/2015.
 */
public class MyListView extends ListView {

    public MyListView(Context context){
        super(context);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event){
        return super.onGenericMotionEvent(event);
    }
}
