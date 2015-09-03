package lsa.viewercloudpoints;

/**
 * Created by Luan Sala on 09/02/2015.
 */

import android.opengl.GLES20;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Points {
    private static final String TAG = "Points";

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 vPosition;" +
            "attribute vec3 vColor;" +
            "varying mediump vec4 fColor;" +
            "void main() {" +
            "  gl_PointSize = float(3);" +
            "  gl_Position = uMVPMatrix * vec4(vPosition,float(1));" +
            "  fColor = vec4(vColor,float(1));" +
            "}";

    private final String fragmentShaderCode =
            "varying mediump vec4 fColor;" +
            "void main() {" +
            "  gl_FragColor = fColor;" +
            "}";

    private int mProgram;
    private int mPosition;
    private int mColor;
    private int mMVPMatrix;

    private int totalPoints;

    private MediumPoint mMediumPointThread;
    private VectorFloat mediumPoint = new VectorFloat(3);

    private int VBO[] = new int[1];
    //private FloatBuffer vertexBuffer;
    //private ByteBuffer colorBuffer;

    private ByteBuffer buffer;

    private boolean mediumPointCalculated = false;

    public Points(){
        // prepare shaders and OpenGL program
        int vertexShader = ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        mProgram = ShaderHelper.createAndLinkProgram(vertexShader,fragmentShader);

        mPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColor    = GLES20.glGetAttribLocation(mProgram, "vColor");
        mMVPMatrix= GLES20.glGetUniformLocation(mProgram,"uMVPMatrix");

        initSomePoints();
        loadVBO();

        mMediumPointThread = new MediumPoint();
        mMediumPointThread.start();

        Log.d(TAG,"Rendered points = "+totalPoints);
    }

    public Thread getMediumPointThread(){
        return mMediumPointThread;
    }

    public boolean mediumPointCalculated(){
        return mediumPointCalculated;
    }

    public VectorFloat getMediumPoint() {
        return mediumPoint;
    }

    /** Funcao temporaria... Somente para testes */
    /* Há erros na hora de se converter o valor lido de um arquivo de texto para
       um valor de ponto flutuante. (erro de precisao) */
    /*private void initSomePoints(){
        String fileName = "points_referencia.txt";
        //String fileName = "points.b";
        File file;

        if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(baseDir + File.separator + fileName);
            if( file.isFile() ){
                try {
                    FileReader fileR = new FileReader(file);
                    BufferedReader bufferR = new BufferedReader(fileR);

                    Vector<Float> listVertex = new Vector<>();
                    Vector<Float> listColor = new Vector<>();
                    while( bufferR.ready() ){
                        String temp = bufferR.readLine();
                        String[] temp2 = temp.split(" ");

                        float x = Float.valueOf(temp2[0]);
                        float y = Float.valueOf(temp2[1]);
                        float z = Float.valueOf(temp2[2]);
                        short r = Short.valueOf(temp2[3]);
                        short g = Short.valueOf(temp2[4]);
                        short b = Short.valueOf(temp2[5]);

                        listVertex.addElement( x );
                        listVertex.addElement( y );
                        listVertex.addElement( z );
                        listColor.addElement( r/255.0f );
                        listColor.addElement( g/255.0f );
                        listColor.addElement( b/255.0f );
                    }

                    int totalVertices = listVertex.size();
                    totalPoints = totalVertices/3;
                    float vertex[] = new float[totalVertices];
                    float color[] = new float[totalVertices];

                    for(int i=0 ; i<totalVertices; i+=3){
                        vertex[i]=listVertex.get(i);  vertex[i+1]=listVertex.get(i+1);  vertex[i+2]=listVertex.get(i+2);
                        color[i] =listColor.get(i);   color[i+1] =listColor.get(i+1);   color[i+2] =listColor.get(i+2);
                    }

                    ByteBuffer vv = ByteBuffer.allocateDirect(vertex.length * 4);
                    vv.order(ByteOrder.nativeOrder());
                    vertexBuffer = vv.asFloatBuffer();
                    //vertexBuffer = FloatBuffer.allocate(asdf.length * 4);
                    vertexBuffer.put(vertex);

                    ByteBuffer cc = ByteBuffer.allocateDirect(color.length * 4);
                    cc.order(ByteOrder.nativeOrder());
                    colorBuffer = cc.asFloatBuffer();
                    //colorBuffer = FloatBuffer.allocate(color.length * 4);
                    colorBuffer.put(color);

                    fileR.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }*/

    private void initSomePoints(){
        //String fileName = "scan000.b";
        //String fileName = "pointcloud_3d.b";
        //String fileName = "points_referencia.b";
        //String fileName = "nuvem_exemplo.b";
        File file;
        //if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
            //String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            //file = new File(baseDir + File.separator + fileName);
            file = new File(Global.file);
            if( file.isFile() ){
                try {
                    DataInputStream data = new DataInputStream(new FileInputStream(file));
                    //FileInputStream data = new FileInputStream(file);

                    int lengthFile = (int) file.length();
                    totalPoints = (lengthFile/15);

                    buffer = ByteBuffer.allocate(lengthFile);
                    //buffer.order(ByteOrder.BIG_ENDIAN);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    data.readFully( buffer.array(),0,lengthFile );

                    data.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        //}
    }

    private void loadVBO(){
        GLES20.glGenBuffers(1,VBO,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBO[0]);
        //GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,((totalPoints*3)*4 + (totalPoints*3)*4),null,GLES20.GL_STATIC_DRAW);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,((totalPoints*3)*4 + (totalPoints*3)),buffer,GLES20.GL_STATIC_DRAW);

        if(GLES20.glGetError()!=GLES20.GL_OUT_OF_MEMORY) {
            //GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false, 15, 0);
            GLES20.glEnableVertexAttribArray(mPosition);
            //GLES20.glVertexAttribPointer(mColor, 3, GLES20.GL_UNSIGNED_BYTE, true, 15, 12);
            GLES20.glEnableVertexAttribArray(mColor);
        }else{
            GLES20.glDeleteBuffers(1,VBO,0);
            Log.e(TAG,"No memory!!");
            System.exit(1);
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
    }

    public void draw(boolean mvpModified,float []mvpMatrix){
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBO[0]);
        GLES20.glUseProgram(mProgram);

        GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false, 15, 0);
        GLES20.glVertexAttribPointer(mColor, 3, GLES20.GL_UNSIGNED_BYTE, true, 15, 12);
        if( mvpModified )
            GLES20.glUniformMatrix4fv(mMVPMatrix, 1, false, mvpMatrix, 0);
        Log.d(TAG,"Error1 = "+GLES20.glGetError());
        GLES20.glDrawArrays(GLES20.GL_POINTS,0,totalPoints);
        Log.d(TAG,"Error = "+GLES20.glGetError());  //@TODO: está dando GL_INVALID_OPERATION aqui
        // em glDrawArrays e esta impossibilitando de renderizar os pontos.

        GLES20.glUseProgram(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        GLES20.glFlush();
        //ad
    }


    private class MediumPoint extends Thread {
        private static final String TAG = "MediumPoint";

        public MediumPoint(){}

        @Override
        public void run(){
            VectorFloat maxPoint = new VectorFloat(3);
            VectorFloat minPoint = new VectorFloat(3);
            float x, y, z;
            maxPoint.setX(buffer.getFloat(0));
            maxPoint.setY(buffer.getFloat(4));
            maxPoint.setZ(buffer.getFloat(8));
            minPoint.setX(maxPoint.getX());
            minPoint.setY(maxPoint.getY());
            minPoint.setZ(maxPoint.getZ());
            for(int i=1; i<totalPoints; i++){
                x = buffer.getFloat( i*15 );
                y = buffer.getFloat((i*15)+4);
                z = buffer.getFloat((i*15)+8);
                if( x>maxPoint.getX() ) maxPoint.setX(x);
                if( y>maxPoint.getY() ) maxPoint.setY(y);
                if( z>maxPoint.getZ() ) maxPoint.setZ(z);
                if( x<minPoint.getX() ) minPoint.setX(x);
                if( y<minPoint.getY() ) minPoint.setY(y);
                if( z<minPoint.getZ() ) minPoint.setZ(z);
            }
            mediumPoint.setX( (maxPoint.getX() + minPoint.getX())*0.5f );
            mediumPoint.setY( (maxPoint.getY() + minPoint.getY())*0.5f );
            mediumPoint.setZ( (maxPoint.getZ() + minPoint.getZ())*0.5f );
            synchronized (this) {
                mediumPointCalculated = true;
                notify();
            }
            //Log.d(TAG,"Thread Finish!!");
            //System.out.println("X = "+maxPoint.getX()+";  Y = "+maxPoint.getY()+";  Z = "+maxPoint.getZ());
            //System.out.println("X = "+minPoint.getX()+";  Y = "+minPoint.getY()+";  Z = "+minPoint.getZ());
            //System.out.println("X = "+mediumPoint.getX()+";  Y = "+mediumPoint.getY()+";  Z = "+mediumPoint.getZ());
        }

    }


}
