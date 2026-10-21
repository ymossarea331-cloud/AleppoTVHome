package com.aleppotv.home;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  final int BG=Color.rgb(7,17,31), PANEL=Color.rgb(17,33,54), TEAL=Color.rgb(45,210,190), WHITE=Color.rgb(245,248,252), MUTED=Color.rgb(165,183,202);
  LinearLayout root, appsRow, prayerBox; TextView clockD, clockA, dateText, prayerStatus; Handler handler=new Handler();
  final String[] prayerNames={"الفجر","الشروق","الظهر","العصر","المغرب","العشاء"};
  String[] prayerTimes={"--:--","--:--","--:--","--:--","--:--","--:--"};
  @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); buildUi(); tick(); loadPrayerTimes(); }
  GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
  TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
  LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
  LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
  void buildUi(){
    root=column(); root.setPadding(34,24,34,22); root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(22,43,69),BG,Color.rgb(4,10,19)})); setContentView(root);
    LinearLayout top=row(); root.addView(top,lp(-1,0)); ((LinearLayout.LayoutParams)top.getLayoutParams()).weight=1;
    LinearLayout left=column(); top.addView(left,new LinearLayout.LayoutParams(0,-1,1.4f));
    TextView brand=text("✦  ALEPPO  TV",18,TEAL,true); left.addView(brand,lp(-1,42));
    clockD=text("--:--",64,WHITE,true); left.addView(clockD,lp(-1,90));
    dateText=text("Düsseldorf  •  --",20,MUTED,false); left.addView(dateText,lp(-1,38));
    TextView sub=text("مساء الخير  ·  أهلاً بك",22,WHITE,true); left.addView(sub,lp(-1,52));
    LinearLayout cities=row(); cities.setPadding(0,10,0,0); left.addView(cities,lp(-1,100));
    LinearLayout dCard=cityCard("دوسلدورف","DÜSSELDORF",false); cities.addView(dCard,new LinearLayout.LayoutParams(0,-1,1));
    LinearLayout aCard=cityCard("حلب","ALEPPO",true); LinearLayout.LayoutParams ac=new LinearLayout.LayoutParams(0,-1,1);ac.leftMargin=12;cities.addView(aCard,ac);
    LinearLayout right=column();right.setPadding(18,0,0,0);top.addView(right,new LinearLayout.LayoutParams(0,-1,1));
    TextView pTitle=text("☾  أوقات الصلاة  |  دوسلدورف",22,WHITE,true);right.addView(pTitle,lp(-1,48));
    prayerBox=column();prayerBox.setPadding(18,8,18,8);prayerBox.setBackground(shape(0xDD10243A,18));right.addView(prayerBox,new LinearLayout.LayoutParams(-1,0,1));
    prayerStatus=text("يتم تحديث أوقات الصلاة…",13,MUTED,false);right.addView(prayerStatus,lp(-1,30)); renderPrayers();
    TextView appsTitle=text("تطبيقاتك",20,WHITE,true);LinearLayout.LayoutParams at=lp(-1,38);at.topMargin=14;root.addView(appsTitle,at);
    ScrollView scroll=new ScrollView(this);scroll.setHorizontalScrollBarEnabled(false);root.addView(scroll,lp(-1,150));appsRow=row();scroll.addView(appsRow); populateApps();
    TextView foot=text("استخدم الأسهم للتنقّل  •  OK لفتح التطبيق  •  Home للعودة",13,MUTED,false);foot.setGravity(Gravity.CENTER);root.addView(foot,lp(-1,30));
  }
  LinearLayout cityCard(String arabic,String english,boolean aleppo){LinearLayout c=column();c.setPadding(16,10,16,10);c.setBackground(shape(0xAAFFFFFF,16));TextView n=text(arabic+"  ·  "+english,14,MUTED,true);c.addView(n,lp(-1,26));TextView clk=text("--:--",32,WHITE,true);if(aleppo)clockA=clk;else { /* main clock also shown above */ } c.addView(clk,lp(-1,46));return c;}
  void populateApps(){
    PackageManager pm=getPackageManager();Intent query=new Intent(Intent.ACTION_MAIN);query.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);List<ResolveInfo> list=pm.queryIntentActivities(query,0);
    if(list.isEmpty()){query.removeCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);query.addCategory(Intent.CATEGORY_LAUNCHER);list=pm.queryIntentActivities(query,0);}
    Collections.sort(list,(a,b)->String.valueOf(a.loadLabel(pm)).compareToIgnoreCase(String.valueOf(b.loadLabel(pm)))); int count=0;
    for(ResolveInfo ri:list){if(ri.activityInfo.packageName.equals(getPackageName()))continue;if(count++>=18)break;
      LinearLayout tile=column();tile.setGravity(Gravity.CENTER);tile.setFocusable(true);tile.setFocusableInTouchMode(true);tile.setPadding(10,8,10,8);tile.setBackground(shape(0xCC142A43,16));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(128,126);tp.rightMargin=12;appsRow.addView(tile,tp);
      ImageView icon=new ImageView(this);try{icon.setImageDrawable(ri.loadIcon(pm));}catch(Exception ignored){} icon.setScaleType(ImageView.ScaleType.FIT_CENTER);tile.addView(icon,lp(48,48));TextView name=text(String.valueOf(ri.loadLabel(pm)),13,WHITE,true);name.setGravity(Gravity.CENTER);name.setMaxLines(2);tile.addView(name,lp(-1,42));
      tile.setOnFocusChangeListener((v,focus)->{v.setBackground(shape(focus?0xFF176A70:0xCC142A43,16));if(focus)v.setScaleX(1.06f);else v.setScaleX(1f);v.setScaleY(focus?1.06f:1f);});tile.setOnClickListener(v->{Intent launch=pm.getLaunchIntentForPackage(ri.activityInfo.packageName);if(launch==null){launch=new Intent(Intent.ACTION_MAIN);launch.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);launch.setPackage(ri.activityInfo.packageName);}try{startActivity(launch);}catch(Exception e){Toast.makeText(this,"تعذّر فتح التطبيق",Toast.LENGTH_SHORT).show();}});
    }
    if(appsRow.getChildCount()>0)appsRow.getChildAt(0).requestFocus();
  }
  void tick(){try{Date now=new Date();SimpleDateFormat d=new SimpleDateFormat("HH:mm",Locale.GERMANY);d.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));clockD.setText(d.format(now));dateText.setText("Düsseldorf  •  "+new SimpleDateFormat("EEEE, dd MMMM",Locale.GERMANY).format(now));SimpleDateFormat a=new SimpleDateFormat("HH:mm",Locale.GERMANY);a.setTimeZone(TimeZone.getTimeZone("Asia/Damascus"));if(clockA!=null)clockA.setText(a.format(now));}catch(Exception ignored){}handler.postDelayed(this::tick,1000);}
  void renderPrayers(){if(prayerBox==null)return;prayerBox.removeAllViews();String[] times=prayerTimes;int next=-1;try{String now=new SimpleDateFormat("HH:mm",Locale.GERMANY).format(new Date());for(int i=0;i<times.length;i++)if(times[i].compareTo(now)>0){next=i;break;}}catch(Exception ignored){}
    for(int i=0;i<prayerNames.length;i++){LinearLayout r=row();r.setPadding(12,2,12,2);if(i==next)r.setBackground(shape(0xFF126B70,10));TextView n=text(prayerNames[i],17,WHITE,i==next);r.addView(n,new LinearLayout.LayoutParams(0,0,1));((LinearLayout.LayoutParams)n.getLayoutParams()).height=38;TextView t=text(times[i],17,WHITE,i==next);t.setGravity(Gravity.CENTER);r.addView(t,lp(78,38));prayerBox.addView(r,lp(-1,40));}}
  void loadPrayerTimes(){new Thread(()->{HttpURLConnection c=null;try{String date=new SimpleDateFormat("dd-MM-yyyy",Locale.US).format(new Date());URL u=new URL("https://api.aladhan.com/v1/timingsByCity/"+date+"?city=Dusseldorf&country=Germany&method=3");c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(7000);c.setReadTimeout(7000);c.setRequestMethod("GET");BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream()));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);JSONObject data=new JSONObject(sb.toString()).getJSONObject("data").getJSONObject("timings");String[] keys={"Fajr","Sunrise","Dhuhr","Asr","Maghrib","Isha"};String[] out=new String[keys.length];for(int i=0;i<keys.length;i++)out[i]=data.getString(keys[i]).substring(0,5);runOnUiThread(()->{prayerTimes=out;renderPrayers();prayerStatus.setText("تُحدّث يومياً عبر الإنترنت  ·  تحقق من طريقة الحساب المحلية");});}catch(Exception e){runOnUiThread(()->prayerStatus.setText("تعذّر تحديث الصلاة — تحقّق من اتصال الإنترنت"));}finally{if(c!=null)c.disconnect();}}).start();}
  @Override public void onBackPressed(){super.onBackPressed();}
}
