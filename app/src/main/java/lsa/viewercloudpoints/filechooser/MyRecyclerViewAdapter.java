package lsa.viewercloudpoints.filechooser;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lsa.viewercloudpoints.R;

/**
 * Created by Luan Sala on 20/01/2016.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ItemViewHolder> {
    private static final String TAG = "MyRecyclerVAdapter";

    public MyRecyclerViewAdapter(int layout, List<OptionFile> list){
        if(list==null)
            dataset = new ArrayList<>();
        dataset = list;
        layoutResource = layout;
    }

    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(layoutResource, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        OptionFile optionFile = dataset.get(position);
        holder.getFirstLine().setText(optionFile.getName());

        if (optionFile.isDirectory()) {
            switch (optionFile.getInfo()) {
                case "Folder":
                    holder.getImageIconFile().setImageResource(R.drawable.folder);
                    break;
                case "Shortcut":
                    holder.getImageIconFile().setImageResource(R.drawable.shortcut);
                    break;
                case "..":
                    holder.getImageIconFile().setImageResource(R.drawable.parent_directory);
                    break;
            }
            holder.getSecondLine().setText("");
        } else {
            holder.getImageIconFile().setImageResource(R.drawable.file);
            holder.getSecondLine().setText(optionFile.getInfo());
        }
    }

    public int getItemCount() {
        return dataset.size();
    }

    public OptionFile getItem(int position){
        return dataset.get(position);
    }

    public void setOnItemClickListener(MyRecyclerViewOnItemClickListener myOnItemClickListener){
        this.myOnItemClickListener = myOnItemClickListener;
    }

    public void updateDataset(List<OptionFile> list){
        dataset = list;
    }

    protected final class ItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private static final String TAG = "ItemViewHolder";

        public ItemViewHolder(View itemView){
            super(itemView);
            imageIconFile = (ImageView) itemView.findViewById(R.id.iconFile);
            firstLine = (TextView) itemView.findViewById(R.id.first_line);
            secondLine = (TextView) itemView.findViewById(R.id.second_line);
            itemView.setOnClickListener(this);
        }

        public ImageView getImageIconFile(){
            return imageIconFile;
        }

        public TextView getFirstLine() {
            return firstLine;
        }

        public TextView getSecondLine() {
            return secondLine;
        }

        @Override
        public void onClick(View v) {
            if ( myOnItemClickListener!=null )
                myOnItemClickListener.onItemClick(v,getAdapterPosition());
        }

        private ImageView imageIconFile;
        private TextView firstLine;
        private TextView secondLine;
    }

    public interface MyRecyclerViewOnItemClickListener{
        void onItemClick(View view, int position);
    }

    //Conjunto de dados que será mostrado no RecyclerView. Os dados são os arquivo ou pastas presentes
    // nos diretórios de armazenamento do dispositivo.
    private List<OptionFile> dataset;

    private MyRecyclerViewOnItemClickListener myOnItemClickListener;

    private int layoutResource;
}
