package example.com.android_graphic_editor;

import java.io.File;

import android.os.Environment;

/**
 * This class is used to save image.
 */
public class Places
{

    /**
     * Gets screenshot folder.
     *
     * @return path of album.
     *
     */
    static File getScreenshotFolder()
    {
        File path=new File(Environment.getExternalStorageDirectory(),
                "/Graphic Editor/");
        path.mkdirs();

        return path;
    }
}

