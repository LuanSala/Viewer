package lsa.viewercloudpoints.math;

/**
 * Created by Luan Sala on 24/07/2015.
 */
public class VectorFloat {
    private float data[];
    private byte size;

    public VectorFloat(){
        size = 3;
        data = new float[size];
    }

    public VectorFloat(int size) {
        if(size<=1 || size>3) throw new IllegalArgumentException("Size is illegal!");
        this.size = (byte)size;
        data = new float[size];
    }

    public VectorFloat(float[] v){
        if(v.length<=1 || v.length>3) throw new IllegalArgumentException("Size of the argument v is illegal!");
        size = (byte)v.length;
        data = new float[size];
        System.arraycopy(v,0,data,0,v.length);
        //for(int i=0; i<size; i++)
            //data[i] = v[i];
    }

    public VectorFloat(float d0, float d1){
        size = 2;
        data = new float[size];
        data[0] = d0;
        data[1] = d1;
    }

    public VectorFloat(float d0, float d1, float d2){
        size = 3;
        data = new float[size];
        data[0] = d0;
        data[1] = d1;
        data[2] = d2;
    }

    public VectorFloat(VectorFloat v){
        size = v.size();
        data = new float[size];
        data[0] = v.getX();
        data[1] = v.getY();
        if(size==3) data[2] = v.getZ();
    }

    public byte size(){
        return size;
    }

    public float getX(){
        return data[0];
    }

    public float getY(){
        if(size>1) return data[1];
        else return 0f;
    }

    public float getZ(){
        if(size>2) return data[2];
        else return 0f;
    }

    public void set(float x, float y){
        data[0] = x;
        data[1] = y;
    }

    public void set(float x, float y, float z){
        set(x,y);
        if(size==3) data[2] = z;
    }

    public void set(VectorFloat v){
        set(v.getX(),v.getY(),v.getZ());
    }

    public void setX(float x){
        data[0] = x;
    }

    public void setY(float y){
        data[1] = y;
    }

    public void setZ(float z){
        if(size>2) data[2] = z;
    }

    public VectorFloat add(VectorFloat v2){
        VectorFloat result;
        if(size>=v2.size()){
            if(size==2) result = new VectorFloat(data[0]+v2.getX(), data[1]+v2.getY());
            else result = new VectorFloat(data[0]+v2.getX(), data[1]+v2.getY(), data[2]+v2.getZ());
        }else{
            result = new VectorFloat(data[0]+v2.getX(), data[1]+v2.getY(), getZ()+v2.getZ());
        }
        return result;
    }

    public VectorFloat sub(VectorFloat v2){
        VectorFloat result;
        if(size>=v2.size()){
            if(size==2) result = new VectorFloat(data[0]-v2.getX(), data[1]-v2.getY());
            else result = new VectorFloat(data[0]-v2.getX(), data[1]-v2.getY(), data[2]-v2.getZ());
        }else{
            result = new VectorFloat(data[0]-v2.getX(), data[1]-v2.getY(), getZ()-v2.getZ());
        }
        return result;
    }
    /**
     * Metodo que retorna o modulo (norma) de um vetor.
     * @return O modulo do vetor
     */
    public float norm(){
        float norm;
        if(size==2) norm = data[0]*data[0] + data[1]*data[1];
        else norm = data[0]*data[0] + data[1]*data[1] + data[2]*data[2];
        return (float)Math.sqrt(norm);
    }

    public void normalize(){
        float norm = norm();
        norm = 1f/norm;
        data[0] = data[0]*norm;
        data[1] = data[1]*norm;
        if(size==3) data[2] = data[2]*norm;
    }

    public float dotProduct(VectorFloat v2){
        return (data[0]*v2.getX() + data[1]*v2.getY() + getZ()*v2.getZ());
    }

    public VectorFloat crossProduct(VectorFloat v2){
        float x =  (getY()*v2.getZ() - getZ()*v2.getY());
        float y = -(getX()*v2.getZ() - getZ()*v2.getX());
        float z =  (getX()*v2.getY() - getY()*v2.getX());
        return new VectorFloat(x,y,z);
    }

    @Override
    public String toString(){
        return "X: "+data[0]+" Y: "+data[1]+" Z: "+getZ()+"___Norm: "+norm()+"\n";
    }

}
