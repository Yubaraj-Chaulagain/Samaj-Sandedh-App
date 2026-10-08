package com.samajsandesh.app;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.Locale;

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
            {"Kantipur FM 96.1", "https://radio-broadcast.ekantipur.com/stream"},
            {"Kalika FM 95.2", "https://streaming.softnep.net:10828/stream"},
            {"BBC Nepali", "https://stream.live.vc.bbcmedia.co.uk/bbc_nepali_radio"},
            {"CIN Khabar", "https://streaming.softnep.net:10996/;stream.mp3"},
            {"Ujyaalo 90 Network", "http://stream.zenolive.com/wtuvp08xq1duv"},
            {"Jayaprithvi FM", "https://streaming.softnep.net:10824/"},
            {"Butwal FM", "https://streaming.softnep.net:10994/;stream.nsv"},
            {"Image FM", "https://www.hamropatro.com/api/radio/stream/9"},
            {"Chitwan Radio Network", "https://www.hamropatro.com/api/radio/stream/123"},
            {"Makalu FM", "https://www.hamropatro.com/api/radio/stream/318"},
            {"Nepali Radio Network", "https://www.hamropatro.com/api/radio/stream/560"}
    };

    // Current public HLS entries checked against the current Nepali IPTV listing.
    // NTV also confirms these five channels are continuously available online.
    private final String[][] tv = {
            {"Nepal TV HD", "https://nepaltv.nettvnepal.com.np/notoken/NTVNEPAL1500.stream/chunks.m3u8"},
            {"NTV Plus HD", "https://nepaltv.nettvnepal.com.np/notoken/hd-NtvPlus-1500.stream/chunks.m3u8"},
            {"NTV News HD", "https://nepaltv.nettvnepal.com.np/notoken/hd-NtvNews-1500.stream/chunks.m3u8"},
            {"NTV Kohalpur HD", "https://nepaltv.nettvnepal.com.np/notoken/netNTVKOHALPUR1500.stream/chunks.m3u8"},
            {"NTV Itahari HD", "https://nepaltv.nettvnepal.com.np/notoken/ntvithari.stream/chunks.m3u8"},
            {"Kantipur TV HD", "https://ktvhdnpicc6670.ekantipur.com/ktv_abr/hd/kantipurtv/hd_720/chunks.m3u8"},
            {"Kantipur TV HD (Backup)", "https://ktvhdnpicc66.ekantipur.com/ktv_abr/hd/playlist.m3u8"},
            {"News24 Nepal", "http://maxotts.maxdigitaltv.com/x-media/C9/master.m3u8"},
            {"Public 4K TV", "http://103.180.240.141:8080/hls/main1/playlist.m3u8"}
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
            if (tvMode) releasePlayer();
            tvMode = false;
            search.setText("");
            switchMode();
            refresh("");
        });

        findViewById(R.id.tvTab).setOnClickListener(v -> {
            if (!tvMode) releasePlayer();
            tvMode = true;
            search.setText("");
            switchMode();
            refresh("");
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.play).setOnClickListener(v -> playSelected());
        findViewById(R.id.stop).setOnClickListener(v -> {
            if (player != null) player.stop();
            status.setText("⏹️ Stopped");
        });

        switchMode();
        refresh("");
    }

    private void switchMode() {
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
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();

        for (String[] item : source) {
            if (item[0].toLowerCase(Locale.ROOT).contains(q)) names.add(item[0]);
        }

        list.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
        ));
    }

    private void playSelected() {
        if (!online()) {
            status.setText("❌ Internet connection छैन।");
            return;
        }

        Object selected = list.getSelectedItem();
        if (selected == null) {
            status.setText(tvMode ? "TV channel छान्नुहोस्" : "Radio छान्नुहोस्");
            return;
        }

        String name = selected.toString();
        String url = findUrl(tvMode ? tv : radio, name);
        if (url.isEmpty()) {
            status.setText("❌ Stream URL उपलब्ध छैन।");
            return;
        }

        releasePlayer();
        player = new ExoPlayer.Builder(this).build();

        if (tvMode) videoPlayer.setPlayer(player);
        else audioControls.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                status.setText("❌ " + name + " अहिले चल्न सकेन।");
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
            MediaItem item = MediaItem.fromUri(url);
            MediaSource source;

            DefaultHttpDataSource.Factory httpFactory =
                    new DefaultHttpDataSource.Factory()
                            .setUserAgent("Samaj-Sandesh/1.0")
                            .setAllowCrossProtocolRedirects(true);

            if (tvMode) {
                // TV is HLS/M3U8: explicitly use Media3 HLS source.
                source = new HlsMediaSource.Factory(httpFactory)
                        .createMediaSource(item);
            } else {
                // Radio is progressive MP3/AAC/etc.: do NOT force M3U8.
                source = new ProgressiveMediaSource.Factory(httpFactory)
                        .createMediaSource(item);
            }

            player.setMediaSource(source);
            player.prepare();
            player.play();
            nowPlaying.setText((tvMode ? "📺 " : "📻 ") + name);

        } catch (Exception e) {
            status.setText("❌ Stream सुरु गर्न सकिएन।");
            releasePlayer();
        }
    }

    private String findUrl(String[][] source, String name) {
        for (String[] item : source) {
            if (item[0].equals(name)) return item[1];
        }
        return "";
    }

    private boolean online() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return nc != null && (
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    private void releasePlayer() {
        if (videoPlayer != null) videoPlayer.setPlayer(null);
        if (audioControls != null) audioControls.setPlayer(null);

        if (player != null) {
            player.stop();
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
