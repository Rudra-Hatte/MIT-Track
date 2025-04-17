package com.example.mittrack;

import java.util.Collections;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import ai.onnxruntime.*;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class faceDetector {

    private OrtEnvironment env;
    private OrtSession session;
    private final int inputSize = 640; // Match your YOLO export size (check your model!)

    public faceDetector(Context context) {
        try {
            env = OrtEnvironment.getEnvironment();
            InputStream is = context.getAssets().open("your_yolov8_face.onnx");
            byte[] modelBytes = new byte[is.available()];
            is.read(modelBytes);
            session = env.createSession(modelBytes, new OrtSession.SessionOptions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RectF> detectFaces(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
        FloatBuffer inputBuffer = preprocessImage(resized);
        List<RectF> faces = new ArrayList<>();

        try {
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputBuffer, new long[]{1, 3, inputSize, inputSize});
            OrtSession.Result result = session.run(Collections.singletonMap(session.getInputNames().iterator().next(), inputTensor));

            float[][] output = (float[][]) result.get(0).getValue();
            for (float[] detection : output) {
                float confidence = detection[4];
                if (confidence > 0.5) { // adjust threshold
                    float xCenter = detection[0] * bitmap.getWidth() / inputSize;
                    float yCenter = detection[1] * bitmap.getHeight() / inputSize;
                    float width = detection[2] * bitmap.getWidth() / inputSize;
                    float height = detection[3] * bitmap.getHeight() / inputSize;

                    float left = xCenter - width / 2;
                    float top = yCenter - height / 2;
                    float right = xCenter + width / 2;
                    float bottom = yCenter + height / 2;

                    faces.add(new RectF(left, top, right, bottom));
                }
            }

            inputTensor.close();
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return faces;
    }

    private FloatBuffer preprocessImage(Bitmap bitmap) {
        FloatBuffer buffer = FloatBuffer.allocate(3 * inputSize * inputSize);
        for (int y = 0; y < inputSize; y++) {
            for (int x = 0; x < inputSize; x++) {
                int pixel = bitmap.getPixel(x, y);
                buffer.put(((pixel >> 16) & 0xFF) / 255.0f);
                buffer.put(((pixel >> 8) & 0xFF) / 255.0f);
                buffer.put((pixel & 0xFF) / 255.0f);
            }
        }
        buffer.rewind();
        return buffer;
    }
}
