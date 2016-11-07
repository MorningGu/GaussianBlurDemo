package hero.gaussianapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ImageView iv_alpha;
    private ImageView iv_gaussian;
    private TextView tv_time;
    private Button btn_start;
    private TextView tv_alpha;
    private AppCompatSeekBar sb_alpha;
    private TextView tv_radius;
    private AppCompatSeekBar sb_radius;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView(){
        iv_alpha = (ImageView)findViewById(R.id.iv_alpha);
        iv_gaussian = (ImageView) findViewById(R.id.iv_gaussian);
        tv_time = (TextView)findViewById(R.id.tv_time);
        btn_start = (Button) findViewById(R.id.btn_start);
        tv_alpha = (TextView)findViewById(R.id.tv_alpha);
        sb_alpha = (AppCompatSeekBar)findViewById(R.id.sb_alpha);
        tv_radius = (TextView)findViewById(R.id.tv_radius);
        sb_radius = (AppCompatSeekBar)findViewById(R.id.sb_radius);
        iv_alpha.setImageResource(R.drawable.bg_1);
        sb_alpha.setProgress(255);
        sb_radius.setProgress(25);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始高斯
                long timeStart = SystemClock.currentThreadTimeMillis();
                Bitmap bmp1 = getBitmapFromView(iv_alpha,4);
                Bitmap bitmap = blurBitmap(bmp1,MainActivity.this,sb_radius.getProgress());
                iv_gaussian.setImageBitmap(bitmap);
                iv_alpha.setAlpha(0);
                sb_alpha.setProgress(0);
                long time = SystemClock.currentThreadTimeMillis()-timeStart;
                tv_time.setText("耗时："+time);
            }
        });
        sb_alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                StringBuilder string = new StringBuilder("透明度:");
                string.append(progress);
                tv_alpha.setText(string);
                iv_alpha.setAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                StringBuilder string = new StringBuilder("模糊半径:");
                string.append(progress);
                tv_radius.setText(string);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    /**
     * 执行高斯模糊
     *
     * @param bitmap
     * @param context
     * @return
     */
    public Bitmap blurBitmap(Bitmap bitmap, Context context,float radius) {
        if(radius<1){
            radius=1;
        }else if(radius>25){
            radius=25;
        }
        // 用需要创建高斯模糊bitmap创建一个空的bitmap
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        // 初始化Renderscript，这个类提供了RenderScript context，
        // 在创建其他RS类之前必须要先创建这个类，他控制RenderScript的初始化，资源管理，释放
        RenderScript rs = RenderScript.create(context);

        // 创建高斯模糊对象
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // 创建Allocations，此类是将数据传递给RenderScript内核的主要方法，
        // 并制定一个后备类型存储给定类型
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        // 设定模糊度
        blurScript.setRadius(radius);

        // Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        // recycle the original bitmap
        bitmap.recycle();

        // After finishing everything, we destroy the Renderscript.
        rs.destroy();
        return outBitmap;
    }

    /**
     * 获取view的bitmap
     * @param v
     * @param  scaleFactor 缩放比例
     * @return
     */
    public Bitmap getBitmapFromView(View v,int scaleFactor) {
        if (v == null) {
            return null;
        }
        Bitmap screenshot;
        screenshot = Bitmap.createBitmap(v.getWidth()/scaleFactor, v.getHeight()/scaleFactor, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(screenshot);
        c.translate(-v.getLeft() / scaleFactor, -v.getTop() / scaleFactor);
        c.scale(1f / scaleFactor, 1f / scaleFactor);
        v.draw(c);
        return screenshot;
    }

}
