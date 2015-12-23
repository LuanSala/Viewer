
package lsa.viewercloudpoints;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Luan Sala on 21/12/2015.
 */
public class Background {
    public final static String TAG = "Background";

    private final static byte VERTEX_BUFFER = 0;
    private final static byte ELEMENT_BUFFER = 1;
    //Profundidade em que o Background estará localizado com relação ao eixo 'z'.
    private final static float Z_PLANE = -19000f;

    //Variável utilizada para armazenar a posição e a cor de cada
    // canto do quadrado do plano de fundo.
    private float[] buffer = {
            //----VERTEX----     -----COLOR-----
            0.0f, 0.0f, 0.0f,    0.2f, 0.2f, 0.7f,
            0.0f, 0.0f, 0.0f,    0.2f, 0.2f, 0.7f,
            0.0f, 0.0f, 0.0f,    0.1f, 0.1f, 0.2f,
            0.0f, 0.0f, 0.0f,    0.1f, 0.1f, 0.2f
    };
    //Indices que são utilizados para fazer a renderização do retângulo do background.
    private byte[] indexs = {
            0,1,2,
            0,2,3
    };

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec3 vertexPosition;" +
            "attribute vec3 vertexColor;" +
            "varying   vec4 fragmentColor;" +
            "void main() {" +
            "   vec4 pos = vec4(vertexPosition,float(1));" +
            "   gl_Position = uMVPMatrix * pos;" +
            "   fragmentColor = vec4(vertexColor,float(1));" +
            "}";

    private final String fragmentShaderCode =
            "varying mediump vec4 fragmentColor;" +
            "void main() {" +
            "   gl_FragColor = fragmentColor;" +
            "}";

