package com.samajsandesh.app;

import android.os.Bundle;
import android.net.*;
import android.text.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import java.util.*;

public class MainActivity extends AppCompatActivity {
 ExoPlayer player; PlayerView pv; Spinner list; EditText search; TextView status; boolean tvMode=false;
 String[][] radio={   {"Kantipur FM 96.1","https://radio-broadcast.ekantipur.com/stream"},
  {"Kalika FM 95.2","https://streaming.softnep.net:10828/stream"},
  {"BBC Nepali","https://stream.live.vc.bbcmedia.co.uk/bbc_nepali_radio"},
  {"CIN Khabar","https://streaming.softnep.net:10996/;stream.mp3"},
  {"Ujyaalo 90 Network","http://stream.zenolive.com/wtuvp08xq1duv"},
  {"Jayaprithvi FM","https://streaming.softnep.net:10824/"},
  {"Butwal FM","https://streaming.softnep.net:10994/;stream.nsv"},
  {"Image FM","https://www.hamropatro.com/api/radio/stream/9"},
  {"Chitwan Radio Network","https://www.hamropatro.com/api/radio/stream/123"},
  {"Makalu FM","https://www.hamropatro.com/api/radio/stream/318"},
  {"Nepali Radio Network","https://www.hamropatro.com/api/radio/stream/560"} };
 String[][] tv={   {"Nepal TV HD","https://nepaltv.nettvnepal.com.np/notoken/netNTV.stream/chunks.m3u8"},
  {"NTV Plus HD","https://nepaltv.nettvnepal.com.np/notoken/netNTVPlus.stream/chunks.m3u8"},
  {"NTV News HD","https://nepaltv.nettvnepal.com.np/notoken/hd-NtvNews-1500.stream/chunks.m3u8"},
  {"NTV Kohalpur HD","https://nepaltv.nettvnepal.com.np/notoken/netNTVKOHALPUR1500.stream/chunks.m3u8"},
  {"NTV Itahari HD","https://nepaltv.nettvnepal.com.np/notoken/ntvithari.stream/chunks.m3u8"},
  {"Kantipur TV HD","https://ktvhdnpicc6670.ekantipur.com/ktv_abr/hd/kantipurtv/hd_1080/playlist.m3u8"},
  {"Kantipur TV 720p","https://ktvhdnpicc6670.ekantipur.com/ktv_abr/hd/kantipurtv/hd_720/chunks.m3u8"},
  {"News24 Nepal","http://maxotts.maxdigitaltv.com/x-media/C9/master.m3u8"},
  {"Public 4K TV","http://103.180.240.141:8080/hls/main1/playlist.m3u8"} };
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);
  pv=findViewById(R.id.playerView);list=findViewById(R.id.list);search=findViewById(R.id.search);status=findViewById(R.id.status);
  findViewById(R.id.radioTab).setOnClickListener(v->{tvMode=false;search.setText("");refresh("");});
  findViewById(R.id.tvTab).setOnClickListener(v->{tvMode=true;search.setText("");refresh("");});
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){} public void onTextChanged(CharSequence s,int a,int b,int c){refresh(s.toString());} public void afterTextChanged(Editable e){}});
  findViewById(R.id.play).setOnClickListener(v->playSelected());
  findViewById(R.id.stop).setOnClickListener(v->{if(player!=null)player.stop();status.setText("⏹️ Stopped");});
  refresh("");
 }
 void refresh(String q){ArrayList<String>a=new ArrayList<>();for(String[]x:(tvMode?tv:radio))if(x[0].toLowerCase().contains(q.toLowerCase()))a.add(x[0]);
  list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,a));status.setText(tvMode?"📺 Live TV":"📻 Radio");}
 void playSelected(){if(!online()){status.setText("Please No Internet connection 🙏");return;}String n=(String)list.getSelectedItem(),u="";
  for(String[]x:(tvMode?tv:radio))if(x[0].equals(n))u=x[1]; if(u.isEmpty()){status.setText("Direct stream URL उपलब्ध छैन।");return;}
  if(player!=null)player.release();player=new ExoPlayer.Builder(this).build();pv.setPlayer(player);player.setMediaItem(MediaItem.fromUri(u));player.prepare();player.play();status.setText("▶️ Playing: "+n);}
 boolean online(){ConnectivityManager c=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);NetworkCapabilities n=c.getNetworkCapabilities(c.getActiveNetwork());return n!=null&&(n.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)||n.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)||n.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));}
 @Override protected void onDestroy(){if(player!=null)player.release();super.onDestroy();}
}