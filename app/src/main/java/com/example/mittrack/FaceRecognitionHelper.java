package com.example.mittrack;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.FaceDetector;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FaceRecognitionHelper {

    private Interpreter tflite;
    private final int inputSize = 112; // Expected input size for MobileFaceNet

    public FaceRecognitionHelper(Context context) {
        try {
            tflite = new Interpreter(loadModelFile(context, "mobile_face_net.tflite"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Returns a 192-dimensional embedding from a face Bitmap.
    public float[] getFaceEmbedding(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
        float[][][][] input = new float[1][inputSize][inputSize][3];

        for (int y = 0; y < inputSize; y++) {
            for (int x = 0; x < inputSize; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                float r = ((pixel >> 16) & 0xFF);
                float g = ((pixel >> 8) & 0xFF);
                float b = (pixel & 0xFF);
                input[0][y][x][0] = (r - 127.5f) / 127.5f;
                input[0][y][x][1] = (g - 127.5f) / 127.5f;
                input[0][y][x][2] = (b - 127.5f) / 127.5f;
            }
        }

        float[][] output = new float[1][192];
        tflite.run(input, output);
        return output[0];
    }

    // Returns an array of embeddings for each detected face in the bitmap.
    // If no face is detected using the multi-face detector, falls back to processing the entire image.
    public float[][] getFaceEmbeddings(Bitmap bitmap) {
        // Convert to mutable RGB_565 bitmap (required by FaceDetector)
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        int maxFaces = 10;
        FaceDetector.Face[] faces = new FaceDetector.Face[maxFaces];
        FaceDetector detector = new FaceDetector(mutableBitmap.getWidth(), mutableBitmap.getHeight(), maxFaces);
        int faceCount = detector.findFaces(mutableBitmap, faces);

        if (faceCount == 0) {
            // Fallback: use the whole image embedding
            float[] fallbackEmbedding = getFaceEmbedding(bitmap);
            return fallbackEmbedding == null ? new float[0][] : new float[][]{fallbackEmbedding};
        }

        float[][] embeddings = new float[faceCount][];
        int detectedCount = 0;

        for (int i = 0; i < faceCount; i++) {
            FaceDetector.Face face = faces[i];
            if (face == null) continue;

            PointF midPoint = new PointF();
            face.getMidPoint(midPoint);
            float eyesDistance = face.eyesDistance();

            int left = (int) Math.max(0, midPoint.x - eyesDistance * 1.5f);
            int top = (int) Math.max(0, midPoint.y - eyesDistance * 2f);
            int right = (int) Math.min(bitmap.getWidth(), midPoint.x + eyesDistance * 1.5f);
            int bottom = (int) Math.min(bitmap.getHeight(), midPoint.y + eyesDistance * 2f);

            // Ensure width and height are positive
            if (right - left <= 0 || bottom - top <= 0) continue;

            Bitmap faceBitmap = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
            Bitmap resizedFace = Bitmap.createScaledBitmap(faceBitmap, inputSize, inputSize, true);
            embeddings[detectedCount] = getFaceEmbedding(resizedFace);
            detectedCount++;
        }

        if (detectedCount == 0) {
            float[] fallbackEmbedding = getFaceEmbedding(bitmap);
            return fallbackEmbedding == null ? new float[0][] : new float[][]{fallbackEmbedding};
        }

        if (detectedCount < faceCount) {
            float[][] result = new float[detectedCount][];
            System.arraycopy(embeddings, 0, result, 0, detectedCount);
            return result;
        }

        return embeddings;
    }
}
