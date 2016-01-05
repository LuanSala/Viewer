package lsa.viewercloudpoints;

/**
 * Created by Luan Sala on 06/02/2015.
 */

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "MyGLRenderer";

    private float[] tempMatrix              = new float[16];
    private final float[] mMVPMatrix        = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private VirtualTrackball virtualTrackball;
    private Camera camera;
    private Background background;

    private VectorFloat centerTrackball;
    private Points mPoints;
    private boolean updatePoints;
    private boolean MVPModified;

    private static final short COLOR_BUFFER             = 0;
    private static final short DEPTH_BUFFER             = 1;
    private static final short NUM_RENDERBUFFER         = 2;
    private static final short defaultFrameBufferWidth  = 256;
    private static final short defaultFrameBufferHeight = 256;

    private int frameBuffer[]  = new int[1];
    private int renderBuffer[] = new int[NUM_RENDERBUFFER];

    //public TexOpenFile fileButton;

    public MyGLRenderer() {
        mTranslucentBackground = true;
        MVPModified = true;
    }

    public MyGLRenderer(Bundle savedInstanceState) {
        mTranslucentBackground = true;
        MVPModified = true;

        float[] floatArray = savedInstanceState.getFloatArray(Global.getContext().getString(R.string.key_save_virtual_trackball));
        virtualTrackball = new VirtualTrackball(floatArray);
        floatArray = savedInstanceState.getFloatArray(Global.getContext().getString(R.string.key_save_camera));
        camera = new Camera(floatArray);
    }

    public Camera getCamera(){
        return camera;
    }

    public VirtualTrackball getVirtualTrackball() {
        return virtualTrackball;
    }

    public void refreshMVP(){
        MVPModified = true;
    }

    public void onDrawFrame(GL10 unused) {
        //Log.d(TAG,"onDrawFrame");
        if(updatePoints){
            mPoints.disable();
            mPoints.update();
            updatePoints = false;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        switch (Global.getStateProgram()){
            case Global.STATE_INIT_PROGRAM:
                GLES20.glEnable(GLES20.GL_TEXTURE_2D);
                //GLES20.glEnable(GLES20.GL_BLEND);
                //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
                //fileButton.drawButton();
                GLES20.glDisable(GLES20.GL_TEXTURE_2D);
                GLES20.glDisable(GLES20.GL_BLEND);
                break;
            case Global.STATE_RENDER_POINTS:
                background.draw();
                if(mPoints.isEnabled()) {
                    if (MVPModified) {
                        if (Global.getViewingStyle() == Global.VIEW_USING_TRACKBALL) {
                            if (Global.requestedCentralizeTrackball()) {
                                synchronized (mPoints.getMediumPointThread()) {
                                    if (!mPoints.mediumPointCalculated())
                                        try {
                                            mPoints.getMediumPointThread().wait();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                }
                                //Matrix.translateM(tempMatrix, 0, virtualTrackball.getMatrix(), 0,
                                        //-mPoints.getMediumPoint().getX(),
                                        //-mPoints.getMediumPoint().getY(),
                                        //-mPoints.getMediumPoint().getZ());
                                virtualTrackball.setCenter(mPoints.getMediumPoint(), mPoints.getZoomNeeded());
                                System.arraycopy(virtualTrackball.getMatrix(), 0, tempMatrix, 0, 16);
                            } else
                                System.arraycopy(virtualTrackball.getMatrix(), 0, tempMatrix, 0, 16);
                            Matrix.multiplyMM(mMVPMatrix, 0,
                                    mProjectionMatrix, 0, tempMatrix, 0);
                        } else
                            Matrix.multiplyMM(mMVPMatrix, 0,
                                    mProjectionMatrix, 0, camera.getViewMatrix(), 0);
                    }
                    mPoints.draw(MVPModified, mMVPMatrix);
                    MVPModified = false;
                    break;
                }
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        /*try {
            updateFrameBuffer(width, height);
        }catch(RuntimeException e){
            e.printStackTrace();
            System.exit(1);
        }*/
        //Log.d(TAG,"SurfaceChanged");
        Global.SCREEN_WIDTH  = (short)width;
        Global.SCREEN_HEIGHT = (short)height;
        GLES20.glViewport(0, 0, width, height);
        float ratio = ((float) width) /((float) height);
        //Matrix.setIdentityM( mProjectionMatrix,0 );
        Matrix.perspectiveM( mProjectionMatrix,0,60.0f,ratio,0.06f,20000.0f);

        background.update(mProjectionMatrix);
        virtualTrackball.updateWindowSize();
        //fileButton.setModelViewMatrix();
        MVPModified = true;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //Log.d(TAG,"SurfaceCreated");
        background = new Background();
        if (virtualTrackball==null)
            virtualTrackball = new VirtualTrackball();
        if (camera==null) {
            camera = new Camera();
            camera.moveX(5f);
            camera.moveZ(20f);
        }
        refreshMVP();

        //createFrameBuffer();
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        //GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);

        GLES20.glDisable(GLES20.GL_DITHER);
        //GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        //fileButton = new TexOpenFile();
        mPoints = new Points();
    }

    public void updatePoints(){
        updatePoints = true;
        Global.centralizeTrackball();
    }

    public void updateFrameBuffer(int width, int height){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[COLOR_BUFFER]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_RGB565, width, height);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[DEPTH_BUFFER]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);

        if( GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)!=GLES20.GL_FRAMEBUFFER_COMPLETE )
            throw new RuntimeException("Framebuffer not is complete!");

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    /**
     * Cria o frameBuffer. As primitivas serão renderizadas neste framebuffer.
     */
    public void createFrameBuffer(){
        GLES20.glGenFramebuffers(1,frameBuffer,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glGenRenderbuffers(NUM_RENDERBUFFER, renderBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[COLOR_BUFFER]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_RGB565, defaultFrameBufferWidth, defaultFrameBufferHeight);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_RENDERBUFFER, renderBuffer[COLOR_BUFFER]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[DEPTH_BUFFER]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, defaultFrameBufferWidth, defaultFrameBufferHeight);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBuffer[DEPTH_BUFFER]);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }


    private boolean mTranslucentBackground;
}
