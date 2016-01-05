package lsa.viewercloudpoints;

/**
 * Created by Luan Sala on 09/02/2015.
 */

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MotionEvent;

import java.util.ArrayList;

public class MyGLSurfaceView extends GLSurfaceView
    implements Preference.OnPreferenceChangeListener{
    private static final String TAG = "MyGLSurfaceView";

    private final MyGLRenderer mRenderer;
    private final DetectMultiTouch gestures;

    private float speedMultiTouch;

    private ArrayList<Integer> pointerID = new ArrayList<>();
    private VectorFloat mPreviousPointerPos = new VectorFloat(2);

    public MyGLSurfaceView(Context context){
        super(context);
        Global.setContext(context);

        //Cria contexto de uso do OpenGL ES 2.0
        setEGLContextClientVersion( 2 );

        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        gestures = new DetectMultiTouch();
    }

    public MyGLSurfaceView(Context context, Bundle savedInstanceState){
        super(context);
        Global.setContext(context);

        //Cria contexto de uso do OpenGL ES 2.0
        setEGLContextClientVersion( 2 );

        mRenderer = new MyGLRenderer(savedInstanceState);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        gestures = new DetectMultiTouch();
    }

    public Renderer getRenderer(){
        return mRenderer;
    }

    /**
     * Configura a velocidade de controle dos gestos multitouch. De acordo com
     * o valor passado, configura a velocidade de controle. Deve ser passado um valor que
     * seja > 0 e <= 100.
     * @param speed Velocidade que será configurado a variavel de controle.
     */
    public void setSpeedMultiTouchInitial(int speed){
        if(speed<0) speed = 1;
        speedMultiTouch = calculateSpeedMultiTouch(speed);
    }

    // A formula se baseia nos valores extremos e meio de speed...
    // O valor mínimo de speed é 1. Quando speed = 1, valor retornado é 0.0001.
    // Quando está na "metade", speed = 50, retorna 0.75.
    // Quando é o valor máximo, speed = 100, retorna 3.
    private float calculateSpeedMultiTouch(int speed){
        return (speed*speed*0.0003f + 0.000006f*speed - 0.0002f);
    }

    /**
     * Método invocado quando ocorre algum evento de toque na tela do aparelho.
     *
     * @param event O evento que ocorreu.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getPointerCount()==1) {
            final float actualX = event.getX();
            final float actualY = event.getY();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (Global.getStateProgram() == Global.STATE_INIT_PROGRAM) {
                        //if (Global.clickedInButton(mRenderer.fileButton, actualX, actualY)) {
                        Global.setStateProgram(Global.STATE_RENDER_POINTS);
                        requestRender();
                        //}
                    }
                    pointerID.add(event.getPointerId(event.getActionIndex()));
                    mPreviousPointerPos.set(actualX, actualY);
                    mRenderer.getVirtualTrackball().pointerDown(actualX, actualY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Global.getStateProgram() == Global.STATE_RENDER_POINTS) {
                        float dx = actualX - mPreviousPointerPos.getX();
                        float dy = actualY - mPreviousPointerPos.getY();
                        if (Global.getViewingStyle() == Global.VIEW_USING_TRACKBALL)
                            mRenderer.getVirtualTrackball().pointerMove(actualX, actualY);
                        else
                            mRenderer.getCamera().rotate(-dy, -dx, 0);
                        mRenderer.refreshMVP();
                        requestRender();
                        mPreviousPointerPos.set(actualX, actualY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    pointerID.clear();
                    mRenderer.getVirtualTrackball().pointerUp();
                    break;
            }
        } else
            gestures.detectGestures(event);
        return true;
    }

    /**
     * Listener de mudanca de preferencia (configuracao). Usado para registrar a alteracao de valor
     * da preferencia de velocidade de movimento multitouch.
     * @param preference Preferencia que foi alterada.
     * @param newValue Valor que será definido na preferencia.
     * @return true caso o evento tenha sido processado corretamente,
     * false caso nao tenha sido.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if( preference.getKey().equals(getResources().getString(R.string.key_mov_speed)) ) {
            //Log.d(TAG,"MovementSpeed changed: "+newValue);
            speedMultiTouch = calculateSpeedMultiTouch((int)newValue);
        }
        return true;
    }

    /**
     * Classe interna de {@link MyGLSurfaceView} responsável por detectar gestos multitouch.
     */
    private class DetectMultiTouch {
        private static final byte MAX_POINTERS = 2;
        private static final byte DISPLAC_POINTER_1 = 0;
        private static final byte DISPLAC_POINTER_2 = 1;
        //private static final byte POINTER_3 = 2;
        //private static final byte POINTER_4 = 3;
        private static final byte POS_POINTER_2 = 0;
        //private static final byte POS_POINTER_3 = 1;
        private static final byte DIST_1_TO_2 = 0;
        private static final byte DIST_2_TO_1 = 1;

        private static final byte MOVE_LEFT = 0;
        private static final byte MOVE_RIGHT = 1;
        private static final byte MOVE_UP = 2;
        private static final byte MOVE_DOWN = 4;
        private static final byte ROTATE_Z = 8;

        private VectorFloat[] previousPointerPos = new VectorFloat[MAX_POINTERS-1];
        private VectorFloat[] vectorDisplacPointer = new VectorFloat[MAX_POINTERS];
        private float modulus[] = new float[MAX_POINTERS];
        private VectorFloat[] distancePointers = new VectorFloat[(MAX_POINTERS-1)*2];
        private float oldDistancePointers;

        public DetectMultiTouch(){
            for(int i=0; i<previousPointerPos.length ; i++)
                previousPointerPos[i] = new VectorFloat(2);
            for(int i=0; i<vectorDisplacPointer.length; i++)
                vectorDisplacPointer[i] = new VectorFloat(2);
            for(int i=0; i<distancePointers.length; i++)
                distancePointers[i] = new VectorFloat(2);
        }

        /**
         * Método responsável por capturar os dados dos ponteiros tocados no touch e detectar se
         * houve um gesto de movimento ou rotação.
         * @param event Evento gerado pelo touch
         * @return true se o gesto detectado for um gesto multitouch, false caso contrário.
         */
        public boolean detectGestures(MotionEvent event){
            boolean ret = false;
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    pointerID.add(event.getPointerId(event.getActionIndex()));
                    if (event.getPointerCount() == 2) {
                        mRenderer.getVirtualTrackball().pointerUp();
                        int firstPointerIndex = event.findPointerIndex(pointerID.get(0));
                        int secondPointerIndex = event.findPointerIndex(pointerID.get(1));
                        float actualSecondX, actualSecondY;
                        try {
                            actualSecondX = event.getX(secondPointerIndex);
                            actualSecondY = event.getY(secondPointerIndex);
                            distancePointers[DIST_1_TO_2].set(actualSecondX - event.getX(firstPointerIndex),
                                    actualSecondY - event.getY(firstPointerIndex));
                        }catch(IllegalArgumentException e){
                            break;
                        }
                        oldDistancePointers = distancePointers[DIST_1_TO_2].norm();

                        previousPointerPos[POS_POINTER_2].set(actualSecondX, actualSecondY);
                        ret = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Global.getStateProgram() == Global.STATE_RENDER_POINTS) {
                        if (event.getPointerCount() == 2) {
                            int firstPointerIndex = event.findPointerIndex(pointerID.get(0));
                            int secondPointerIndex = event.findPointerIndex(pointerID.get(1));
                            float actualFirstX, actualFirstY;
                            float actualSecondX, actualSecondY;
                            try {
                                actualFirstX = event.getX(firstPointerIndex);
                                actualFirstY = event.getY(firstPointerIndex);
                                actualSecondX = event.getX(secondPointerIndex);
                                actualSecondY = event.getY(secondPointerIndex);
                            }catch(IllegalArgumentException e){
                                break;
                            }
                            distancePointers[DIST_1_TO_2].set(actualSecondX - actualFirstX, actualSecondY - actualFirstY);
                            float actualDistancePointers = distancePointers[DIST_1_TO_2].norm();

                            vectorDisplacPointer[DISPLAC_POINTER_1].set(
                                    actualFirstX - mPreviousPointerPos.getX(),
                                    actualFirstY - mPreviousPointerPos.getY() );

                            vectorDisplacPointer[DISPLAC_POINTER_2].set(
                                    actualSecondX - previousPointerPos[POS_POINTER_2].getX(),
                                    actualSecondY - previousPointerPos[POS_POINTER_2].getY() );

                            if( !detectMovements() ) {
                                //Gesto de pinça. Zoom in ou Zoom out
                                if (Global.getViewingStyle() == Global.VIEW_USING_TRACKBALL)
                                    mRenderer.getVirtualTrackball().zoom((oldDistancePointers - actualDistancePointers) * speedMultiTouch);
                                else
                                    mRenderer.getCamera().moveZ((oldDistancePointers - actualDistancePointers) * speedMultiTouch);
                            }

                            oldDistancePointers = actualDistancePointers;
                            previousPointerPos[POS_POINTER_2].set(actualSecondX, actualSecondY);
                            mPreviousPointerPos.set(actualFirstX, actualFirstY);
                            mRenderer.refreshMVP();
                            requestRender();
                            ret = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    int indexList = pointerID.indexOf(event.getPointerId(event.getActionIndex()));
                    if(event.getPointerCount()>2) {
                        if (indexList == 0){
                            float actualFirstX = event.getX(event.findPointerIndex(pointerID.get(1)));
                            float actualFirstY = event.getY(event.findPointerIndex(pointerID.get(1)));
                            float actualSecondX = event.getX(event.findPointerIndex(pointerID.get(2)));
                            float actualSecondY = event.getY(event.findPointerIndex(pointerID.get(2)));
                            mPreviousPointerPos.set( actualFirstX , actualFirstY );
                            previousPointerPos[POS_POINTER_2].set( actualSecondX , actualSecondY );

                            distancePointers[DIST_1_TO_2].set(actualSecondX - actualFirstX, actualSecondY - actualFirstY);
                            oldDistancePointers = distancePointers[DIST_1_TO_2].norm();
                        }else if(indexList == 1){
                            float actualFirstX = event.getX(event.findPointerIndex(pointerID.get(0)));
                            float actualFirstY = event.getY(event.findPointerIndex(pointerID.get(0)));
                            float actualSecondX = event.getX(event.findPointerIndex(pointerID.get(2)));
                            float actualSecondY = event.getY(event.findPointerIndex(pointerID.get(2)));
                            previousPointerPos[POS_POINTER_2].set( actualSecondX , actualSecondY );

                            distancePointers[DIST_1_TO_2].set(actualSecondX - actualFirstX, actualSecondY - actualFirstY);
                            oldDistancePointers = distancePointers[DIST_1_TO_2].norm();
                        }
                    }
                    //Opcao de quando há 2 dedos na tela. Remove-se um e resta somente um dedo no touch.
                    if (indexList == 0) {
                        mPreviousPointerPos.set(
                                event.getX(event.findPointerIndex(pointerID.get(1))) ,
                                event.getY(event.findPointerIndex(pointerID.get(1))) );
                    }
                    pointerID.remove(indexList);
                    ret = true;
                    break;
            }
            return ret;
        }

        private boolean detectMovements(){
            boolean ret = false;
            modulus[DISPLAC_POINTER_1] = vectorDisplacPointer[DISPLAC_POINTER_1].norm();
            modulus[DISPLAC_POINTER_2] = vectorDisplacPointer[DISPLAC_POINTER_2].norm();

            if( movements(MOVE_LEFT) ) ret=true;
            else if( movements(MOVE_RIGHT) ) ret=true;
            else if( movements(MOVE_UP) )    ret=true;
            else if( movements(MOVE_DOWN) )  ret=true;
            else if( movements(ROTATE_Z) )   ret=true;

            return ret;
        }

        private boolean movements(byte direction){
            boolean ret = false;
            float angle1, angle2;
            if( direction==MOVE_UP || direction==MOVE_DOWN ){
                angle1 = (float)(Math.acos( (-vectorDisplacPointer[DISPLAC_POINTER_1].getY()/
                        modulus[DISPLAC_POINTER_1]) )*180.0/Math.PI);
                angle2 = (float)(Math.acos( (-vectorDisplacPointer[DISPLAC_POINTER_2].getY()/
                        modulus[DISPLAC_POINTER_2]) )*180.0/Math.PI);
            } else {
                angle1 = (float)(Math.acos( (vectorDisplacPointer[DISPLAC_POINTER_1].getX()/
                        modulus[DISPLAC_POINTER_1]) )*180.0/Math.PI);
                angle2 = (float)(Math.acos( (vectorDisplacPointer[DISPLAC_POINTER_2].getX()/
                        modulus[DISPLAC_POINTER_2]) )*180.0/Math.PI);
            }
            if(direction == MOVE_LEFT) {
                if (angle2 >= 155.0f && angle2 <= 180.0f)
                    if (angle1 >= 155.0f && angle1 <= 180.0f) {
                        if( Global.getViewingStyle()==Global.VIEW_USING_TRACKBALL )
                            mRenderer.getVirtualTrackball().moveX((-vectorDisplacPointer[DISPLAC_POINTER_2].getX())*speedMultiTouch);
                        else
                            mRenderer.getCamera().moveX((vectorDisplacPointer[DISPLAC_POINTER_2].getX())*speedMultiTouch);
                        ret = true;
                    }
            }else if(direction == MOVE_RIGHT) {
                if( angle2>=0.0f && angle2<=25.0f )
                    if( angle1>=0.0f && angle1<=25.0f ) {
                        if( Global.getViewingStyle()==Global.VIEW_USING_TRACKBALL )
                            mRenderer.getVirtualTrackball().moveX((-vectorDisplacPointer[DISPLAC_POINTER_2].getX())*speedMultiTouch);
                        else
                            mRenderer.getCamera().moveX((vectorDisplacPointer[DISPLAC_POINTER_2].getX())*speedMultiTouch);
                        ret = true;
                    }
            }else if(direction == MOVE_UP){
                if( angle2>=0.0f && angle2<=25.0f )
                    if( angle1>=0.0f && angle1<=25.0f ){
                        if( Global.getViewingStyle()==Global.VIEW_USING_TRACKBALL )
                            mRenderer.getVirtualTrackball().moveY((vectorDisplacPointer[DISPLAC_POINTER_2].getY())*speedMultiTouch);
                        else
                            mRenderer.getCamera().moveY((-vectorDisplacPointer[DISPLAC_POINTER_2].getY())*speedMultiTouch);
                        ret = true;
                    }
            }else if(direction == MOVE_DOWN){
                if( angle2>=155.0f && angle2<=180.0f )
                    if( angle1>=155.0f && angle1<=180.0f ){
                        if( Global.getViewingStyle()==Global.VIEW_USING_TRACKBALL )
                            mRenderer.getVirtualTrackball().moveY((vectorDisplacPointer[DISPLAC_POINTER_2].getY())*speedMultiTouch);
                        else
                            mRenderer.getCamera().moveY((-vectorDisplacPointer[DISPLAC_POINTER_2].getY())*speedMultiTouch);
                        ret = true;
                    }
            }else if(direction == ROTATE_Z){
                if( Global.getViewingStyle()==Global.VIEW_USING_CAMERA ) {
                    float distancePointersTested = distancePointers[DIST_1_TO_2].getY();
                    float value = vectorDisplacPointer[DISPLAC_POINTER_1].getX();
                    if (angle1 >= 65.0f && angle1 <= 115.0f) {
                        angle1 = (float) (Math.acos((-vectorDisplacPointer[DISPLAC_POINTER_1].getY() /
                                modulus[DISPLAC_POINTER_1])) * 180.0 / Math.PI);
                        angle2 = (float) (Math.acos((-vectorDisplacPointer[DISPLAC_POINTER_2].getY() /
                                modulus[DISPLAC_POINTER_2])) * 180.0 / Math.PI);
                        distancePointersTested = distancePointers[DIST_1_TO_2].getX();
                        value = -vectorDisplacPointer[DISPLAC_POINTER_1].getY();
                    }
                    if (Math.abs(distancePointersTested) >= 200.0f) {
                        if (distancePointersTested >= 0.0f) {
                            if (angle1 >= 155.0f && angle1 <= 180.0f) {
                                if (angle2 >= 0.0f && angle2 <= 25.0f) ret = true;
                            } else if (angle1 >= 0.0f && angle1 <= 25.0f) {
                                if (angle2 >= 155.0f && angle2 <= 180.0f) ret = true;
                            }
                            if (ret)
                                mRenderer.getCamera().rotate(0f, 0f, value*speedMultiTouch*4);
                        } else {
                            if (angle1 >= 155.0f && angle1 <= 180.0f) {
                                if (angle2 >= 0.0f && angle2 <= 25.0f) ret = true;
                            } else if (angle1 >= 0.0f && angle1 <= 25.0f) {
                                if (angle2 >= 155.0f && angle2 <= 180.0f) ret = true;
                            }
                            if (ret)
                                mRenderer.getCamera().rotate(0f, 0f, -value*speedMultiTouch*4);
                        }
                    }
                }
            }
            return ret;
        }


    }// end class DetectMultiTouch



} // end class MyGLSurfaceView
