package lsa.viewercloudpoints;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Luan Sala on 06/01/2016.
 */
public class AxisTrackball {
    private static final String TAG = "AxisTrackball";

    private final String vertexShaderCode =
            "uniform mat4 uMVPmatrix;" +
            "attribute vec3 vertexPosition;" +
            "attribute vec3 vertexColor;" +
            "varying vec4 fragmentColor;" +
            "void main() {" +
            "  gl_Position = uMVPmatrix * vec4(vertexPosition,float(1));" +
            "  fragmentColor = vec4(vertexColor,float(1));" +
            "}";

    private final String fragmentShaderCode =
            "varying mediump vec4 fragmentColor;" +
            "void main() {" +
            "  gl_FragColor = fragmentColor;" +
            "}";


    public AxisTrackball() {
        vertexBufferPosition = ByteBuffer.allocate(bufferPosition.length * 4).order(ByteOrder.nativeOrder());
        for( float aux : bufferPosition ){
            vertexBufferPosition.putFloat(aux);
        }
        vertexBufferPosition.position(0);
        vertexBufferColor = ByteBuffer.allocate(bufferPosition.length).order(ByteOrder.nativeOrder());
        vertexBufferColor.put(bufferColor);
        vertexBufferColor.position(0);

        mProgram = ShaderHelper.createAndLinkProgram(
                ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode),
                ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode)
        );

        vPosition  = GLES20.glGetAttribLocation(mProgram, "vertexPosition");
        vColor     = GLES20.glGetAttribLocation(mProgram, "vertexColor");
        uMVPmatrix = GLES20.glGetUniformLocation(mProgram, "uMVPmatrix");

        generateBuffers();
    }

    private void generateBuffers() {
        GLES20.glGenBuffers(2, BO, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, BO[VERTEX_BUFFER]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferPosition.length * 4, vertexBufferPosition, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, BO[COLOR_BUFFER]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBufferColor.array().length, vertexBufferColor, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void updateProjectionMatrix(float[] projectionMatrix) {
        System.arraycopy(projectionMatrix, 0, this.projectionMatrix, 0, projectionMatrix.length);
    }

    public void draw(float[] modelviewMatrix) {
        modelviewMatrix[12] = modelviewMatrix[13] = 0f;
        modelviewMatrix[14] = -15f;
        modelviewMatrix[15] = 1f;
        Matrix.multiplyMM(MVPMatrix,0,projectionMatrix,0,modelviewMatrix,0);

        GLES20.glLineWidth(lineWidth);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(uMVPmatrix, 1, false, MVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glEnableVertexAttribArray(vColor);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, BO[VERTEX_BUFFER]);
        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, BO[COLOR_BUFFER]);
        GLES20.glVertexAttribPointer(vColor,3,GLES20.GL_UNSIGNED_BYTE,false,0,0);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 6);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glUseProgram(0);
        GLES20.glFlush();
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void press() {
        lineWidth = 4f;
    }

    public void unPress() {
        lineWidth = 2f;
    }

    private boolean enabled = true;

    private static final byte VERTEX_BUFFER = 0;
    private static final byte COLOR_BUFFER = 1;

    private float lineWidth = 2f;

    /**
     * BUFFER OBJECT
     */
    private int BO[] = new int[2];

    private float[] bufferPosition = {
            -3f,0f,0f,   3f,0f,0f,
            0f,-3f,0f,   0f,3f,0f,
            0f,0f,-3f,   0f,0f,3f
    };

    private byte[] bufferColor = {
            (byte) 11111111, 0, 0,  (byte) 11111111, 0, 0,
            0, (byte) 11111111, 0,  0, (byte) 11111111, 0,
            0, 0, (byte) 11111111,  0, 0, (byte) 11111111
    };

    private ByteBuffer vertexBufferPosition;
    private ByteBuffer vertexBufferColor;

    private float[] projectionMatrix = new float[16];
    private float[] MVPMatrix        = new float[16];

    private int mProgram;
    private int vPosition;
    private int vColor;
    private int uMVPmatrix;

}
