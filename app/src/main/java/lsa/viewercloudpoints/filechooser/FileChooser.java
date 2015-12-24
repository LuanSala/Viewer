package lsa.viewercloudpoints.filechooser;

import lsa.viewercloudpoints.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Luan Sala on 05/03/2015.
 */
public class FileChooser extends ActionBarActivity
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener{
    private static final String TAG = "FileChooser";

    //STATES para a recuperacao dos dados no ciclo de vida do FileChooser.
    //Usado quando a orientacao da tela muda.
    private static final String STATE_CURRENT_DIRECTORY = "currentDirectory";
    private static final String STATE_FILE_SELECTED = "fileSelected";

    private String fileSelected;
    private boolean isFileSelected;
    private File currentDirectory;
    private ListView listView;
    /**
     * Botao que volta uma pasta ou fecha o FileChooser
     */
    private Button button1;
    /**
     * Botao que escolhe o arquivo selecionado
     */
    private Button button2;
    private MyArrayAdapter<OptionFile> arrayAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null) {
            currentDirectory = new File(savedInstanceState.getString(STATE_CURRENT_DIRECTORY, File.separator));
            String file = savedInstanceState.getString(STATE_FILE_SELECTED);
            if(file!=null){
                isFileSelected = true;
                fileSelected = file;
            }
        }else
            currentDirectory = new File(File.separator);
        setContentView(R.layout.file_chooser);
        listView = (ListView) findViewById(R.id.listView);
        button1 = (Button) findViewById(R.id.button1);
        fillListView(currentDirectory);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                File nextDir;
                if( arrayAdapter.getItem(position).getInfo().equals("..") )
                    nextDir = currentDirectory.getParentFile();
                else
                    nextDir = new File(currentDirectory, arrayAdapter.getItem(position).getName());
                if (nextDir.isDirectory()) {
                    currentDirectory = nextDir;
                    isFileSelected = false;
                    try {
                        fillListView(currentDirectory.getCanonicalFile());
                    }catch(IOException e){ e.printStackTrace(); }
                } else {
                    fileSelected = nextDir.getAbsolutePath();
                    try {
                        FileInputStream file = new FileInputStream(fileSelected);
                        byte[] numberMagicBytes = new byte[4];
                        String numberMagic;
                        file.read(numberMagicBytes,0,4);
                        numberMagic = new String(numberMagicBytes);
                        file.close();

                        if(numberMagic.equals("PCl1") || numberMagic.equals("PCl2")) {
                            isFileSelected = true;
                            //Nome do arquivo passado como parâmetro.
                            buildOpenFileDialog( arrayAdapter.getItem(position).getName() );
                        } else
                            Toast.makeText(getApplicationContext(),getString(R.string.message_open_file_not_valid),Toast.LENGTH_LONG).show();
                    } catch(IOException e){
                        Toast.makeText(getApplicationContext(),getString(R.string.message_open_file_not_valid),Toast.LENGTH_LONG).show();
                        //e.printStackTrace();
                    }
                }
            }
        });

        button1.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !currentDirectory.getAbsolutePath().equals(File.separator) ) {
                    isFileSelected = false;
                    currentDirectory = currentDirectory.getParentFile();
                    fillListView(currentDirectory);
                } else {
                    setResult(ActionBarActivity.RESULT_CANCELED);
                    finish();
                }
            }
        });
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
            Intent intent = new Intent();
            intent.putExtra("fileSelected", fileSelected);
            setResult(ActionBarActivity.RESULT_OK, intent);
            finish();
        }else if( which==DialogInterface.BUTTON_NEGATIVE)
            isFileSelected = false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isFileSelected = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CURRENT_DIRECTORY,currentDirectory.getAbsolutePath());
        if(isFileSelected)
            outState.putString(STATE_FILE_SELECTED,fileSelected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFileSelected)
            buildOpenFileDialog(new File(fileSelected).getName());
    }

    // Aqui você preenche a ActionBar colocando os botões desejados.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_viewer,menu);
        return super.onCreateOptionsMenu(menu);
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
                } else
                    files.add( new OptionFile(f.getName(),"File",false) );
            }
            Collections.sort( dirs );
            Collections.sort( files );
            list.addAll(dirs);
            list.addAll(files);
        }
        if( !file.getAbsolutePath().equals(File.separator) ) {
            //list.add(0, "..Parent Directory..");
            list.add( 0, new OptionFile("Parent Directory","..",false) );
            button1.setText("BACK");
        } else
            button1.setText("CANCEL");
        //arrayAdapter = new ArrayAdapter<>(FileChooser.this, android.R.layout.simple_list_item_1, list);
        arrayAdapter = new MyArrayAdapter<OptionFile>(FileChooser.this, R.layout.file_view, list);
        listView.setAdapter(arrayAdapter);
    }

}
