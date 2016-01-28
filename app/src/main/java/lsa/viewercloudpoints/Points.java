package lsa.viewercloudpoints;

/**
 * Created by Luan Sala on 09/02/2015.
 */

import android.opengl.GLES20;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lsa.viewercloudpoints.math.VectorFloat;

public class Points {
    private static final String TAG = "Points";

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "invariant gl_Position;" +
            "attribute vec4 vPosition;" +
            "attribute vec3 vColor;" +
            "varying vec4 fColor;" +
            "void main() {" +
            "  gl_PointSize = float(3);" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  fColor = vec4(vColor,float(1));" +
            "}";

    private final String fragmentShaderCode =
            "precision lowp float;" +
            "varying vec4 fColor;" +
            "void main() {" +
            "  gl_FragColor = fColor;" +
            "}";

    private int mProgram;
    private int mPosition;
    private int mColor;
    private int mMVPMatrix;

    private int totalPoints;
    private boolean isEnabled;

    private MediumPoint mMediumPointThread;
    private VectorFloat mediumPoint = new VectorFloat(3);
    private float zoomNeeded;

    private int VBO[] = new int[1];
    //private FloatBuffer vertexBuffer;
    //private ByteBuffer colorBuffer;

    private boolean mPointCloudWithColor;
    private float[] mRGB = new float[3];
    private ByteBuffer buffer;
    private ByteBuffer vertexBuffer;
    private ByteBuffer colorBuffer;
    private int sizeBuffer;

    //Variavel para verificar se houve GL_INVALID_OPERATION na hora de renderizar na tela.
    //Se ocorreu o erro, o VBO sera montado como "estrutura de arrays" (buffers separados).
    private boolean problemBufferInterleaved = false;
    private boolean mediumPointCalculated = false;

    public Points(){
        mRGB[0] = 0.8f; mRGB[1] = 0.2f; mRGB[2] = 0.2f;
        // prepare shaders and OpenGL program
        int vertexShader = ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        mProgram = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader);

        mPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColor    = GLES20.glGetAttribLocation(mProgram, "vColor");
        mMVPMatrix= GLES20.glGetUniformLocation(mProgram,"uMVPMatrix");

        initSomePoints();
        loadVBO();

        mMediumPointThread = new MediumPoint();
        mMediumPointThread.start();

        enable();
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

    public float getZoomNeeded(){
        return zoomNeeded;
    }

    public boolean isEnabled(){
        return isEnabled;
    }

    public void enable(){
        isEnabled = true;
    }

    public void disable(){
        isEnabled = false;
    }

    public void update(){
        if(buffer!=null) {
            buffer.limit(0);
            buffer = null;
        }
        initSomePoints();
        mMediumPointThread = new MediumPoint();
        mMediumPointThread.start();
        if( problemBufferInterleaved ) {
            vertexBuffer.limit(0);
            vertexBuffer = null;
            colorBuffer.limit(0);
            colorBuffer = null;
            separateBuffers();
            loadVBO2();
        } else {
            GLES20.glDeleteBuffers(VBO.length, VBO, 0);
            VBO = new int[1];
            loadVBO();
        }
        enable();
    }

