package example.com.android_graphic_editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class of Main Activity.
 */
public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener
{
    /**
     * Default size.
     */
    static final int DEFAULT_SIZE=5;

    /**
     * Code of request.
     */
    static final int WRITE_REQUEST=10;

    /**
     * Object of class Paint. Is line.
     */
    private Paint linePaint;

    /**
     * Object of class MaskFilter. Is filter emboss.
     */
    private MaskFilter emboss;

    /**
     * Object of class MaskFilter. Is filter blur.
     */
    private MaskFilter blur;

    /**
     * Object of class DashPathEffect. Is dot dash.
     */
    private DashPathEffect dotDash;

    /**
     * Object of class DiscretePathEffect. Is broken straight line.
     */
    private DiscretePathEffect brokenStraightLine;

    /**
     * Object of class DrawView. Is used for drawing.
     */
    DrawView drawView;

    /**
     * Object of class View. Is used to save image.
     */
    View screenView;

    /**
     * Creates MainActivity.
     *
     * @param savedInstanceState
     *                          object of class Bundle. Is saved instance state of activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        drawView=new DrawView(this);
        setContentView(drawView);

        int api=Build.VERSION.SDK_INT;

        if (api>=23)
        {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
            {
                init();
            }

            else
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new  String[]
                            {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            WRITE_REQUEST);
                }

                else
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]
                            {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            WRITE_REQUEST);
                }
            }
        }

        else if (api<23)
        {
            init();
        }
    }

    /**
     * This method is used for result of requesting permissions.
     *
     * @param requestCode
     *                   code of request.
     *
     * @param permissions
     *                   permissions.
     *
     * @param grantResults
     *                    results of grant.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults)
    {
        switch (requestCode)
        {
            case WRITE_REQUEST:
            {
                if (grantResults.length>0 &&
                        grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
                        init();
                    }
                }

                else
                {
                    Toast.makeText(this,"Permission isn't granted",Toast.LENGTH_LONG).show();
                }

                return;
            }
        }
    }

    /**
     * Initialisation after check version and permissions.
     */
    private void init()
    {
        linePaint=new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(DEFAULT_SIZE);

        emboss=new EmbossMaskFilter(new float[]
                                    {
                                            1, 1, 1
                                    },
                                    0.4f,
                                    6,
                                    3.5f);

        blur=new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
    }

    /**
     * Class of DrawView. Is used for drawing.
     */
    public class DrawView extends View
    {
        /**
         * Object of class Bitmap.
         */
        private Bitmap bitmap;

        /**
         * Object of class Canvas. Is used for drawing.
         */
        private Canvas canvas;

        /**
         * Object of class Path. Is used to select PathEffect.
         */
        private Path path;

        /**
         * Object of class Paint. Is used to draw bitmap.
         */
        private Paint bitmapPaint;

        /**
         * Point X. Is used to recognize coordinate in oX and to draw.
         */
        private float x;

        /**
         * Point Y. Is used to recognize coordinate in oY and to draw.
         */
        private float y;

        /**
         * Touch tolerance. Is difference between old point X and new point X and between old point Y and new point Y.
         */
        private static final float TOUCH_TOLERANCE=4;

        /**
         * Constructor of class DrawView.
         *
         * @param context
         *               object of class Context. Is used to create view where we shall be to draw.
         */
        public DrawView(Context context)
        {
            super(context);

            path=new Path();
            setFocusable(true);
            setFocusableInTouchMode(true);

            float[] intervals=new float[] { 60.0f, 10.0f, 5.0f, 10.5f };

            float phase=0;

            dotDash=new DashPathEffect(intervals, phase);

            brokenStraightLine=new DiscretePathEffect(10, 5);
        }

        /**
         * Changes size of Bitmap.
         *
         * @param w
         *         width.
         *
         * @param h
         *         height.
         *
         * @param oldW
         *            old width.
         *
         * @param oldH
         *            old height.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldW, int oldH)
        {
            super.onSizeChanged(w, h, oldW, oldH);

            bitmap=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas=new Canvas(bitmap);
        }

        /**
         * Performs operations which are associated with drawing.
         *
         * @param canvas
         *              object of class Canvas which used to draw.
         */
        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawColor(Color.WHITE);

            canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);

            canvas.drawPath(path, linePaint);

        }

        /**
         * ACTION_DOWN happens.
         *
         * @param x
         *         point X.
         *
         * @param y
         *         point Y.
         */
        private void touch_start(float x, float y)
        {
            path.reset();
            path.moveTo(x, y);

            this.x=x;
            this.y=y;
        }

        /**
         * ACTION_MOVE happens.
         *
         * @param x
         *          point X. Is used to draw.
         *
         * @param y
         *         point Y. Is used to draw.
         */
        private void touch_move(float x, float y)
        {
            boolean draw=true;
            float dx=Math.abs(x-this.x);
            float dy=Math.abs(y-this.y);

            if (dx>=TOUCH_TOLERANCE ||
                    dy>=TOUCH_TOLERANCE)
            {
                path.quadTo(this.x,
                            this.y,
                            (x+this.x)/2,
                            (y+this.y)/2);

                this.x=x;
                this.y=y;
            }
        }

        /**
         * ACTION_UP happens.
         */
        private void touch_up()
        {
            path.lineTo(x, y);

            canvas.drawPath(path, linePaint);

            path.reset();
        }

        /**
         * Responds to touch events: ACTION_DOWN, ACTION_MOVE, ACTION_UP.
         *
         * @param event
         *             object of class MotionEvent. Recognizes action.
         *
         * @return true.
         *
         */
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float x=event.getX();
            float y=event.getY();

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();

                    break;

                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();

                    break;

                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();

                    break;
            }

            return true;
        }
    }

    /**
     * Changes color.
     *
     * @param color
     *             color of linePaint.
     *
     */
    public void colorChanged(int color)
    {
        linePaint.setColor(color);
    }

    /**
     * Creates menu.
     *
     * @param menu
     *            object of class Menu. Is used to create menu.
     *
     * @return true.
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Reaction of select of item in Menu happens.
     *
     * @param item
     *            object of class MenuItem. Recognizes selected item.
     *
     * @return true.
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.colorLine:
                new ColorPickerDialog(this,
                                        this,
                                        linePaint.getColor()).show();
                return true;

            case R.id.sizeLine:
                LayoutInflater inflater_size=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout_size=inflater_size.inflate(R.layout.size,(ViewGroup) findViewById(R.id.root));
                AlertDialog.Builder builder_size=new AlertDialog.Builder(this).setView(layout_size);
                final AlertDialog alertDialog_size=builder_size.create();
                alertDialog_size.show();

                SeekBar seekBarSize=(SeekBar) layout_size.findViewById(R.id.seekBarSize);
                seekBarSize.setProgress(DEFAULT_SIZE);

                final Button buttonSelectSize=(Button) layout_size.findViewById(R.id.buttonSelectSize);

                final TextView textViewSizeValue=(TextView) layout_size.findViewById(R.id.textViewSizeValue);
                textViewSizeValue.setText("Default size:"+DEFAULT_SIZE);

                seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {
                    public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser)
                    {
                        textViewSizeValue.setText("You selected size:"+progress);
                        linePaint.setStrokeWidth(progress);

                        buttonSelectSize.setOnClickListener(new OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                if (progress==0)
                                {
                                    Toast.makeText(getApplicationContext(),"Select size not less than 1",
                                            Toast.LENGTH_LONG).show();
                                }

                                else
                                {
                                    alertDialog_size.dismiss();
                                }
                            }
                        });
                    }

                    public void onStartTrackingTouch(SeekBar seekBar)
                    {

                    }

                    public void onStopTrackingTouch(SeekBar seekBar)
                    {

                    }
                });

                return true;

            case R.id.save:
                saveImage();

                break;

            case R.id.emboss:
                linePaint.setXfermode(null);
                linePaint.setMaskFilter(emboss);

                return true;

            case R.id.blur:
                linePaint.setXfermode(null);
                linePaint.setMaskFilter(blur);

                return true;

            case R.id.withoutEffects:
                linePaint.setPathEffect(null);
                linePaint.setXfermode(null);

                return true;

            case R.id.dashPathEffect:
                linePaint.setPathEffect(dotDash);
                linePaint.setXfermode(null);

                return true;

            case R.id.discretePathEffect:
                linePaint.setPathEffect(brokenStraightLine);
                linePaint.setXfermode(null);

                return true;

            case R.id.usualLine:
                linePaint.setMaskFilter(null);
                linePaint.setXfermode(null);

                return true;

            case R.id.eraser:
                linePaint.setMaskFilter(null);
                linePaint.setPathEffect(null);
                linePaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

                return true;

            case R.id.clear:
                drawView=new DrawView(this);
                setContentView(drawView);
                Toast.makeText(getApplicationContext(), "All is cleared", Toast.LENGTH_LONG).show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Saves image.
     *
     * @return file (if file!=null), else null.
     */
    private File saveImage()
    {
        screenView=drawView;
        screenView.setDrawingCacheEnabled(true);

        Bitmap cachedBitmap=screenView.getDrawingCache();
        Bitmap copyBitmap=cachedBitmap.copy(Bitmap.Config.RGB_565, true);

        FileOutputStream output=null;
        File file=null;

        try
        {
            File path=Places.getScreenshotFolder();
            Calendar cal=Calendar.getInstance();
            file=new File(path,
                    cal.get(Calendar.YEAR)+"_"+
                           (1 + cal.get(Calendar.MONTH))+"_"+
                            cal.get(Calendar.DAY_OF_MONTH)+"_"+
                            cal.get(Calendar.HOUR_OF_DAY)+"_"+
                            cal.get(Calendar.MINUTE)+"_"+
                            cal.get(Calendar.SECOND)+
                            ".png");

            output=new FileOutputStream(file);
            copyBitmap.compress(CompressFormat.PNG, 100, output);
            screenView.setDrawingCacheEnabled(false);
        }

        catch (FileNotFoundException e)
        {
            file=null;
            e.printStackTrace();
        }

        finally
        {
            if (output!=null)
            {
                try
                {
                    output.close();
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (file!=null)
        {
            Toast.makeText(getApplicationContext(),
                    "Saved file in path:"+
                            file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

            Intent requestScan=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

            requestScan.setData(Uri.fromFile(file));
            sendBroadcast(requestScan);


            return file;
        }

        else
        {
            return null;
        }
    }

    /**
     * This method is performed when button "Back" (on phone or in toolbar).
     * Calls AlertDialog to offer saving image.
     */
    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                        .setTitle("You exit from application")
                        .setMessage("Do you want to save image?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface arg0, int arg1)
                            {
                                MainActivity.super.onBackPressed();
                            }
                        })
                        .setNeutralButton("Cancel", null)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface arg0, int arg1)
                            {
                                saveImage();
                                MainActivity.super.onBackPressed();
                            }
                        }).create().show();
    }
}
