package lsa.viewercloudpoints;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceFragment;
import android.util.SparseArray;

import java.lang.reflect.InvocationTargetException;


/**
 * Created by Luan Sala on 25/02/2015.
 */
public class Global {
    private static final String TAG = "Global";

    /**
     * Codigo de requisicao usado para identificar o resultado da atividade FileChooser
     * via Intent.
     * @see lsa.viewercloudpoints.filechooser.FileChooser
     * @see android.content.Intent
     */
    public static final int PICK_FILE = 1;
    /**
     * Estado do programa.
     * Estado em que o programa se encontra ao inicia-lo.
     */
    public static final byte STATE_INIT_PROGRAM  = 0;
    /**
     * Estado do programa.
     * Estado em que o programa se encontra, normalmente apos o estado
     *  {@link #STATE_INIT_PROGRAM}.
     */
    public static final byte STATE_RENDER_POINTS = 1;

    //Nomes simbolicos de todos os estados do programa.
    private static final SparseArray<String> STATE_SYMBOLIC_NAMES = new SparseArray<>();
    static {
        SparseArray<String> names = STATE_SYMBOLIC_NAMES;
        names.append(STATE_INIT_PROGRAM, "STATE_INIT_PROGRAM");
        names.append(STATE_RENDER_POINTS, "STATE_RENDER_POINTS");
    }

    public static final byte VIEW_USING_CAMERA    = 0;

    public static final byte VIEW_USING_TRACKBALL = 1;

    //Nomes simbolicos de todos os tipos de visualização do programa.
    private static final SparseArray<String> VIEW_SYMBOLIC_NAMES = new SparseArray<>();
    static {
        SparseArray<String> names = VIEW_SYMBOLIC_NAMES;
        names.append(VIEW_USING_CAMERA, "VIEW_USING_CAMERA");
        names.append(VIEW_USING_TRACKBALL, "VIEW_USING_TRACKBALL");
    }

    public Global(MyGLSurfaceView glSurfaceView,Activity activity){
        Global.activity = activity;
        mHelper = new Helper(glSurfaceView);
        mHelper.start();
    }

    private static Activity getActivity(){
        return activity;
    }

    /**
     * Retorna o estado atual em que o programa se encontra.
     * @return O estado atual, como {@link #STATE_INIT_PROGRAM}.
     */
    public static byte getStateProgram(){
        return stateProgram;
    }

    /**
     * Configura o estado em que o programa atualmente se encontra. Caso o state entrado nao seja
     * valido, o valor padrao {@link #STATE_INIT_PROGRAM} sera atribuido.
     *
     * @param state O estado em que o programa esta entrando, como {@link #STATE_INIT_PROGRAM}.
     *
     * @see #STATE_INIT_PROGRAM
     * @see #STATE_RENDER_POINTS
     */
    public static void setStateProgram(byte state) {
        if( STATE_SYMBOLIC_NAMES.get(state)!=null )
            stateProgram = state;
        else
            stateProgram = STATE_INIT_PROGRAM;
    }

    public static byte getViewingStyle(){
        return viewingStyle;
    }

    public static void setViewingStyle(byte vStyle){
        if( VIEW_SYMBOLIC_NAMES.get(vStyle)!=null ) viewingStyle = vStyle;
        else viewingStyle = VIEW_USING_TRACKBALL;
    }

    /**
     * Retorna o contexto do programa.
     * @return o contexto.
     */
    public static Context getContext(){
        return context;
    }

    /**
     * Configura o atual contexto de aplicacao em que o programa esta.
     * Configurado para acessar determinadas funcoes de qualquer classe que se esteja.
     *
     * @param cont O contexto da aplicacao. Pode ser enviado pela funcao 'getApplicationContext()'.
     */
    public static void setContext(Context cont){
        context = cont;
    }

    public static float getProportionateHeight(float width){
        float ratio = ((float)SCREEN_WIDTH)/((float)SCREEN_HEIGHT);
        return (ratio * width);
    }

    public static boolean requestedCentralizeTrackball(){
        boolean ret = true;
        if (centerTrackball)
            centerTrackball = false;
        else ret = false;
        return ret;
    }

    public static void centralizeTrackball(){
        centerTrackball = true;
    }

    public static boolean clickedInButton(Texture button,float clickX,float clickY){
        float scale      = 1.0f;
        float translateX = 0.0f;
        float translateY = 0.0f;
        try {
            try {
                scale      = (Float) button.getClass().getMethod("getScale", (Class[]) null).invoke(button, (Object[]) null);
                translateX = (Float) button.getClass().getMethod("getTranslateX", (Class[]) null).invoke(button, (Object[]) null);
                translateY = (Float) button.getClass().getMethod("getTranslateY", (Class[]) null).invoke(button, (Object[]) null);
            }catch(InvocationTargetException | IllegalAccessException e){
                e.printStackTrace();
            }
        }catch(NoSuchMethodException e){
            e.printStackTrace();
        }
        float sX = scale*Global.SCREEN_WIDTH;
        boolean ret = false;
        if( (clickX >= sX*translateX) && (clickX <= sX*(translateX+1)) ){
            if( (clickY <= Global.SCREEN_HEIGHT-sX*translateY) && (clickY >= Global.SCREEN_HEIGHT-sX*(translateY+1)) )
                ret = true;
        }

        return ret;
    }

    public void destroy(){
        mHelper.destroyHelper();
        centerTrackball = false;
    }

    //Classe interna que ajuda na
    private static class Helper extends Thread{
        private static final String TAG = "GlobalHelper";
        public Helper(MyGLSurfaceView glSurfaceView){
            this.glSurfaceView = glSurfaceView;
            if(glSurfaceView!=null) mHasGLSurface = true;
            navigationDrawer = (PreferenceFragment)getActivity().getFragmentManager().
                    findFragmentById(R.id.id_nav_drawer_fragment);
        }

        @Override
        public void run(){
            if(mHasGLSurface) {
                while (true) {
                    synchronized (navigationDrawer) {
                        try {
                            navigationDrawer.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (mLock) {
                            if (mDone) break;
                        }
                        glSurfaceView.getRenderer().refreshMVP();
                        glSurfaceView.requestRender();
                    }
                }
            }
        }

        public void destroyHelper(){
            synchronized (mLock){
                mDone = true;
                mLock.notify();
            }
        }

        private final PreferenceFragment navigationDrawer;

        private MyGLSurfaceView glSurfaceView;
        private boolean mHasGLSurface;
        private boolean mDone = false;
        private final Object mLock = new Object();
    }

    public static String file;

    /**
     * Tamanho da tela do aplicativo na horizontal.
     */
    public static short SCREEN_WIDTH;
    /**
     * Tamanho da tela do aplicativo na vertical.
     */
    public static short SCREEN_HEIGHT;

    public static int OPEN_FILE = R.drawable.ic_action_new;

    private static Context context;
    private static Activity activity;

    private static byte stateProgram;

    /**
     * Estilo de visualização sendo utilizado no momento pelo aplicativo.
     */
    private static byte viewingStyle;

    private static boolean createdTexture = false;

    //Variável que dira se a nuvem visualizada em modo Trackball, será visualizada
    // com os movimentos de rotação no centro dela.
    private static boolean centerTrackball = false;

    private Helper mHelper;

}
