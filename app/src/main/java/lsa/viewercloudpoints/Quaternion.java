package lsa.viewercloudpoints;

/**
 * Created by Luan Sala on 21/07/2015.
 */
public class Quaternion {

    private float s;
    private float x, y, z;

    public Quaternion(Quaternion q){
        s = q.getReal();
        x = q.getX();
        y = q.getY();
        z = q.getZ();
    }

    public Quaternion(float scalar, float[] vector) {
        if( vector.length!=3 ) throw new IllegalArgumentException("Length of the argument 'vector' != 3");

        s = scalar;
        x = vector[0];
        y = vector[1];
        z = vector[2];
    }

    public Quaternion(float[] vector) {
        if( vector.length!=4 ) throw new IllegalArgumentException("Length of the argument 'vector' != 4");

        s = vector[0];
        x = vector[1];
        y = vector[2];
        z = vector[3];
    }

    public Quaternion(float scalar, float x, float y, float z){
        s = scalar;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion(float scalar, VectorFloat v){
        s = scalar;
        x = v.getX();
        y = v.getY();
        z = v.getZ();
    }

    public float getReal(){
        return s;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getZ(){
        return z;
    }

    public void set(Quaternion q){
        s = q.s;
        x = q.x;
        y = q.y;
        z = q.z;
    }

    static public Quaternion getRotationIdentity(){
        return new Quaternion(1f,0f,0f,0f);
    }

    static public Quaternion getRotationQuaternion(float angle,
                                                   float x, float y, float z){
        float scalar = (float)Math.cos(angle*0.5f);
        float sin    = (float)Math.sin(angle*0.5f);
        float norm = (float)Math.sqrt( x*x + y*y + z*z );
        norm = 1f/norm;
        return new Quaternion(scalar,x*sin*norm,y*sin*norm,z*sin*norm);
    }

    static public Quaternion getRotationQuaternion(float angle,
                                                   VectorFloat axis){
        float scalar = (float)Math.cos(angle*0.5f);
        float sin    = (float)Math.sin(angle*0.5f);
        float x = axis.getX();
        float y = axis.getY();
        float z = axis.getZ();
        float norm = (float)Math.sqrt( x*x + y*y + z*z );
        norm = 1f/norm;
        return new Quaternion(scalar,x*sin*norm,y*sin*norm,z*sin*norm);
    }

    public Quaternion add(Quaternion q){
        return new Quaternion( s+q.getReal(),
                x+q.getX(), y+q.getY(), z+q.getZ() );
    }

    public float norm(){
        return ((float)Math.sqrt( s*s + x*x + y*y + z*z ));
    }

    public float dotProduct(Quaternion q){
        return ( s*q.getReal() + x*q.getX() + y*q.getY() + z*q.getZ() );
    }

    public Quaternion conjugate(){
        return new Quaternion(s, -x, -y, -z);
    }

    public Quaternion mult(float a){
        return new Quaternion(a*s, a*x, a*y, a*z);
    }

    public Quaternion mult(Quaternion q){
        float scalar = s*q.getReal() - ( x*q.getX()+y*q.getY()+z*q.getZ() );
        float vector[] = {s*q.getX()+q.getReal()*x + (y*q.getZ()-z*q.getY()),
                          s*q.getY()+q.getReal()*y + (z*q.getX()-x*q.getZ()),
                          s*q.getZ()+q.getReal()*z + (x*q.getY()-y*q.getX()) };

        return new Quaternion(scalar,vector);
    }

    public void normalize(){
        float norm = this.norm();
        if( norm>0.00001f && Math.abs(norm-1f)>0.00001f ) {
            norm = 1.0f / norm;
            s = s * norm;
            x = x * norm;
            y = y * norm;
            z = z * norm;
        }
    }

    public float[] getMatrix(){
        float xx = x*x; float yy = y*y; float zz = z*z;
        float xs = x*s; float xy = x*y; float xz = x*z;
        float ys = y*s;                 float yz = y*z;
        float zs = z*s;

        return new float[]{
                1-2*(yy + zz), 2*(xy-zs)   , 2*(xz+ys)  , 0.0f,
                2*(xy+zs)    , 1-2*(xx+zz) , 2*(yz-xs)  , 0.0f,
                2*(xz-ys)    , 2*(yz+xs)   , 1-2*(xx+yy), 0.0f,
                0.0f         , 0.0f        , 0.0f       , 1.0f };
    }

    @Override
    public String toString(){
        return "Scalar: "+s+"   X: "+x+" Y: "+y+" Z: "+z+"\nNorm: "+norm()+"\n";
    }

}
