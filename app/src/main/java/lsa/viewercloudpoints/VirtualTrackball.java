package lsa.viewercloudpoints;


import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by Luan Sala on 28/07/2015.
 */
public class VirtualTrackball {
    private static final String TAG = "VirtualTrackball";

    private static final byte UPDATE_NORMALIZE_ROTATION = 127;

    private static final float TRACKBALL_RADIUS = 1.0f;

    private byte countUpdateRotation = 0;

    private boolean touchPressed = false;

    private Quaternion test = new Quaternion(0f,0f,0f,0f);
    private float displacement[] = new float[3];
    private float mMatrix[]      = new float[16];

    private short width, height;

    private Quaternion rotation;

    private VectorFloat oldPointer = new VectorFloat(2);

    public VirtualTrackball() {
        rotation = Quaternion.getRotationIdentity();
        loadMatrixIdentity();
    }

    public VirtualTrackball(float[] floatArray){
        this.rotation = new Quaternion(floatArray[0],floatArray[1],floatArray[2],floatArray[3]);
        System.arraycopy(rotation.getMatrix(),0,mMatrix,0,16);
        displacement[0] = floatArray[4];
        displacement[1] = floatArray[5];
        displacement[2] = floatArray[6];
        applyZoom();
    }

    public void updateWindowSize(){
        width = Global.SCREEN_WIDTH;
        height = Global.SCREEN_HEIGHT;
    }

    public Quaternion getRotationCopy(){
        return new Quaternion(rotation);
    }

    //Função que retorna os valores do estado atual da virtual trackball.
    // floatArray[0,1,2,3] = quaternion
    // floatArray[4,5,6] = displacement
    public float[] getForSaveInstanceState(){
        float[] rot = rotation.getFloatArray();
        return new float[]{
                rot[0], rot[1], rot[2], rot[3],
                displacement[0],
                displacement[1],
                displacement[2]};
    }

    public float[] getMatrix(){
        float[] tempMatrix = new float[16];
        //test.set(0,displacement[0],0f,0f);
        //test.set( (rotation.mult(test)).mult( rotation.conjugate() ) );
        Matrix.translateM(tempMatrix, 0, mMatrix, 0,
                displacement[0], displacement[1], displacement[2]);
        return tempMatrix;
        //return mMatrix;
    }

    public void setDisplacement(VectorFloat displac, float zoom){
        if ( displac!=null ) {
            displacement[0] = -displac.getX();
            displacement[1] = -displac.getY();
            displacement[2] = -displac.getZ();
            this.zoom = zoom;
            applyZoom();
        }
    }

    public void pointerDown(float x, float y){
        oldPointer.setX( (2f*x - width) / width );  // Colocando os valores de X e Y no intervalo
        oldPointer.setY( (height - 2f*y) / height );// de [-1, 1].
        touchPressed = true;
    }

    public void pointerUp(){
        touchPressed = false;
    }

    public void pointerMove(float x, float y){
        if(touchPressed) {
            x = (2f * x - width) / width;
            y = (height - 2f * y) / height;

            trackball(x, y);

            oldPointer.set(x, y);
            applyZoom();
        }else pointerDown(x,y);
    }

    // Função que simula a trackball virtual baseado na implementação de Holroyd.
    // Este site https://www.opengl.org/wiki/Object_Mouse_Trackball foi utilizado
    // como referência.
    private void trackball(float x, float y){
        if( oldPointer.getX()==x && oldPointer.getY()==y )
            return;

        VectorFloat pOld = new VectorFloat(oldPointer.getX(),oldPointer.getY(),0f);
        VectorFloat pNew = new VectorFloat(x, y, 0f);

        mapsOntoSurface(pOld);
        mapsOntoSurface(pNew);

        pOld.normalize();
        pNew.normalize();

        VectorFloat axis = pNew.crossProduct(pOld);
        //Log.d(TAG, ""+rotation);
        float arg = pOld.dotProduct(pNew);
        arg = arg>=-1f && arg<=1f ? arg : (arg/Math.abs(arg) > 0f ? 1f : -1f);
        float theta = (float)Math.acos( arg );
        rotation.set( rotation.mult(Quaternion.getRotationQuaternion(theta, axis)) );
        if(countUpdateRotation==UPDATE_NORMALIZE_ROTATION){
            countUpdateRotation = 0;
            rotation.normalize();
        }else countUpdateRotation++;
        //mMatrix = rotation.getMatrix();
        System.arraycopy(rotation.getMatrix(),0,mMatrix,0,16);
    }


    private void mapsOntoSurface(VectorFloat p){
        float overSqrt2 = 0.70710678118654752440f;
        float length = p.norm();
        float r = TRACKBALL_RADIUS;

        if( length <= (r*overSqrt2) ) // r/sqrt(2)
            p.setZ( (float)Math.sqrt( (r*r)-(length*length) ) );
        else
            p.setZ( (r*r)/(2f*length) );
    }

    public void moveX(float dx){
        displacement[0] -= dx;
        //applyZoom();
    }

    public void moveY(float dy){
        displacement[1] -= dy;
        //applyZoom();
    }

    public void moveZ(float dz){
        zoom -= dz;
        applyZoom();
    }

    private void applyZoom(){
        //mMatrix[12] = displacement[0];
        //mMatrix[13] = displacement[1];
        mMatrix[14] = zoom;
        mMatrix[15] = 1f;
    }

    private void loadMatrixIdentity(){
        mMatrix[ 0]=1f; mMatrix[ 1]=0f; mMatrix[ 2]=0f; mMatrix[ 3]=0f;
        mMatrix[ 4]=0f; mMatrix[ 5]=1f; mMatrix[ 6]=0f; mMatrix[ 7]=0f;
        mMatrix[ 8]=0f; mMatrix[ 9]=0f; mMatrix[10]=1f; mMatrix[11]=0f;
        mMatrix[12]=0f; mMatrix[13]=0f; mMatrix[14]=0f; mMatrix[15]=1f;
    }

    private float zoom;
}
