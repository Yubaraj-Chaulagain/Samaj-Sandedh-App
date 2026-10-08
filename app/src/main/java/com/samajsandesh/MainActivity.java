package com.samajsandesh.app;

import android.os.Bundle;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView videoPlayer;
    private PlayerControlView audioControls;
    private Spinner list;
    private EditText search;
    private TextView status;
    private TextView nowPlaying;
    private boolean tvMode = false;

    private final String[][] radio = {
        {"Kantipur FM 96.1","https://radio-broadcast.ekantipur.com/stream"},
        {"Kalika FM 95.2","https://streaming.softnep.net:10828/stream"},
        {"BBC Nepali","https://stream.live.vc.bbcmedia.co.uk/bbc_nepali_radio"},
        {"CIN Khabar","https://streaming.softnep.net:10996/;stream.mp3"},
        {"Ujyaalo 90 Network","http://stream.zenolive.com/wtuvp08xq1duv"},
        {"Jayaprithvi FM","https://streaming.softnep.net:10824/"},
        {"Butwal FM","https://streaming.softnep.net:10994/;stream.nsv"},
        {"Image FM","https://www.hamropatro.com/api/radio/stream/9"},
        {"Chitwan Radio Network","https://www.hamropatro.com/api/radio/stream/123"},
        {"Makalu FM","https://www.hamropatro.com/api/radio/stream/318"},
        {"Nepali Radio Network","https://www.hamropatro.com/api/radio/stream/560"}
    };

    private final String[][] tv = {
        {"Nepal TV HD","https://nepaltv.nettvnepal.com.np/notoken/NTVNEPAL1500.stream/chunks.m3u8"},
        {"NTV Plus HD","https://nepaltv.nettvnepal.com.np/notoken/hd-NtvPlus-1500.stream/chunks.m3u8"},
        {"NTV News HD","https://nepaltv.nettvnepal.com.np/notoken/hd-NtvNews-1500.stream/chunks.m3u8"},
        {"NTV Kohalpur HD","https://nepaltv.nettvnepal.com.np/notoken/netNTVKOHALPUR1500.stream/chunks.m3u8"},
        {"NTV Itahari HD","https://nepaltv.nettvnepal.com.np/notoken/ntvithari.stream/chunks.m3u8"},
        {"Kantipur TV HD","https://ktvhdnpicc6670.ekantipur.com/ktv_abr/hd/kantipurtv/hd_720/chunks.m3u8"},
        {"Kantipur TV HD (Backup)","https://ktvhdnpicc66.ekantipur.com/ktv_abr/hd/playlist.m3u8"},
        {"Image Channel Nepal","http://imagetvonline.imagekhabar.com:1935/live/image/playlist.m3u8"},
        {"News24 Nepal","http://tiny.cc/vnkklz"},
        {"ABC Nepal","http://live.zecast.net/ntvglobal/nepalitv/chunklist.m3u8"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoPlayer = findViewById(R.id.playerView);
        audioControls = findViewById(R.id.audioControls);
        list = findViewById(R.id.list);
        search = findViewById(R.id.search);
        status = findViewById(R.id.status);
        nowPlaying = findViewById(R.id.nowPlaying);

        findViewById(R.id.radioTab).setOnClickListener(v -> {
            tvMode = false;
            search.setText("");
            switchMode();
            refresh("");
        });

        findViewById(R.id.tvTab).setOnClickListener(v -> {
            tvMode = true;
            search.setText("");
            switchMode();
            refresh("");
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.play).setOnClickListener(v -> playSelected());

        findViewById(R.id.stop).setOnClickListener(v -> {
            if (player != null) {
                player.stop();
            }
            status.setText("⏹️ Stopped");
        });

        switchMode();
        refresh("");
    }

    private void switchMode() {
        releasePlayer();

        if (tvMode) {
            videoPlayer.setVisibility(View.VISIBLE);
            audioControls.setVisibility(View.GONE);
            nowPlaying.setText("📺 Live TV Player");
            status.setText("TV channel छान्नुहोस्");
        } else {
            videoPlayer.setVisibility(View.GONE);
            audioControls.setVisibility(View.VISIBLE);
            nowPlaying.setText("📻 Radio Audio Player");
            status.setText("Radio छान्नुहोस्");
        }
    }

    private void refresh(String query) {
        ArrayList<String> names = new ArrayList<>();
        String[][] source = tvMode ? tv : radio;

        for (String[] item : source) {
            if (item[0].toLowerCase().contains(query.toLowerCase())) {
                names.add(item[0]);
            }
        }

        list.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
        ));
    }

    private void playSelected() {
        if (!online()) {
            status.setText("Please No Internet connection 🙏");
            return;
        }

        Object selected = list.getSelectedItem();
        if (selected == null) {
            status.setText(tvMode ? "TV channel छान्नुहोस्" : "Radio छान्नुहोस्");
            return;
        }

        String name = selected.toString();
        String url = "";
        String[][] source = tvMode ? tv : radio;

        for (String[] item : source) {
            if (item[0].equals(name)) {
                url = item[1];
                break;
            }
        }

        if (url.isEmpty()) {
            status.setText("Direct stream URL उपलब्ध छैन।");
            return;
        }

        releasePlayer();

        player = new ExoPlayer.Builder(this).build();

        if (tvMode) {
            videoPlayer.setPlayer(player);
        } else {
            audioControls.setPlayer(player);
        }

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                status.setText("❌ यो stream अहिले चल्न सकेन। अर्को channel छान्नुहोस्।");
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    status.setText("⏳ Loading: " + name);
                } else if (state == Player.STATE_READY) {
                    status.setText("▶️ Playing: " + name);
                }
            }
        });

        try {
            MediaItem.Builder mediaBuilder = new MediaItem.Builder()
                    .setUri(url);

            // Only force HLS for TV. Radio streams may be MP3/AAC/etc.
            if (tvMode) {
                mediaBuilder.setMimeType(MimeTypes.APPLICATION_M3U8);
            }

            MediaItem mediaItem = mediaBuilder.build();
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
            nowPlaying.setText((tvMode ? "📺 " : "📻 ") + name);
        } catch (Exception e) {
            status.setText("❌ Stream error");
        }
    }

    private boolean online() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());

        return nc != null &&
                (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
