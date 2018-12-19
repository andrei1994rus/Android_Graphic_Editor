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

public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener
{
    static final int DEFAULT_SIZE=5;

    final int WRITE_REQUEST=10;

    private Paint mPaint;

    private MaskFilter mEmboss;

    private MaskFilter mBlur;

    private DashPathEffect mDashPathEffect;

    private DiscretePathEffect mDiscretePathEffect;

    DrawView drawView;

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
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
            {
                start();
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
            start();
        }
    }

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
                        start();
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

    private void start()
    {
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(DEFAULT_SIZE);

        mEmboss=new EmbossMaskFilter(new float[]
                {
                        1, 1, 1
                },
                0.4f,
                6,
                3.5f);

        mBlur=new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
    }

    public class DrawView extends View
    {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;

        public DrawView(Context c)
        {
            super(c);

            mPath=new Path();
            setFocusable(true);
            setFocusableInTouchMode(true);

            float[] intervals=new float[] { 60.0f, 10.0f, 5.0f, 10.5f };

            float phase=0;

            mDashPathEffect=new DashPathEffect(intervals, phase);

            mDiscretePathEffect=new DiscretePathEffect(10, 5);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh)
        {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas=new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawColor(Color.WHITE);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mPath, mPaint);

        }

        private float mX, mY;

        private static final float TOUCH_TOLERANCE=4;

        private void touch_start(float x, float y)
        {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y)
        {
            boolean draw=true;
            float dx=Math.abs(x-mX);
            float dy=Math.abs(y-mY);

            if (dx>=TOUCH_TOLERANCE ||
                    dy>=TOUCH_TOLERANCE)
            {
                mPath.quadTo(mX,
                            mY,
                            (x + mX)/2,
                            (y + mY)/2);
                mX = x;
                mY = y;

            }
        }

        private void touch_up()
        {
            mPath.lineTo(mX, mY);

            mCanvas.drawPath(mPath, mPaint);

            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float x = event.getX();
            float y = event.getY();

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

    public void colorChanged(int color)
    {
        mPaint.setColor(color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.Color_Draw:
                new ColorPickerDialog(this,
                                        this,
                                        mPaint.getColor()).show();
                return true;

            case R.id.Size:
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
                        mPaint.setStrokeWidth(progress);

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

            case R.id.Save:
                takeScreenshot(true);

                break;

            case R.id.Emboss:
                mPaint.setXfermode(null);
                mPaint.setMaskFilter(mEmboss);

                return true;

            case R.id.Blur:
                mPaint.setXfermode(null);
                mPaint.setMaskFilter(mBlur);

                return true;

            case R.id.No_Effects:
                mPaint.setPathEffect(null);
                mPaint.setXfermode(null);

                return true;

            case R.id.DashPathEffect:
                mPaint.setPathEffect(mDashPathEffect);
                mPaint.setXfermode(null);

                return true;

            case R.id.DiscretePathEffect:
                mPaint.setPathEffect(mDiscretePathEffect);
                mPaint.setXfermode(null);

                return true;

            case R.id.Usual_Line:
                mPaint.setMaskFilter(null);
                mPaint.setXfermode(null);

                return true;

            case R.id.Eraser:
                mPaint.setMaskFilter(null);
                mPaint.setPathEffect(null);
                mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

                return true;

            case R.id.Clear:
                setContentView(new DrawView(this));
                Toast.makeText(getApplicationContext(), "All is cleared", Toast.LENGTH_LONG).show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private File takeScreenshot(boolean showToast)
    {
        View screenView=drawView;
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
            if (showToast)
            {
                Toast.makeText(getApplicationContext(),"Saved:"+file.getAbsolutePath(),Toast.LENGTH_LONG).show();
            }

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
                                takeScreenshot(true);
                                MainActivity.super.onBackPressed();
                            }
                        }).create().show();
    }
}
