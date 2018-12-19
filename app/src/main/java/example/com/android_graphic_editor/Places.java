package example.com.android_graphic_editor;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class Places
{
    public static File getScreenshotFolder()
    {
        File path = new File(Environment.getExternalStorageDirectory(),
                "/Graphic Editor/");
        path.mkdirs();

        return path;
    }

    public static File getCameraTempFolder()
    {
        File path = new File(Environment.getExternalStorageDirectory(),
                "/Graphic Editor/");
        path.mkdirs();

        File noScanning = new File(path, ".nomedia");

        if (!noScanning.exists())
        {
            try
            {
                noScanning.createNewFile();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return path;
    }

    public static File getCameraTempFile()
    {
        return new File(getCameraTempFolder(), "temp.jpg");
    }
}

