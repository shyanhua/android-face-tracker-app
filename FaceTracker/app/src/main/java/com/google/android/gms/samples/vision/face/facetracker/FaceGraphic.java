/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.util.Log;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {

    public interface FaceGraphicDelegate {
        void FrontFaceVerified();
        void LeftFaceVerified();
        void RightFaceVerified();
        void SmileFaceVerified();
        void FaceVerified();
    }

    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private Boolean flagContinue = false, flagFaceRight = false, flagFaceLeft = false, flagFaceId = false, flagSmile = false;
    private Integer faceId;
    private FaceGraphicDelegate faceGraphicDelegate;

    public void setFaceGraphicDelegate(FaceGraphicDelegate faceGraphicDelegate) {
        this.faceGraphicDelegate = faceGraphicDelegate;
    }

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null)
        {
            return;
        }

        float faceY = face.getEulerY();

        //get Face ID
        if (flagFaceId == false)
        {
            faceId = mFaceId;
            flagFaceId = true;
        }

        //if face id changed then reset flag
        if (flagFaceId == true && mFaceId > faceId)
        {
            resetFlag();
        }

        //Shyan
        //detect front face
        if (flagContinue == false)
        {
            if (faceY > -12 && faceY < 12)
            {
                faceGraphicDelegate.FrontFaceVerified();
                flagContinue = true;
                return;
            }
        }

        //after front face , detect left face then right face
        if (flagContinue == true)
        {
            //left face
            if (flagFaceLeft == false)
            {
                if (faceY < -30)
                {
                    faceGraphicDelegate.LeftFaceVerified();
                    flagFaceLeft = true;
                    return;
                }
            }
            //right face
            if (flagFaceLeft == true)
            {
                if (flagFaceRight == false)
                {
                    if (faceY > 30)
                    {
                        faceGraphicDelegate.RightFaceVerified();
                        flagFaceRight = true;
                        return;
                    }
                }
            }
        }

        //detect smile after left face and right face detected
        if (flagFaceLeft == true && flagFaceRight == true)
        {
            if (flagSmile == false)
            {
                if (face.getIsSmilingProbability() > 0.6)
                {
                    faceGraphicDelegate.SmileFaceVerified();
                    flagSmile = true;
                    return;
                }
            }
        }

        //selfie after smile
        if(flagSmile == true)
        {
            //TODO : After this the screen still able to take photo
            faceGraphicDelegate.FaceVerified();
            return;
        }
    }

    private void resetFlag()
    {
        flagSmile = false;
        flagContinue = false;
        flagFaceLeft = false;
        flagFaceRight = false;
        flagFaceId = false;
    }

}
