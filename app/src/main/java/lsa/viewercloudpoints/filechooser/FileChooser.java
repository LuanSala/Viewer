package lsa.viewercloudpoints.filechooser;

import lsa.viewercloudpoints.Global;
import lsa.viewercloudpoints.R;
import lsa.viewercloudpoints.Viewer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Luan Sala on 05/03/2015.
 */
public class FileChooser extends AppCompatActivity
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener, MyRecyclerViewAdapter.MyRecyclerViewOnItemClickListener {
    private static final String TAG = "FileChooser";

    //STATES para a recuperacao dos dados no ciclo de vida do FileChooser.
    //Usado quando a orientacao da tela muda.
    private static final String STATE_CURRENT_DIRECTORY = "currentDirectory";
    private static final String STATE_FILE_SELECTED = "fileSelected";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_file_chooser);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_file_chooser));
        toolbar = getSupportActionBar();

        if(savedInstanceState!=null) {
            currentDirectory = new File(savedInstanceState.getString(STATE_CURRENT_DIRECTORY, File.separator));
            String file = savedInstanceState.getString(STATE_FILE_SELECTED);
            if (file != null) {
                isFileSelected = true;
                fileSelected = file;
                buildOpenFileDialog(new File(fileSelected).getName());
            }
            toolbar.setTitle(File.separator + currentDirectory.getName());
        } else
            currentDirectory = new File(File.separator);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_list_file);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fillListView(currentDirectory);

        boolean[] bool = getIntent().getBooleanArrayExtra(getString(R.string.isActivityForResult));
        if(bool!=null)
            isActivityForResult = bool[0];

        toast = Toast.makeText(getApplicationContext(), getString(R.string.message_open_file_not_valid), Toast.LENGTH_SHORT);
    }

    public void buildOpenFileDialog(String fileName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String string = getString(R.string.message_dialog_open_file);

        //Pega o nome do arquivo escolhido e põe no meio da mensagem a ser mostrada para o usuário.
        builder.setMessage(string + " \"" + fileName + "\"?");
        builder.setTitle(R.string.title_dialog_open_file);
        //Configura a ação a ser tomada caso o diálogo seja fechado por qualquer motivo.
        builder.setOnDismissListener(this);
        //Configura a ação do botão positivo ao ser apertado.
        builder.setPositiveButton(R.string.positiveButton_dialog_open_file, this);
        //Configura a ação do botão negativo ao ser apertado.
        builder.setNegativeButton(R.string.negativeButton_dialog_open_file, this);
        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which==DialogInterface.BUTTON_POSITIVE) {
            //Fecha o File Chooser com o arquivo escolhido sendo mandado para o Viewer
            // para ser visualizado a nuvem de pontos.
            if( isActivityForResult ) {
                Intent intent = new Intent();
                intent.putExtra("fileSelected", fileSelected);
                setResult(AppCompatActivity.RESULT_OK,intent);
                finish();
            } else {
                Global.file = fileSelected;
                startActivity(new Intent(this, Viewer.class));
            }
        } else if( which==DialogInterface.BUTTON_NEGATIVE)
            isFileSelected = false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isFileSelected = false;
    }

    @Override
    public void onBackPressed() {
        if( !currentDirectory.getAbsolutePath().equals(File.separator) ) {
            isFileSelected = false;
            currentDirectory = currentDirectory.getParentFile();
            toolbar.setTitle(File.separator + currentDirectory.getName());
            fillListView(currentDirectory);
        } else {
            setResult(AppCompatActivity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CURRENT_DIRECTORY, currentDirectory.getAbsolutePath());
        if(isFileSelected)
            outState.putString(STATE_FILE_SELECTED, fileSelected);
    }

    @Override
    public void onItemClick(View view, int position) {
        File nextDir;
        OptionFile fileClicked = myRecyclerViewAdapter.getItem(position);
        if (fileClicked.getInfo().equals(".."))
            nextDir = currentDirectory.getParentFile();
        else
            nextDir = new File(currentDirectory, fileClicked.getName());
        if (nextDir.isDirectory()) {
            currentDirectory = nextDir;
            isFileSelected = false;
            toolbar.setTitle(File.separator+currentDirectory.getName());
            try {
                fillListView(currentDirectory.getCanonicalFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fileSelected = nextDir.getAbsolutePath();
            try {
                FileInputStream file = new FileInputStream(fileSelected);
                byte[] numberMagicBytes = new byte[4];
                String numberMagic;
                file.read(numberMagicBytes, 0, 4);
                numberMagic = new String(numberMagicBytes);
                file.close();

                if (numberMagic.equals("PCl1") || numberMagic.equals("PCl2")) {
                    isFileSelected = true;
                    //Nome do arquivo passado como parâmetro.
                    buildOpenFileDialog(fileClicked.getName());
                } else
                    toast.show();
            } catch (IOException e) {
                toast.show();
            }
        }
    }

    private void fillListView(File file){
        List<OptionFile> list = new ArrayList<>();
        File[] listFiles = file.listFiles();
        if( listFiles!=null ) {
            List<OptionFile> dirs  = new ArrayList<>();
            List<OptionFile> files = new ArrayList<>();
            for(File f : listFiles) {
                if( f.isDirectory() ) {
                    try {
                        if ( f.getAbsolutePath().equals(f.getCanonicalPath()) )
                            dirs.add(new OptionFile(f.getName(), "Folder", true));
                        else
                            dirs.add(new OptionFile(f.getName(), "Shortcut", true));
                    }catch(IOException e){ e.printStackTrace(); }
                } else {
                    float info = f.length();
                    String strInfo;
                    String unit;
                    if (info>1024f){
                        info/=1024f;
                        if(info>1024f){
                            info/=1024f;
                            unit = " MiB";
                        } else
                            unit = " KiB";
                    } else
                        unit = " B";
                    strInfo = String.valueOf(info);
                    if ((strInfo.length()-strInfo.indexOf("."))>3 )
                        strInfo = strInfo.substring(0,strInfo.indexOf(".")+3);
                    files.add(new OptionFile(f.getName(), strInfo+unit, false));
                }
            }
            Collections.sort( dirs );
            Collections.sort( files );
            list.addAll(dirs);
            list.addAll(files);
        }
        if( !file.getAbsolutePath().equals(File.separator) )
            list.add( 0, new OptionFile("Parent Directory","..",true) );
        if (myRecyclerViewAdapter==null) {
            myRecyclerViewAdapter = new MyRecyclerViewAdapter(R.layout.recyclerview_file_chooser_item, list);
            myRecyclerViewAdapter.setOnItemClickListener(this);
        } else
            myRecyclerViewAdapter.updateDataset(list);
        recyclerView.setAdapter(myRecyclerViewAdapter);

    }

    private String fileSelected;
    private File currentDirectory;
    private boolean isFileSelected;

    private Toast toast;
    private ActionBar toolbar;
    private RecyclerView recyclerView;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;

    private boolean isActivityForResult = false;

}
