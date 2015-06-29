package com.example.stud.scorbot1;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;



/*public class IOService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startService()
    {
        try {
// WatchData - это класс, с помощью которого мы передадим параметры в
// создаваемый поток
            WatchData data = new WatchData();
            data.email = acc_email;
            data.ctx = this;

// создаем новый поток для сокет-соединения
            new WatchSocket().execute(data);

        } catch (Exception e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}*/

public class MainActivity extends Activity {
    Area area;
    /*static public class Area extends FrameLayout
    {

        public Area()
        {
            super();

        }

    }*/

    //View.OnTouchListener onTouch;
    //RadioGroup rgSel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //rgSel = (RadioGroup)findViewById(R.id.rgSel);
        FrameLayout fl = (FrameLayout)findViewById(R.id.area);
        area = new Area(this);
        fl.addView(area);
        area.setOnTouchListener(area);
        //Drawable d = area.getForeground();
        //area.
        //d.
        //setContentView(new GraphicsView(this));

    }
    static public class Area extends View implements View.OnTouchListener {
        int R0 = 15,  // смещение от центра платформы до плеча
            Z0 = 363, // 208: высота нижнего края платформы + 140: расстояние от нижнего края до плеча + 15: фанера
            L1 = 221,   // Растояние от плеча до локтя
            L2 = 221,   // от локтя до кисти
            L3 = 140;   // от кисти до центра между захватами
        float scale = 0.5f;
        Paint pBot, pAxes;
        public class ABC {
            float A, B, C;
        }

        public class Pos {
            float X, Y, R, Z, A, B, C, D, E;
        }

        Pos pos;
        int state;
        Socket socket = null;
        InputStream is;
        DataOutputStream os = null;
        byte[] buf = new byte[100];
        public void connect()
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket("192.168.4.1", 8080);
                        //socket.setKeepAlive(true);
                        //is = socket.getInputStream();
                        os = new DataOutputStream(socket.getOutputStream());
                        os.writeBytes("pos;");
                        os.flush();
                        os.writeBytes("ptp x" + pos.X + " y" + -pos.Y + " z" + pos.Z + " e" + pos.E + " d" + pos.D + ";\r\n");
                        os.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        public void disconnect()
        {
            try {
                socket.close();
                socket = null;
                os = null;
                is = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public class Sender extends Thread
        {
            public String s;
            public DataOutputStream os;
            @Override
            public void run() {
                if(os != null) try
                {
                    os.writeBytes(s);
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Sender(String s, DataOutputStream os)
            {
                this.s = s;
                this.os = os;
            }
        }
        public void send(String s)
        {
            Sender sender = new Sender(s, os);
            sender.start();
        }
        public Area(Context context) {
            super(context);
            pBot = new Paint();
            pBot.setStrokeWidth(30);
            pBot.setColor(Color.GRAY);
            pAxes = new Paint();
            pAxes.setColor(Color.BLUE);
            pos = new Pos();
            pos.A = 0;
            pos.X = 300;
            pos.Y = 0;
            pos.Z = 300;
            pos.R = (float) Math.sqrt(pos.X * pos.X + pos.Y * pos.Y);
            pos.D = 0;
            pos.E = 0;
            state = 0;
            connect();
        }
        public boolean RZtoBC(ABC abc, float R, float Z, float D)
        {
            D *= Math.PI / 180;
            R -= R0 + L3 * Math.sin(D);
            Z -= Z0 - L3 * Math.cos(D);
            float L = (float) Math.sqrt(R * R + Z * Z);
            if(L > L1 + L2) return false;
            float a = (float)Math.atan2(Z, R);
            float t = (float)Math.acos(L / (L1 + L2));
            float B = (float)(Math.PI/2 - a - t);// * (180 / Math.PI));
            float C = (t - a);// * (180 / Math.PI));
            abc.B = B;
            abc.C = C;
            return true;
        }
        @Override
        protected void onDraw(Canvas canvas) {
            // ваши команды для рисования
            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h / 2;
            switch(state) {
                case 0:
                    canvas.drawCircle(cx, cy, 50, pBot);
                    canvas.drawLine(cx, cy, cx + pos.X * scale, cy - pos.Y * scale, pBot);

                    canvas.drawLine(cx, cy, cx + 100, cy, pAxes);
                    canvas.drawText("X", cx + 100, cy + 10, pAxes);
                    canvas.drawLine(cx, cy, cx, cy - 100, pAxes);
                    canvas.drawText("Y", cx + 10, cy - 100, pAxes);
                    break;
                case 1:
                case 2:
                    cx = 60;
                    //canvas.drawCircle(cx, cy, 50, pBot);
                    canvas.drawRect(cx - 50, h - Z0 * scale, cx + 50, h, pBot);
                    ABC abc = new ABC();
                    if(RZtoBC(abc, pos.R, pos.Z, pos.D))
                    {
                        float x1 = (float)(R0 + Math.sin(abc.B) * L1);
                        float z1 = (float)(Z0 + Math.cos(abc.B) * L1);
                        canvas.drawLine(cx + R0 * scale, h - Z0 * scale, cx + x1 * scale, h - z1 * scale, pBot);
                        float x2 = (float)(x1 + Math.cos(abc.C) * L2);
                        float z2 = (float)(z1 - Math.sin(abc.C) * L2);
                        canvas.drawLine(cx + x1 * scale, h - z1 * scale, cx + x2 * scale, h - z2 * scale, pBot);
                        float x3 = (float)(x2 + Math.sin(pos.D * Math.PI / 180) * L3);
                        float z3 = (float)(z2 - Math.cos(pos.D * Math.PI / 180) * L3);
                        canvas.drawLine(cx + x2 * scale, h - z2 * scale, cx + x3 * scale, h - z3 * scale, pBot);

                        //canvas.drawLine(cx, h - Z0 * scale, cx + pos.R * scale, h - pos.Z * scale, pBot);
                    }
                    canvas.drawLine(cx, h - 10, cx + 100, h - 10, pAxes);
                    canvas.drawText("R", cx + 100, h - 10, pAxes);
                    canvas.drawLine(cx, h - 10, cx, h - 100, pAxes);
                    canvas.drawText("Z", cx + 10, h - 100, pAxes);
                    break;
            }
        }
        public void calcBC()
        {
            Area.ABC abc = new Area.ABC();
            if(RZtoBC(abc, pos.R, pos.Z, pos.D))
            {
                pos.B = abc.B;
                pos.C = abc.C;
            }
        }
        float tX, tY;
        public boolean onTouch(View v, MotionEvent e)
        {
            switch(e.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    tX = e.getX();
                    tY = e.getY();
                    break;
                case MotionEvent.ACTION_UP:


                    break;
                case MotionEvent.ACTION_MOVE:

                    float x = e.getX();
                    float y = e.getY();
                    switch(state)
                    {
                        case 0:
                            pos.X += x - tX;
                            pos.Y -= y - tY;
                            pos.A = (float) Math.atan2(pos.Y, pos.X);// / PI180;
                            send("ref x" + pos.X + " y" + -pos.Y + "; ");
                            pos.R = (float) Math.sqrt(pos.X * pos.X + pos.Y * pos.Y);
                            break;
                        case 1:
                            float oR = pos.R, oZ = pos.Z;
                            pos.R += x - tX;
                            pos.Z -= y - tY;

                            ABC abc = new ABC();
                            if(!RZtoBC(abc, pos.R, pos.Z, pos.D)) {
                                pos.R = oR;
                                pos.Z = oZ;
                                break;
                            }

                            pos.X = (float) (pos.R * Math.cos(pos.A));
                            pos.Y = (float) (pos.R * Math.sin(pos.A));

                            send("ref r" + pos.R + " z" + pos.Z + "; ");
                            break;
                        case 2:
                            pos.R -= L3 * Math.sin(pos.D * Math.PI / 180);
                            pos.Z += L3 * Math.cos(pos.D * Math.PI / 180);

                            pos.E += (x - tX) * 0.2;
                            pos.D -= (y - tY) * 0.2;
                            pos.R += L3 * Math.sin(pos.D * Math.PI / 180);
                            pos.Z -= L3 * Math.cos(pos.D * Math.PI / 180);
                            pos.X = (float) (pos.R * Math.cos(pos.A));
                            pos.Y = (float) (pos.R * Math.sin(pos.A));

                            send("ref d" + pos.D + " e" + pos.E + "; ");
                            break;
                    }
                    tX = x;
                    tY = y;
                    invalidate();
                    break;
            }
            return true;
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/
    public void onDisconnect(View v)
    {
        area.disconnect();
    }
    public void onConnect(View v)
    {
        area.connect();
    }
    public void onGet(View v)
    {
        area.send("get;");
    }
    public void onPut(View v)
    {
        area.send("put;");
    }
    public void onPush(View v)
    {
        area.send("save;");
    }
    public void onPop(View v)
    {
        area.send("pop;");
    }
    public void onGo(View v)
    {
        area.send("go;");
    }
    public void onXY(View v) {
        //rgSel.getCheckedRadioButtonId();
        area.state = 0;
        area.invalidate();
    }
    public void onRZ(View v) {
        //rgSel.getCheckedRadioButtonId();
        area.state = 1;
        area.invalidate();
    }
    public void onDE(View v) {
        //rgSel.getCheckedRadioButtonId();
        area.calcBC();
        area.state = 2;
        area.invalidate();
    }
    /*public void onXY(View v)
    {
        Button b = (Button)findViewById(R.id.bXY);
        //b.setBackground(getResources().getColor(R.color.background_material_dark));
        //b.setTextColor(0xFF0000);
        //bXY;

    }*/

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
}
