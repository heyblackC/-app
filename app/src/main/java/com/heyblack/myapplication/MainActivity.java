package com.heyblack.myapplication;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private DrawView Draw1 = null;//画板视图
    private DrawView Draw2 = null;//画板视图dummy
    private Button btn1 = null;
    private Button btn2 = null;
    private Button btn3 = null;
    private Button btn4 = null;
    private Button btn5 = null;
    private Button btn6 = null;
    private Button confirmBtn = null;
    private ImageView underView = null;
    public Bitmap bitmap=BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/l58.png"));

    ImageView img;
    String path;
    String filepath;

    String interPath = "/data/data/com.heyblack.myapplication/ImgStorage";

    //风格化处理等待提示框、完成提示
    AlertDialog waitMsg,finishMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        underView = findViewById(R.id.imageView);

        waitMsg = new AlertDialog.Builder(this)
                .setTitle("请稍等")
                .setMessage("正在为您风格化中，可能需要30秒左右的处理时间")
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .create();

        finishMsg = new AlertDialog.Builder(this)
                .setTitle("完成通知")
                .setMessage("您的图片风格化已完成")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定" ,  null )
                .create();


        //图版初始化、设置等操作
        inite();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //用来处理上传图片的返回
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            // UI界面的更新等相关操作
            Draw1.resultMap = base64ToBitmap(val);
            if (Draw1.imageView != null) {

                Draw1.imageView.setImageBitmap(Draw1.resultMap);

                waitMsg.dismiss();
                finishMsg.show();
            }
        }
    };


    private void inite(){
        Draw1=(DrawView)findViewById(R.id.writting);
        Draw2=(DrawView)findViewById(R.id.writting2);
        Draw2.dummy=true;

        btn1 = (Button)findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                img = (ImageView) findViewById(R.id.imageView1);
                img.setImageBitmap(bitmap);
                underView.setImageBitmap(bitmap);
                Draw1.bringToFront();
                Draw1.rawImg = bitmap;
                Draw1.resultMap = bitmap;
                Draw1.imageView = img;

                //getPhoto();
            }
        });

        btn2 = (Button)findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.changeSta(2);

                if (Draw1.resultMap == null) return;
                String img = bitmapToBase64(Draw1.resultMap);

                final String url = "http://120.79.67.94/finalversion.php";
                final Map<String,String> params = new HashMap<>();
                params.put("img",img);

                new Thread(new Runnable() {//创建子线程
                    @Override
                    public void run() {
                        NetWork netWork = new NetWork();
                        netWork.getwebinfo(url,params,handler);//把路径选到MainActivity中
                    }
                }).start();//启动子线程


                waitMsg.show();

            }
        });




        btn3 = (Button)findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.setcolor(Color.BLACK);
                Draw1.changeSta(2);
            }
        });

        confirmBtn = findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Draw1.onDrawImage();
            }
        });

        btn4 = (Button)findViewById(R.id.button4);
        btn4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.setcolor(Color.BLACK);
                Draw1.changeSta(0);
            }
        });
        btn5 = (Button)findViewById(R.id.button5);
        btn5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.setcolor(Color.GRAY);
                Draw1.changeSta(0);
            }
        });
        btn6 = (Button)findViewById(R.id.button6);
        btn6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Draw1.clearall();
            }
        });


    }

    private  void getPhoto(){
        img = (ImageView) findViewById(R.id.imageView1);
        File sd = Environment.getExternalStorageDirectory();
        path = sd.getPath();//获得手机内存storage的位置
        filepath = path + "/Pictures/Screenshots/S90606-180018.JPG";//storage下需要全屏显示的图片路径（要根据自己手机中需要显示图片路径位置进行修改）
        File file = new File(filepath);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<"+filepath);
        if (file.exists()) {
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<4");
            Bitmap bm = BitmapFactory.decodeFile(filepath);//获得设置路径下图片并编码为Bitmap格式

            System.out.println("<<<<<<<<<<<<<<<<<5");
            img.setImageBitmap(bm);//设置图片为背景图
        }
        else {
            System.err.println("<<<<<<<<<<<<<404 Not Find");//控制台输出没找到图片
        }
    }

    /**
     * 将Bitmap转换成Base64
     * @param bit
     * @return
     */
    private String bitmapToBase64(Bitmap bit)
    {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * 将Base64转换成Bitmap
     * @param base64String
     * @return
     */
    public static Bitmap base64ToBitmap(String base64String) {

        byte[] decode = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);

        return bitmap;
    }

    //创建文件夹
    public void createDirectory(){

        File destDir = new File(interPath);
        if (!destDir.exists()) {
            destDir.mkdirs();

        }
    }

    //保存风格化图片
    public void createImg(String img){

        //通过UUID生成字符串文件名
        String fileName = UUID.randomUUID().toString() + ".png";
        try {
            File file = new File(interPath + fileName);
            FileOutputStream out = new FileOutputStream(file);
            base64ToBitmap(img).compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