    public Background(){
        ByteBuffer temporary = ByteBuffer.allocateDirect(buffer.length * 4);
        temporary.order(ByteOrder.nativeOrder());
        bufferPositionColor = temporary.asFloatBuffer();
        bufferPositionColor.put(buffer);
        bufferPositionColor.position(0);

        indexBuffer = ByteBuffer.allocate(indexs.length);
        indexBuffer.put(indexs);
        indexBuffer.position(0);

        mProgram = ShaderHelper.createAndLinkProgram(
                ShaderHelper.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode),
                ShaderHelper.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode)
        );

        mPosition = GLES20.glGetAttribLocation(mProgram, "vertexPosition");
        mColor    = GLES20.glGetAttribLocation(mProgram, "vertexColor");
        mMVPMatrix= GLES20.glGetUniformLocation(mProgram,"uMVPMatrix");

        generateBuffers();
    }

    private void generateBuffers(){
        GLES20.glGenBuffers(2,BO,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, BO[ELEMENT_BUFFER]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexs.length, indexBuffer,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, BO[VERTEX_BUFFER]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,(buffer.length)*4,bufferPositionColor,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void update(float[] projectionMatrix){
        //Log.d(TAG,"update");
        System.arraycopy(projectionMatrix,0,matrix,0,projectionMatrix.length);
        System.arraycopy(projectionMatrix,0,this.projectionMatrix,0,projectionMatrix.length);

        if(!invert(matrix))
            Log.e(TAG,"Algo errado no cálculo da matriz inversa para o Background!");

        float vect[] = new float[3];
        float w = projectionMatrix[14]*Z_PLANE + projectionMatrix[15];
        vect[0] = matrix[0]*Z_PLANE + matrix[1]*Z_PLANE + matrix[2]*-Z_PLANE + matrix[3]*w;
        vect[1] = matrix[4]*Z_PLANE + matrix[5]*Z_PLANE + matrix[6]*-Z_PLANE + matrix[7]*w;
        vect[2] = matrix[8]*Z_PLANE + matrix[9]*Z_PLANE + matrix[10]*-Z_PLANE + matrix[11]*w;
        //vect[3] = matrix[12]*Z_PLANE + matrix[13]*Z_PLANE + matrix[14]*-Z_PLANE + matrix[15]*w;

        //Formação do retângulo do background, sendo vect[0]=X, vect[1]=Y e vect[2]=Z.
        buffer[0] =  vect[0];   buffer[1] =  vect[1];   buffer[2] = vect[2];
        buffer[6] = -vect[0];   buffer[7] =  vect[1];   buffer[8] = vect[2];
        buffer[12]= -vect[0];   buffer[13]= -vect[1];   buffer[14]= vect[2];
        buffer[18]=  vect[0];   buffer[19]= -vect[1];   buffer[20]= vect[2];

        bufferPositionColor.position(0);
        bufferPositionColor.put(buffer);
        bufferPositionColor.position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,BO[VERTEX_BUFFER]);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0,buffer.length*4,bufferPositionColor);

        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(mMVPMatrix, 1, false, this.projectionMatrix, 0);
        GLES20.glUseProgram(0);
    }

    public void draw(){
        //Log.d(TAG,"draw");
        GLES20.glUseProgram(mProgram);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, BO[ELEMENT_BUFFER]);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,BO[VERTEX_BUFFER]);
        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glEnableVertexAttribArray(mColor);
        GLES20.glVertexAttribPointer(mPosition,3,GLES20.GL_FLOAT,false,24,0);
        GLES20.glVertexAttribPointer(mColor,3, GLES20.GL_FLOAT, false,24,12);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,6,GLES20.GL_UNSIGNED_BYTE,0);
        GLES20.glFlush();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glUseProgram(0);
    }

    /*
     * Mesa 3-D graphics library
     *
     * Copyright (C) 1999-2005  Brian Paul   All Rights Reserved.
     *
     * Permission is hereby granted, free of charge, to any person obtaining a
     * copy of this software and associated documentation files (the "Software"),
     * to deal in the Software without restriction, including without limitation
     * the rights to use, copy, modify, merge, publish, distribute, sublicense,
     * and/or sell copies of the Software, and to permit persons to whom the
     * Software is furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included
     * in all copies or substantial portions of the Software.
     ******************************************************************************
     * Compute inverse of 4x4 transformation matrix.
     *
     * \param mat pointer to a GLmatrix structure. The matrix inverse will be
     * stored in the GLmatrix::inv attribute.
     *
     * \return GL_TRUE for success, GL_FALSE for failure (\p singular matrix).
     *
     * \author
     * Code contributed by Jacques Leroy jle@star.be
     *
     * Calculates the inverse matrix by performing the gaussian matrix reduction
     * with partial pivoting followed by back/substitution with the loops manually
     * unrolled.
     */
     private static boolean invert(float mat[]){
        float[][] wtmp = new float[4][8];
        float m0, m1, m2, m3, s;
        float r0[], r1[], r2[], r3[];
        float[] temp;

        r0 = wtmp[0]; r1 = wtmp[1]; r2 = wtmp[2]; r3 = wtmp[3];

        r0[0] = mat[(0<<2)+0]; r0[1] = mat[(1<<2)+0];
        r0[2] = mat[(2<<2)+0]; r0[3] = mat[(3<<2)+0];
        r0[4] = 1.f; r0[5] = r0[6] = r0[7] = 0.f;

        r1[0] = mat[(0<<2)+1]; r1[1] = mat[(1<<2)+1];
        r1[2] = mat[(2<<2)+1]; r1[3] = mat[(3<<2)+1];
        r1[5] = 1.f; r1[4] = r1[6] = r1[7] = 0.f;

        r2[0] = mat[(0<<2)+2]; r2[1] = mat[(1<<2)+2];
        r2[2] = mat[(2<<2)+2]; r2[3] = mat[(3<<2)+2];
        r2[6] = 1.f; r2[4] = r2[5] = r2[7] = 0.f;

        r3[0] = mat[(0<<2)+3]; r3[1] = mat[(1<<2)+3];
        r3[2] = mat[(2<<2)+3]; r3[3] = mat[(3<<2)+3];
        r3[7] = 1.f; r3[4] = r3[5] = r3[6] = 0.f;

   /* choose pivot - or die */
        if (Math.abs(r3[0])>Math.abs(r2[0])){ temp=r3; r3=r2; r2=temp; }
        if (Math.abs(r2[0])>Math.abs(r1[0])){ temp=r2; r2=r1; r1=temp; }
        if (Math.abs(r1[0])>Math.abs(r0[0])){ temp=r1; r1=r0; r0=temp; }
        if (0.0 == r0[0])  return false;

   /* eliminate first variable     */
        m1 = r1[0]/r0[0]; m2 = r2[0]/r0[0]; m3 = r3[0]/r0[0];
        s = r0[1]; r1[1] -= m1 * s; r2[1] -= m2 * s; r3[1] -= m3 * s;
        s = r0[2]; r1[2] -= m1 * s; r2[2] -= m2 * s; r3[2] -= m3 * s;
        s = r0[3]; r1[3] -= m1 * s; r2[3] -= m2 * s; r3[3] -= m3 * s;
        s = r0[4];
        if (s != 0.0) { r1[4] -= m1 * s; r2[4] -= m2 * s; r3[4] -= m3 * s; }
        s = r0[5];
        if (s != 0.0) { r1[5] -= m1 * s; r2[5] -= m2 * s; r3[5] -= m3 * s; }
        s = r0[6];
        if (s != 0.0) { r1[6] -= m1 * s; r2[6] -= m2 * s; r3[6] -= m3 * s; }
        s = r0[7];
        if (s != 0.0) { r1[7] -= m1 * s; r2[7] -= m2 * s; r3[7] -= m3 * s; }

   /* choose pivot - or die */
        if (Math.abs(r3[1])>Math.abs(r2[1])){ temp=r3; r3=r2; r2=temp; }
        if (Math.abs(r2[1])>Math.abs(r1[1])){ temp=r2; r2=r1; r1=temp; }
        if (0.0 == r1[1])  return false;

   /* eliminate second variable */
        m2 = r2[1]/r1[1]; m3 = r3[1]/r1[1];
        r2[2] -= m2 * r1[2]; r3[2] -= m3 * r1[2];
        r2[3] -= m2 * r1[3]; r3[3] -= m3 * r1[3];
        s = r1[4]; if (0.0 != s) { r2[4] -= m2 * s; r3[4] -= m3 * s; }
        s = r1[5]; if (0.0 != s) { r2[5] -= m2 * s; r3[5] -= m3 * s; }
        s = r1[6]; if (0.0 != s) { r2[6] -= m2 * s; r3[6] -= m3 * s; }
        s = r1[7]; if (0.0 != s) { r2[7] -= m2 * s; r3[7] -= m3 * s; }

   /* choose pivot - or die */
        if (Math.abs(r3[2])>Math.abs(r2[2])){ temp=r3; r3=r2; r2=temp; }
        if (0.0 == r2[2])  return false;

   /* eliminate third variable */
        m3 = r3[2]/r2[2];
        r3[3] -= m3 * r2[3]; r3[4] -= m3 * r2[4];
        r3[5] -= m3 * r2[5]; r3[6] -= m3 * r2[6];
        r3[7] -= m3 * r2[7];

   /* last check */
        if (0.0 == r3[3]) return false;

        s = 1.0f/r3[3];              /* now back substitute row 3 */
        r3[4] *= s; r3[5] *= s; r3[6] *= s; r3[7] *= s;

        m2 = r2[3];                 /* now back substitute row 2 */
        s  = 1.0f/r2[2];
        r2[4] = s * (r2[4] - r3[4] * m2); r2[5] = s * (r2[5] - r3[5] * m2);
        r2[6] = s * (r2[6] - r3[6] * m2); r2[7] = s * (r2[7] - r3[7] * m2);
        m1 = r1[3];
        r1[4] -= r3[4] * m1; r1[5] -= r3[5] * m1;
        r1[6] -= r3[6] * m1; r1[7] -= r3[7] * m1;
        m0 = r0[3];
        r0[4] -= r3[4] * m0; r0[5] -= r3[5] * m0;
        r0[6] -= r3[6] * m0; r0[7] -= r3[7] * m0;

        m1 = r1[2];                 /* now back substitute row 1 */
        s  = 1.0f/r1[1];
        r1[4] = s * (r1[4] - r2[4] * m1); r1[5] = s * (r1[5] - r2[5] * m1);
        r1[6] = s * (r1[6] - r2[6] * m1); r1[7] = s * (r1[7] - r2[7] * m1);
        m0 = r0[2];
        r0[4] -= r2[4] * m0; r0[5] -= r2[5] * m0;
        r0[6] -= r2[6] * m0; r0[7] -= r2[7] * m0;

        m0 = r0[1];                 /* now back substitute row 0 */
        s  = 1.0f/r0[0];
        r0[4] = s * (r0[4] - r1[4] * m0); r0[5] = s * (r0[5] - r1[5] * m0);
        r0[6] = s * (r0[6] - r1[6] * m0); r0[7] = s * (r0[7] - r1[7] * m0);

        mat[(0<<2)+0] = r0[4]; mat[(1<<2)+0] = r0[5];
        mat[(2<<2)+0] = r0[6]; mat[(3<<2)+0] = r0[7];
        mat[(0<<2)+1] = r1[4]; mat[(1<<2)+1] = r1[5];
        mat[(2<<2)+1] = r1[6]; mat[(3<<2)+1] = r1[7];
        mat[(0<<2)+2] = r2[4]; mat[(1<<2)+2] = r2[5];
        mat[(2<<2)+2] = r2[6]; mat[(3<<2)+2] = r2[7];
        mat[(0<<2)+3] = r3[4]; mat[(1<<2)+3] = r3[5];
        mat[(2<<2)+3] = r3[6]; mat[(3<<2)+3] = r3[7];

        return true;
    }

    private FloatBuffer bufferPositionColor;
    private ByteBuffer indexBuffer;

    private float[] matrix = new float[16];
    private float[] projectionMatrix = new float[16];

    private int mProgram;
    private int mPosition;
    private int mColor;
    private int mMVPMatrix;

    private int[] BO = new int[2];

}
