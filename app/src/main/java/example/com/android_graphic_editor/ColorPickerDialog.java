package example.com.android_graphic_editor;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 *  Class of Dialog where select color.
 */
public class ColorPickerDialog extends Dialog
{
    /**
     * This interface is used to change color.
     */
    public interface OnColorChangedListener
    {
        void colorChanged(int color);
    }

    /**
     * Object of interface OnColorChangedListener.
     */
    private OnColorChangedListener colorChangedListener;

    /**
     * color which was before calling of ColorPickerDialog.
     */
    private int initialColor;

    /**
     * This class is used to build ColorPickerDialog.
     */
    private static class ColorPickerView extends View
    {
        /**
         * Object of class Paint. Is wheel.
         */
        private Paint wheelPaint;

        /**
         * Object of class Paint. Is center.
         */
        private Paint centerPaint;

        /**
         * Colors which will be in ColorPickerDialog.
         */
        private final int[] colors;

        /**
         * Object of interface OnColorChangedListener. Is used to change color.
         */
        private OnColorChangedListener colorChangedListener;

        /**
         * Number PI.
         */
        private static final float PI=3.1415926f;

        /**
         * Boolean variable which recognizes: was center button selected or not.
         */
        private boolean trackingCenter;

        /**
         * Boolean variable which recognizes: does center button have highlight or not.
         */
        private boolean highlightCenter;

        /**
         * Center point X.
         */
        private static final int CENTER_X=100;

        /**
         * Center point Y.
         */
        private static final int CENTER_Y=100;

        /**
         * Radius of center.
         */
        private static final int CENTER_RADIUS=32;

        /**
         * Constructor of class ColorPickerView.
         *
         * @param context
         *               object of class Context.
         *
         * @param colorChangedListener
         *                            object of interface OnColorChangedListener.
         *
         * @param color
         *             color which will be selected.
         *
         */
        ColorPickerView(Context context, OnColorChangedListener colorChangedListener, int color)
        {
            super(context);
            this.colorChangedListener=colorChangedListener;
            colors=new int[]
            {
                    Color.BLACK,Color.BLUE,Color.WHITE,
                    Color.GRAY,Color.DKGRAY,Color.LTGRAY,
                    Color.CYAN,Color.GREEN,Color.MAGENTA,
                    Color.RED
            };

            Shader s=new SweepGradient(0, 0, colors, null);

            wheelPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
            wheelPaint.setShader(s);
            wheelPaint.setStyle(Paint.Style.STROKE);
            wheelPaint.setStrokeWidth(32);

            centerPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
            centerPaint.setColor(color);
            centerPaint.setStrokeWidth(5);
        }

        /**
         * Performs operations which are associated with drawing.
         *
         * @param canvas
         *              object of class Canvas which is used to draw.
         *
         */
        protected void onDraw(Canvas canvas)
        {
            float r=CENTER_X-wheelPaint.getStrokeWidth()*0.5f;

            canvas.translate(CENTER_X, CENTER_X);

            canvas.drawOval(new RectF(-r, -r, r, r), wheelPaint);
            canvas.drawCircle(0, 0, CENTER_RADIUS, centerPaint);

            if (trackingCenter)
            {
                int c=centerPaint.getColor();
                centerPaint.setStyle(Paint.Style.STROKE);

                if (highlightCenter)
                {
                    centerPaint.setAlpha(0xFF);
                }

                else
                {
                    centerPaint.setAlpha(0x80);
                }

                canvas.drawCircle(0, 0,CENTER_RADIUS+centerPaint.getStrokeWidth(), centerPaint);

                centerPaint.setStyle(Paint.Style.FILL);
                centerPaint.setColor(c);
            }
        }

        /**
         * Sets size of ColorPickerDialog.
         *
         * @param widthMeasureSpec
         *                        width which it's necessary to set.
         *
         * @param heightMeasureSpec
         *                         height which it's necessary to set.
         *
         */
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
            setMeasuredDimension(CENTER_X*2, CENTER_Y*2);
        }


