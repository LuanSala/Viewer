package lsa.viewercloudpoints;

import android.opengl.Matrix;

/**
 * Created by Luan Sala on 24/07/2015.
 */
public class Camera {
    private static final byte UPDATE_NORMALIZE_ROTATION = 127;
    private byte countUpdateRotation = 0;

    private Quaternion rotation;
    private Quaternion up;
    private Quaternion axisX;
    private float displacement[];
    private float mViewMatrix[];
    private float tempViewMatrix[] = new float[16];

    public Camera(){
        mViewMatrix = new float[16];
        displacement = new float[3];
        rotation = Quaternion.getRotationIdentity();
        axisX = new Quaternion(0f,1f,0f,0f);
        up = new Quaternion(0f,0f,1f,0f);
        loadViewIdentity();
    }

    public float[] getDisplacement(){
        return displacement;
    }

    public float[] getViewMatrix(){
        return mViewMatrix;
    }

    public void loadViewIdentity(){
        mViewMatrix[ 0]=1f; mViewMatrix[ 1]=0f; mViewMatrix[ 2]=0f; mViewMatrix[ 3]=0f;
        mViewMatrix[ 4]=0f; mViewMatrix[ 5]=1f; mViewMatrix[ 6]=0f; mViewMatrix[ 7]=0f;
        mViewMatrix[ 8]=0f; mViewMatrix[ 9]=0f; mViewMatrix[10]=1f; mViewMatrix[11]=0f;
        mViewMatrix[12]=0f; mViewMatrix[13]=0f; mViewMatrix[14]=0f; mViewMatrix[15]=1f;
        tempViewMatrix = mViewMatrix;
    }

    public void joinRotation(Quaternion q){
        rotation.set( rotation.mult(q) );
    }

    public void moveX(float dx){
        tempViewMatrix = rotation.getMatrix();
        Quaternion displac = new Quaternion(0,-dx,0,0);
        displac.set( rotation.mult(displac).mult(rotation.conjugate()) );
        displacement[0] += displac.getX();
        displacement[1] += displac.getY();
        displacement[2] += displac.getZ();
        applyTranslate();
    }

    public void moveY(float dy){
        tempViewMatrix = rotation.getMatrix();
        Quaternion displac = new Quaternion(0,0,-dy,0);
        displac.set( rotation.mult(displac).mult(rotation.conjugate()) );
        displacement[0] += displac.getX();
        displacement[1] += displac.getY();
        displacement[2] += displac.getZ();
        applyTranslate();
    }

    public void moveZ(float dz){
        tempViewMatrix = rotation.getMatrix();
        Quaternion displac = new Quaternion(0,0,0,-dz);
        displac.set( rotation.mult(displac).mult(rotation.conjugate()) );
        displacement[0] += displac.getX();
        displacement[1] += displac.getY();
        displacement[2] += displac.getZ();
        applyTranslate();
    }

    private void applyTranslate(){
        Matrix.translateM(mViewMatrix, 0, tempViewMatrix, 0,
                displacement[0], displacement[1], displacement[2]);
    }

    public void rotate(float rotX, float rotY, float rotZ){
        // transforma o angulo de rotação de graus para radianos. PI/180f=0.0174...
        // 0.2f uma constante para diminuir a velocidade de transição.
        rotX *= (float) (0.017453292519943295729)*0.2f;
        rotY *= (float) (0.017453292519943295729)*0.2f;
        rotZ *= (float) (0.017453292519943295729)*0.2f;
        rotation.set( Quaternion.getRotationQuaternion(rotY,up.getX(),up.getY(),up.getZ()).mult(rotation) ); // Rotação Global
        rotation.set( rotation.mult(Quaternion.getRotationQuaternion(rotX, axisX.getX(), axisX.getY(), axisX.getZ())) ); // Rotação Local
        Quaternion rZ = Quaternion.getRotationQuaternion(rotZ,0f,0f,1f);
        rotation.set( rotation.mult( rZ ) ); // Rotação Local
        up.set( rZ.mult(up).mult(rZ.conjugate()) );

        if(countUpdateRotation==UPDATE_NORMALIZE_ROTATION){
            countUpdateRotation = 0;
            rotation.normalize();
        }else countUpdateRotation++;

        tempViewMatrix = rotation.getMatrix();
        applyTranslate();
    }

    private static void setRotateEuler(float[] rm,
                                       float x, float y, float z) {
        x *= (float) (0.017453292519943295729); // Esse valor é o resultado de PI/180
        y *= (float) (0.017453292519943295729);
        z *= (float) (0.017453292519943295729);
        float cx = (float) Math.cos(x);
        float sx = (float) Math.sin(x);
        float cy = (float) Math.cos(y);
        float sy = (float) Math.sin(y);
        float cz = (float) Math.cos(z);
        float sz = (float) Math.sin(z);
        float sycz = sy * cz;
        float sysz = sy * sz;

        rm[0]  =  cy * cz;
        rm[1]  =  sycz * sx + cx * sz;
        rm[2]  = -sycz * cx + sx * sz;
        rm[3]  =  0.0f;

        rm[4]  = -cy * sz;
        rm[5]  = -sysz * sx + cx * cz;
        rm[6]  =  sysz * cx + sx * cz;
        rm[7]  =  0.0f;

        rm[8]  =  sy;
        rm[9]  = -sx * cy;
        rm[10] =  cx * cy;
        rm[11] =  0.0f;

        rm[12] =  0.0f;
        rm[13] =  0.0f;
        rm[14] =  0.0f;
        rm[15] =  1.0f;
    }

    private static void setRotateArbitrary(float[] rm,
                                           float angle, float eX, float eY, float eZ){
        angle *= (float) (0.017453292519943295729); // Esse valor é o resultado de PI/180
        float co = (float)Math.cos((double)angle);
        float si = (float)Math.sin((double)angle);

        rm[0] = co + (1-co)*eX*eX;
        rm[1] = eX*eY*(1-co) + eZ*si;
        rm[2] = eX*eZ*(1-co) - eY*si;
        rm[3] = 0.0f;

        rm[4] = eY*eX*(1-co) - eZ*si;
        rm[5] = co + (1-co)*eY*eY;
        rm[6] = eY*eZ*(1-co) - eX*si;
        rm[7] = 0.0f;

        rm[8] =  eZ*eX*(1-co) + eY*si;
        rm[9] =  eZ*eY*(1-co) - eX*si;
        rm[10] = co + (1-co)*eZ*eZ;
        rm[11] = 0.0f;

        rm[12] = 0.0f;
        rm[13] = 0.0f;
        rm[14] = 0.0f;
        rm[15] = 1.0f;
    }

}
