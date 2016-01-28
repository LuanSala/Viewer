package lsa.viewercloudpoints.modeVision;

import android.opengl.Matrix;

import lsa.viewercloudpoints.math.Quaternion;

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

    public Camera(float[] floatArray){
        rotation = new Quaternion(floatArray[0],floatArray[1],floatArray[2],floatArray[3]);
        up = new Quaternion(floatArray[4],floatArray[5],floatArray[6],floatArray[7]);
        axisX = new Quaternion(floatArray[8],floatArray[9],floatArray[10],floatArray[11]);
        displacement = new float[3];
        displacement[0] = floatArray[12];
        displacement[1] = floatArray[13];
        displacement[2] = floatArray[14];
        mViewMatrix = new float[16];
        System.arraycopy(rotation.getMatrix(),0,tempViewMatrix,0,16);
        applyTranslate();
    }

    //Função que retorna os valores do estado atual da câmera.
    // floatArray[0,1,2,3] = rotation quaternion
    // floatArray[4,5,6,7] = up quaternion
    // floatArray[8,9,10,11] = axisX quaternion
    // floatArray[12,13,14] = displacement
    public float[] getForSaveInstanceState(){
        float[] rot = rotation.getFloatArray();
        float[] up = this.up.getFloatArray();
        float[] aX = axisX.getFloatArray();
        return new float[]{
                rot[0], rot[1], rot[2], rot[3],
                up[0], up[1], up[2], up[3],
                aX[0], aX[1], aX[2], aX[3],
                displacement[0], displacement[1], displacement[2]
        };
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
        System.arraycopy(mViewMatrix,0,tempViewMatrix,0,16);
        //tempViewMatrix = mViewMatrix;
    }

    public void joinRotation(Quaternion q){
        rotation.set( rotation.mult(q) );
    }

    public void move(float dx, float dy){
        System.arraycopy(rotation.getMatrix(),0,tempViewMatrix,0,16);
        //tempViewMatrix = rotation.getMatrix();
        Quaternion displac = new Quaternion(0,-dx,dy,0);
        displac.set( rotation.mult(displac).mult(rotation.conjugate()) );
        displacement[0] += displac.getX();
        displacement[1] += displac.getY();
        displacement[2] += displac.getZ();
        applyTranslate();
    }

    public void moveZ(float dz){
        System.arraycopy(rotation.getMatrix(),0,tempViewMatrix,0,16);
        //tempViewMatrix = rotation.getMatrix();
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

        System.arraycopy(rotation.getMatrix(),0,tempViewMatrix,0,16);
        //tempViewMatrix = rotation.getMatrix();
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
