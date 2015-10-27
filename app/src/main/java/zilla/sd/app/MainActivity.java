package zilla.sd.app;

import android.app.Activity;
import android.content.Context;
import android.os.*;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    private void dosomething() {
        List<String> sds = getStoragePaths(this);
        IMountService service = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        try {
            for (String path : sds) {
                service.unmountVolume(path, true, false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getStoragePaths(Context cxt) {
        List<String> pathsList = new ArrayList<String>();
        StorageManager storageManager = (StorageManager) cxt.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
            method.setAccessible(true);
            Object result = method.invoke(storageManager);
            if (result != null && result instanceof String[]) {
                String[] pathes = (String[]) result;
                StatFs statFs;
                for (String path : pathes) {
                    if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                        statFs = new StatFs(path);
                        if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
                            pathsList.add(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            File externalFolder = Environment.getExternalStorageDirectory();
            if (externalFolder != null) {
                pathsList.add(externalFolder.getAbsolutePath());
            }
        }
        return pathsList;
    }

    @Override
    public void onClick(View v) {
        dosomething();
    }
}
