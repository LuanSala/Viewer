package lsa.viewercloudpoints;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Luan Sala on 26/02/2015.
 */
public abstract class Texture {

    private static byte VERTEX_BUFFER  = 0;
    private static byte ELEMENT_BUFFER = 1;

    public static final String vShaderCode =
            "uniform mat4 mMVPMatrix;" +
            "attribute vec3 vPosition;" +
            "attribute vec2 vTextureCoord;" +
            "varying vec2 fTextureCoord;" +
            "void main() {" +
            "   gl_Position = mMVPMatrix * vec4(vPosition,float(1));" +
            "   fTextureCoord = vTextureCoord;" +
            "}";

    public static final String fShaderCode =
            "uniform sampler2D samplerTexture;" +
            "varying mediump vec2 fTextureCoord;" +
            "void main() {" +
            "   gl_FragColor = texture2D(samplerTexture,fTextureCoord);" +
            "}";

    protected static int shProgram;
    private static int shMVPMatrix;
    private static int shPosition;
    private static int shTextCoord;
    private static int shSampler2D;

    private static FloatBuffer vertexBuff;
    private static ByteBuffer  indexBuff;
    private FloatBuffer textureBuff;

    private int VBO[] = new int[3];

    private int textures[] = new int[2];

    private static float mProjectionMatrix[] = new float[16];

    private static float vertices[] = {
            0.0f,0.0f,0.0f,
            1.0f,0.0f,0.0f,
            1.0f,1.0f,0.0f,
            0.0f,1.0f,0.0f
    };
    private static byte indices[] = {
            0,1,2,
            0,2,3
    };
    private float textureCoord[];

    public Texture(float []texCoord) {
        // Fazer com que essa classe inicialize o programa shader apenas na primeira vez que ela
        // for instanciada.
        textureCoord = texCoord;
        ByteBuffer buff;
            int vertexShader   = ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER,vShaderCode);
            int fragmentShader = ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER,fShaderCode);
            shProgram = ShaderHelper.createAndLinkProgram(vertexShader,fragmentShader);

            shMVPMatrix = GLES20.glGetUniformLocation(shProgram,"mMVPMatrix");
            shPosition  = GLES20.glGetAttribLocation(shProgram, "vPosition");
            shTextCoord = GLES20.glGetAttribLocation(shProgram, "vTextureCoord");
            shSampler2D = GLES20.glGetUniformLocation(shProgram,"samplerTexture");

            buff = ByteBuffer.allocateDirect( (vertices.length*4)+(texCoord.length*4) );
            buff.order(ByteOrder.nativeOrder());
            vertexBuff = buff.asFloatBuffer();
            vertexBuff.put(vertices,0,3);
            vertexBuff.put(texCoord,0,2);
            vertexBuff.put(vertices,3,3);
            vertexBuff.put(texCoord,2,2);
            vertexBuff.put(vertices,6,3);
            vertexBuff.put(texCoord,4,2);
            vertexBuff.put(vertices,9,3);
            vertexBuff.put(texCoord,6,2);
            indexBuff = ByteBuffer.allocateDirect( indices.length );
            indexBuff.put(indices);
            vertexBuff.position(0);
            indexBuff.position(0);

            GLES20.glGenBuffers(2,VBO,0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBO[VERTEX_BUFFER]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,(vertices.length+texCoord.length)*4,vertexBuff,GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,VBO[ELEMENT_BUFFER]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,indices.length,indexBuff,GLES20.GL_STATIC_DRAW);
            GLES20.glEnableVertexAttribArray(shPosition);
            GLES20.glEnableVertexAttribArray(shTextCoord);

            Matrix.orthoM(mProjectionMatrix,0,0,1,0,1,-1,1);

        buff = ByteBuffer.allocateDirect( texCoord.length * 4 );
        buff.order(ByteOrder.nativeOrder());
        textureBuff = buff.asFloatBuffer();
        textureBuff.put(textureCoord);
        textureBuff.position(0);

        instances++;
    }

    public void draw(float []modelViewMatrix){
        float mMVPMatrix[] = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, modelViewMatrix, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,VBO[VERTEX_BUFFER]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,VBO[ELEMENT_BUFFER]);
        GLES20.glUseProgram(shProgram);

            GLES20.glVertexAttribPointer(shPosition, 3, GLES20.GL_FLOAT, false, 20, 0);
            GLES20.glVertexAttribPointer(shTextCoord, 2, GLES20.GL_FLOAT, false, 20, 12);

            GLES20.glUniformMatrix4fv(shMVPMatrix, 1, false, mMVPMatrix, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glUniform1i(shSampler2D, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,6,GLES20.GL_UNSIGNED_BYTE,0);

        GLES20.glUseProgram(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,0);
        GLES20.glFlush();
    }

    protected void loadTexture(int id,Context context){
        InputStream imageStream = context.getResources().openRawResource(id);
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeStream(imageStream);
        try{
            imageStream.close();
        }catch(IOException e){ e.printStackTrace(); }
        GLES20.glGenTextures(1,textures,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);

        bitmap.recycle();
    }

    public abstract void setModelViewMatrix();


    // Variavel que indica quantos objetos de textura foram criados.
    // Usado para quando o primeiro objeto ser criado, carregar o programa shader.
    private static short instances = 0;

}
