/*
 * Copyright 2014 Universidad Nacional de Colombia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unal.od.jdiffraction.prop;

import org.jtransforms.fft.FloatFFT_2D;
import unal.od.jdiffraction.utils.FloatArrayUtils;

/**
 *
 * @author Pablo Piedrahita-Quintero (jppiedrahitaq@unal.edu.co)
 * @author Jorge Garcia-Sucerquia (jigarcia@unal.edu.co)
 */
public class FloatFresnelFourier extends FloatPropagator {

    private final int M, N;
    private final float z, lambda, dx, dy, dxOut, dyOut;
    private final float[][] kernel, outputPhase;
    private final FloatFFT_2D fft;

    /**
     *
     * @param M Number of data points on x direction.
     * @param N Number of data points on y direction.
     * @param lambda Wavelenght
     * @param z Distance.
     * @param dx Sampling pitch on x direction.
     * @param dy Sampling pitch on y direction.
     */
    public FloatFresnelFourier(int M, int N, float lambda, float z, float dx, float dy) {
        this.M = M;
        this.N = N;
        this.lambda = lambda;
        this.dx = dx;
        this.dy = dy;
        this.z = z;
        
        dxOut = lambda * z / (M * dx);
        dyOut = lambda * z / (N * dy);

        kernel = new float[M][2 * N];
        outputPhase = new float[M][2 * N];
        fft = new FloatFFT_2D(M, N);

        calculateKernel();
    }

    private void calculateKernel() {

        int M2, N2;
        float factor, factor2, factor3, dxSq, dySq, dxOutSq, dyOutSq;

        M2 = M / 2;
        N2 = N / 2;
        
        dxOutSq = dxOut * dxOut;
        dyOutSq = dyOut * dyOut;

        dxSq = dx * dx;
        dySq = dy * dy;
        factor = (float) Math.PI / (lambda * z);
        factor2 = (float) Math.PI * 2 * z / lambda;
        factor3 = dx * dy / (lambda * z);

        for (int i = 0; i < M; i++) {
            int i2 = i - M2 + 1;
            float p1 = i2 * i2 * dxSq;
            float p2 = i2 * i2 * dxOutSq;

            for (int j = 0; j < N; j++) {
                int j2 = j - N2 + 1;
                float phase;

                phase = p1 + j2 * j2 * dySq;
                phase *= factor;
                kernel[i][2 * j] = (float) Math.cos(phase);
                kernel[i][2 * j + 1] = (float) Math.sin(phase);
                
                phase = p2 +j2 * j2 * dyOutSq;
                phase *= factor;
                outputPhase[i][2 * j] = (float) Math.sin(factor2 + phase) * factor3;
                outputPhase[i][2 * j + 1] = (float) -Math.cos(factor2 + phase) * factor3;
            }
        }

    }

    @Override
    public void diffract(float[][] field) {
        if (M != field.length || N != (field[0].length / 2)) {
            throw new IllegalArgumentException("Array dimension must be " + M + " x " + 2 * N + ".");
        }

        FloatArrayUtils.complexMultiplication2(field, kernel);
        FloatArrayUtils.complexShift(field);
        fft.complexForward(field);
        FloatArrayUtils.complexShift(field);
        FloatArrayUtils.complexMultiplication2(field, outputPhase);
    }

    public int getM() {
        return M;
    }

    public int getN() {
        return N;
    }

    public float getZ() {
        return z;
    }

    public float getLambda() {
        return lambda;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public float getDxOut() {
        return dxOut;
    }

    public float getDyOut() {
        return dyOut;
    }
    
    
    
}