    private void initSomePoints(){
        File file;
        //if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
            //String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            //file = new File(baseDir + File.separator + fileName);
            file = new File(Global.file);
            if( file.isFile() ){
                try {
                    //DataInputStream data = new DataInputStream(new FileInputStream(file));
                    FileInputStream data = new FileInputStream(file);
                    String magicNumber;
                    byte[] magicNumberByte = new byte[4];

                    int lengthFile = (int) (file.length()-5);
                    sizeBuffer = lengthFile;

                    data.read(magicNumberByte,0,4);
                    magicNumber = new String(magicNumberByte);
                    if(magicNumber.equals("PCl2")) {
                        //Log.d(TAG,"Magic Number = PCL2");
                        mPointCloudWithColor = true;
                        totalPoints = (lengthFile/15);
                    }else {
                        //Log.d(TAG,"Magic Number = PCL1");
                        mPointCloudWithColor = false;
                        problemBufferInterleaved = false;
                        totalPoints = (lengthFile/12);
                    }
                    System.gc();

                    buffer = ByteBuffer.allocate(lengthFile);
                    //buffer.order(ByteOrder.BIG_ENDIAN);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    buffer.position(0);
                    data.skip(1); //pula o '\n' depois do número mágico
                    data.read(buffer.array(),0,lengthFile);
                    //data.readFully( buffer.array(),0,lengthFile );

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
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,sizeBuffer,buffer.position(0),GLES20.GL_STATIC_DRAW);

        if(GLES20.glGetError()==GLES20.GL_OUT_OF_MEMORY) {
            GLES20.glDeleteBuffers(1,VBO,0);
            Log.e(TAG, "No memory!!");
            buffer.limit(0);
            buffer = null;
            System.gc();
            //Toast.makeText(Global.getContext(),"No Memory!!",Toast.LENGTH_LONG).show();
            System.exit(1);
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
    }

    //Deleta o antigo VBO e carrega 2 VBO separados. Um para o buffer de vertices e o outro para
    // o buffer de cor.
    // Esse metodo so e chamado caso tenha ocorrido GL_INVALID_OPERATION na chamada do comando
    // glDrawArrays no metodo draw().
    private void loadVBO2(){
        if(problemBufferInterleaved) {
            GLES20.glDeleteBuffers(VBO.length, VBO, 0);
            VBO = new int[2];
            GLES20.glGenBuffers(2, VBO, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBO[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.array().length,
                    vertexBuffer.position(0), GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBO[1]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.array().length,
                    colorBuffer.position(0), GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            buffer.limit(0);
            buffer = null;
        }
    }

    // Separa o buffer (variavel byte[] buffer) em 2 buffers, colocando em um buffer os vertices
    // (vertexBuffer) e em outro buffer as cores (colorBuffer).
    private void separateBuffers(){
        if(problemBufferInterleaved) {
            System.gc();
            vertexBuffer = ByteBuffer.allocate(totalPoints * 3 * 4).order(ByteOrder.LITTLE_ENDIAN);
            colorBuffer = ByteBuffer.allocate(totalPoints * 3).order(ByteOrder.LITTLE_ENDIAN);
            vertexBuffer.position(0);
            colorBuffer.position(0);
            for (int i = 0; i < (totalPoints - 1); i++) {
                vertexBuffer.putFloat(buffer.getFloat(i * 15));
                vertexBuffer.putFloat(buffer.getFloat((i * 15) + 4));
                vertexBuffer.putFloat(buffer.getFloat((i * 15) + 8));
                colorBuffer.put(buffer.get((i * 15) + 12));
                colorBuffer.put(buffer.get((i * 15) + 13));
                colorBuffer.put(buffer.get((i * 15) + 14));
            }
        }
    }

    public void draw(boolean mvpModified,float []mvpMatrix){
        GLES20.glUseProgram(mProgram);
        if( mvpModified )
            GLES20.glUniformMatrix4fv(mMVPMatrix, 1, false, mvpMatrix, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBO[0]);
        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glEnableVertexAttribArray(mColor);
        if(problemBufferInterleaved) {
            GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false, 0, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBO[1]);
            GLES20.glVertexAttribPointer(mColor, 3,GLES20.GL_UNSIGNED_BYTE,true,0,0);
        }else{
            // Descobri que da erro no DrawArrays por causa de alguma
            // coisa que ocorre aqui no VertexAttribPointer. Não reconhece o stride '15', e tambem
            // nao reconhece stride que nao e multiplo de 4.
            if(mPointCloudWithColor) {
                GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false, 15, 0);
                GLES20.glVertexAttribPointer(mColor, 3, GLES20.GL_UNSIGNED_BYTE, true, 15, 12);
            }else {
                GLES20.glDisableVertexAttribArray(mColor);
                //Nao ha cor no arquivo escolhido, consequentemente, o buffer dos vertices
                // sao diretos. Ha apenas 1 buffer para vertices. Nao ha "stride" no buffer.
                GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false, 0, 0);
                //GLES20.glVertexAttribPointer(mColor, 3, GLES20.GL_UNSIGNED_BYTE, true, 0, 0);
                GLES20.glVertexAttrib3f(mColor, mRGB[0], mRGB[1], mRGB[2]);
            }
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        GLES20.glDrawArrays(GLES20.GL_POINTS,0,totalPoints);
        if(!problemBufferInterleaved && GLES20.glGetError()==GLES20.GL_INVALID_OPERATION){
            //Log.d(TAG,"Problem in buffer");
            problemBufferInterleaved = true;
            separateBuffers();
            loadVBO2();
            draw(mvpModified,mvpMatrix);
        }
        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(mColor);
        GLES20.glUseProgram(0);

        GLES20.glFlush();
    }


    private class MediumPoint extends Thread {
        private static final String TAG = "MediumPoint";

        public MediumPoint(){}

        @Override
        public void run(){
            VectorFloat maxPoint = new VectorFloat(3);
            VectorFloat minPoint = new VectorFloat(3);
            maxPoint.setX(buffer.getFloat(0));
            maxPoint.setY(buffer.getFloat(4));
            maxPoint.setZ(buffer.getFloat(8));
            minPoint.setX(maxPoint.getX());
            minPoint.setY(maxPoint.getY());
            minPoint.setZ(maxPoint.getZ());

            // Esse try/catch aqui está pois uma vez o aplicativo deu crash sem explicação.
            // Deu a exceção NullPointerException na 4ª linha do método cloudWithColor.
            try{
                if(mPointCloudWithColor)
                    cloudWithColor(maxPoint,minPoint);
                else
                    cloudWithoutColor(maxPoint,minPoint);
            }catch (NullPointerException e){
                initSomePoints();
                run();
            }

            mediumPoint.setX( (maxPoint.getX() + minPoint.getX())*0.5f );
            mediumPoint.setY( (maxPoint.getY() + minPoint.getY())*0.5f );
            mediumPoint.setZ( (maxPoint.getZ() + minPoint.getZ())*0.5f );

            byte option = 0x00;
            float maxP_X = Math.abs(maxPoint.getX()), minP_X = Math.abs(minPoint.getX());
            float maxP_Y = Math.abs(maxPoint.getY()), minP_Y = Math.abs(minPoint.getY());
            if (maxP_X>minP_X && maxP_X>maxP_Y && maxP_X>minP_Y)
                option = 0x01;
            else if (minP_X>maxP_X && minP_X>maxP_Y && minP_X>minP_Y)
                option = 0x02;
            else if (maxP_Y>maxP_X && maxP_Y>minP_Y && maxP_Y>minP_Y)
                option = 0x04;
            else option = 0x08;

            if ( (option&0x01)!=0 )
                zoomNeeded = -(maxP_X+mediumPoint.norm());
            else if ( (option&0x02)!=0 )
                zoomNeeded = -(minP_X+mediumPoint.norm());
            else if ( (option&0x04)!=0 )
                zoomNeeded = -(maxP_Y+mediumPoint.norm());
            else
                zoomNeeded = -(minP_Y+mediumPoint.norm());

            synchronized (this) {
                mediumPointCalculated = true;
                notify();
            }
        }

        private void cloudWithColor(VectorFloat maxPoint, VectorFloat minPoint)
                throws NullPointerException {
            float x,y,z;
            for(int i=1; i<totalPoints; i++){
                x = buffer.getFloat(i * 15);
                y = buffer.getFloat((i * 15) + 4);
                z = buffer.getFloat((i * 15) + 8);

                if( x>maxPoint.getX() ) maxPoint.setX(x);
                if( y>maxPoint.getY() ) maxPoint.setY(y);
                if( z>maxPoint.getZ() ) maxPoint.setZ(z);
                if( x<minPoint.getX() ) minPoint.setX(x);
                if( y<minPoint.getY() ) minPoint.setY(y);
                if( z<minPoint.getZ() ) minPoint.setZ(z);
            }
        }

        private void cloudWithoutColor(VectorFloat maxPoint, VectorFloat minPoint)
                throws NullPointerException {
            float x,y,z;
            for(int i=1; i<totalPoints; i++){
                x = buffer.getFloat(i * 12);
                y = buffer.getFloat((i * 12) + 4);
                z = buffer.getFloat((i * 12) + 8);

                if( x>maxPoint.getX() ) maxPoint.setX(x);
                if( y>maxPoint.getY() ) maxPoint.setY(y);
                if( z>maxPoint.getZ() ) maxPoint.setZ(z);
                if( x<minPoint.getX() ) minPoint.setX(x);
                if( y<minPoint.getY() ) minPoint.setY(y);
                if( z<minPoint.getZ() ) minPoint.setZ(z);
            }
        }

    }


}