        /**
         * Gets code of every color separately: red, green, blue, alpha.
         *
         * @param c0
         *          1st color separately: red, green, blue, alpha.
         *
         * @param c1
         *          2nd color separately: red, green, blue, alpha.
         *
         * @param p
         *         id of color.
         *
         * @return code of of every color separately: red, green, blue, alpha.
         *
         */
        private int ave(int c0, int c1, float p)
        {
            return c0+java.lang.Math.round(p*(c1-c0));
        }

        /**
         * Interprets color.
         *
         * @param colors
         *              array of colors which are in ColorPickerDialog.
         *
         * @param unit
         *            unit in multiple [0,1].
         *
         * @return color which was selected in wheel.
         *
         */
        private int interpretationColor(int colors[], float unit)
        {
            if (unit<=0)
            {
                return colors[0];
            }

            if (unit>=1)
            {
                return colors[colors.length-1];
            }

            float p=unit*(colors.length-1);
            int i=(int)p;
            p-=i;

            int c0=colors[i];
            int c1=colors[i+1];
            int a=ave(Color.alpha(c0), Color.alpha(c1), p);
            int r=ave(Color.red(c0), Color.red(c1), p);
            int g=ave(Color.green(c0), Color.green(c1), p);
            int b=ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        /**
         * Responds to touch events: ACTION_DOWN, ACTION_MOVE, ACTION_UP.
         *
         * @param event
         *             object of class MotionEvent. Recognizes action.
         *
         * @return true.
         */
        public boolean onTouchEvent(MotionEvent event)
        {
            float x=event.getX()-CENTER_X;
            float y=event.getY()-CENTER_Y;
            boolean inCenter=java.lang.Math.sqrt(x*x+y*y)<=CENTER_RADIUS;

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    trackingCenter=inCenter;

                    if (inCenter)
                    {
                        highlightCenter=true;
                        invalidate();

                        break;
                    }

                case MotionEvent.ACTION_MOVE:
                    if (trackingCenter)
                    {
                        if (highlightCenter!=inCenter)
                        {
                            highlightCenter=inCenter;
                            invalidate();
                        }
                    }

                    else
                    {
                        float angle=(float)java.lang.Math.atan2(y, x);
                        float unit=angle/(2*PI);

                        if (unit<0)
                        {
                            unit+=1;
                        }

                        centerPaint.setColor(interpretationColor(colors, unit));
                        invalidate();
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    if (trackingCenter)
                    {
                        if (inCenter)
                        {
                            colorChangedListener.colorChanged(centerPaint.getColor());
                        }

                        trackingCenter=false;
                        invalidate();
                    }

                    break;
            }

            return true;
        }
    }

    /**
     * Constructor of class ColorPickerDialog.
     *
     * @param context
     *               object of class Context. Is used to create ColorPickerDialog.
     *
     * @param colorChangedListener
     *                            object of interface OnColorChangedListener.
     *
     * @param initialColor
     *                    color which was before calling of ColorPickerDialog.
     *
     */
    public ColorPickerDialog(Context context,OnColorChangedListener colorChangedListener,int initialColor)
    {
        super(context);

        this.colorChangedListener=colorChangedListener;
        this.initialColor=initialColor;
    }


    /**
     * Creates ColorPickerDialog.
     *
     * @param savedInstanceState
     *                          object of class Bundle. Is saved instance state of activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        OnColorChangedListener colorChangedListener=new OnColorChangedListener()
        {
            /**
             * Changes color.
             *
             * @param color
             *             selected color.
             */
            public void colorChanged(int color)
            {
                ColorPickerDialog.this.colorChangedListener.colorChanged(color);
                dismiss();
            }
        };

        setContentView(new ColorPickerView(getContext(), colorChangedListener, initialColor));
        setTitle("Select color");
    }
}
