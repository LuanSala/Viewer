package lsa.viewercloudpoints;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by Luan Sala on 27/02/2015.
 */
public class TexOpenFile extends Texture {
    private static float texCoord[] = {
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f
    };

    private float mModelViewMatrix[] = new float[16];

    private float translateX = 1.2f;
    private float translateY = 1.65f;
    private float scale = 0.3f;

    public TexOpenFile(){
        super(texCoord);
        loadTexture(Global.OPEN_FILE,Global.getContext());
    }

    public void setModelViewMatrix(){
        Matrix.setIdentityM(mModelViewMatrix,0);
        Matrix.scaleM(mModelViewMatrix,0,scale,Global.getProportionateHeight(scale),scale);
        Matrix.translateM(mModelViewMatrix,0,translateX,translateY,0.0f);
    }

    public float getTranslateX(){
        return translateX;
    }
    public float getTranslateY(){
        return translateY;
    }
    public float getScale(){
        return scale;
    }

    public void drawButton() {
        draw(mModelViewMatrix);
    }
}
