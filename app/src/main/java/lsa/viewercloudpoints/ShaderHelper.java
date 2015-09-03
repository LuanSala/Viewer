package lsa.viewercloudpoints;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by Luan Sala on 27/02/2015.
 */
public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int isCompiled[] = new int[1];
        GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,isCompiled,0);
        if(isCompiled[0]==0) {
            printShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
        }
        return shader;
    }

    public static int createAndLinkProgram(int vertexShader, int fragmentShader){
        int program;
        program = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // create OpenGL program executables
        int isLinked[] = new int[1];

        GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,isLinked,0);
        if( isLinked[0]==0 ) {
            printProgramInfoLog(program);
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragmentShader);
        }
        GLES20.glDetachShader(program,vertexShader);
        GLES20.glDetachShader(program,fragmentShader);
        return program;
    }

    private static void printShaderInfoLog(int sh){
        String infoLog = GLES20.glGetShaderInfoLog(sh);
        Log.e(TAG, "Problem in Shader Code:\n"+infoLog);
    }

    public static void printProgramInfoLog(int pr){
        String infoLog = GLES20.glGetProgramInfoLog(pr);
        Log.e(TAG, "Problem in Shader Program:\n"+infoLog);
    }
}
