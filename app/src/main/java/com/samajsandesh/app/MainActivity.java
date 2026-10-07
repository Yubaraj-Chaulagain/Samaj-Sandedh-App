package com.samajsandesh.app;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.Collections;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private Spinner list;
    private EditText search;
    private TextView nowPlaying;
    private TextView status;

    private PlayerView playerView;
    private PlayerControlView audioControls;

    private Button playButton;
    private Button stopButton;
    private Button radioTab;
    private Button tvTab;

    private ExoPlayer player;
    private boolean tvMode = false;

    private final String[][] RADIO = {
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

    private final String[][] TV = {
            {"Nepal Television HD", "https://nepaltv.nettvnepal.com.np/notoken/NTVNEPAL1500.stream/chunks.m3u8"},
            {"NTV Plus HD", "https://nepaltv.nettvnepal.com.np/notoken/hd-NtvPlus-1500.stream/chunks.m3u8"},
            {"NTV News HD", "https://nepaltv.nettvnepal.com.np/notoken/hd-NtvNews-1500.stream/chunks.m3u8"},
            {"Kantipur TV HD", "https://ktvhdnpicc66.ekantipur.com/ktv_abr/hd/playlist.m3u8"},
            {"Kantipur TV HD – Backup", "https://ktvhdsg.ekantipur.com:8443/high_quality_85840165/hd/playlist.m3u8"}
    };

    private final ArrayList<String> displayNames = new ArrayList<>();
    private final ArrayList<String> displayUrls = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // IDs exactly match the current activity_main.xml
        list = findViewById(R.id.list);
        search = findViewById(R.id.search);
        nowPlaying = findViewById(R.id.nowPlaying);
        status = findViewById(R.id.status);

        playerView = findViewById(R.id.playerView);
        audioControls = findViewById(R.id.audioControls);

        playButton = findViewById(R.id.play);
        stopButton = findViewById(R.id.stop);
        radioTab = findViewById(R.id.radioTab);
        tvTab = findViewById(R.id.tvTab);

        playerView.setUseController(true);
        playerView.setVisibility(View.GONE);
        audioControls.setVisibility(View.GONE);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                displayNames
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        list.setAdapter(adapter);

        radioTab.setOnClickListener(v -> showRadioList());
        tvTab.setOnClickListener(v -> showTvList());

        list.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
                        // Selecting an item does not auto-play.
                        // Press Play to start the selected station/channel.
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        playButton.setOnClickListener(v -> playSelectedItem());
        stopButton.setOnClickListener(v -> stopPlayback());

        showRadioList();
    }

    private void showRadioList() {
        tvMode = false;
        stopCurrentPlayer();

        playerView.setVisibility(View.GONE);
        audioControls.setVisibility(View.GONE);

        fillList(RADIO, "📻 ");
        nowPlaying.setText("📻 Radio Player");
        status.setText("Radio छान्नुहोस्");
    }

    private void showTvList() {
        tvMode = true;
        stopCurrentPlayer();

        playerView.setVisibility(View.GONE);
        audioControls.setVisibility(View.GONE);

        fillList(TV, "📺 ");
        nowPlaying.setText("📺 Live TV");
        status.setText("Live TV channel छान्नुहोस्");
    }

    private void fillList(String[][] source, String prefix) {
        displayNames.clear();
        displayUrls.clear();

        for (String[] item : source) {
            displayNames.add(prefix + item[0]);
            displayUrls.add(item[1]);
        }

        adapter.notifyDataSetChanged();

        if (!displayNames.isEmpty()) {
            list.setSelection(0);
        }
    }

    private void filterList(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        String[][] source = tvMode ? TV : RADIO;
        String prefix = tvMode ? "📺 " : "📻 ";

        displayNames.clear();
        displayUrls.clear();

        for (String[] item : source) {
            if (q.isEmpty() || item[0].toLowerCase().contains(q)) {
                displayNames.add(prefix + item[0]);
                displayUrls.add(item[1]);
            }
        }

        adapter.notifyDataSetChanged();

        if (!displayNames.isEmpty()) {
            list.setSelection(0);
        }
    }

    private void playSelectedItem() {
        int position = list.getSelectedItemPosition();

        if (position < 0 || position >= displayUrls.size()) {
            status.setText("❌ पहिले Radio/TV छान्नुहोस्");
            return;
        }

        playSelected(displayNames.get(position), displayUrls.get(position));
    }

    private void playSelected(String name, String url) {
        if (tvMode) {
            playTv(name, url);
        } else {
            playRadio(name, url);
        }
    }

    private DefaultHttpDataSource.Factory createHttpFactory() {
        return new DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Android) Samaj Sandesh/1.8")
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(
                        Collections.singletonMap("Accept", "*/*")
                );
    }

    private void playTv(String name, String url) {
        tvMode = true;
        stopCurrentPlayer();

        audioControls.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);

        nowPlaying.setText(name);
        status.setText("⏳ Connecting to live video...");

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem item = MediaItem.fromUri(Uri.parse(url));

        HlsMediaSource source =
                new HlsMediaSource.Factory(createHttpFactory())
                        .createMediaSource(item);

        player.setMediaSource(source);
        addPlayerListener();

        player.prepare();
        player.play();
    }

    private void playRadio(String name, String url) {
        tvMode = false;
        stopCurrentPlayer();

        playerView.setVisibility(View.GONE);
        audioControls.setVisibility(View.VISIBLE);

        nowPlaying.setText(name);
        status.setText("⏳ Loading...");

        player = new ExoPlayer.Builder(this).build();
        audioControls.setPlayer(player);

        player.setMediaItem(
                MediaItem.fromUri(Uri.parse(url))
        );

        addPlayerListener();

        player.prepare();
        player.play();
    }

    private void addPlayerListener() {
        if (player == null) return;

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    status.setText("⏳ Buffering...");
                } else if (state == Player.STATE_READY) {
                    status.setText("▶️ LIVE");
                } else if (state == Player.STATE_ENDED) {
                    status.setText("⏹ Playback ended");
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                status.setText(
                        "❌ Stream unavailable. Try another channel."
                );
            }
        });
    }

    private void stopPlayback() {
        stopCurrentPlayer();

        playerView.setVisibility(View.GONE);
        audioControls.setVisibility(View.GONE);

        nowPlaying.setText(
                tvMode ? "📺 Live TV" : "📻 Radio Player"
        );
        status.setText("⏹ Stopped");
    }

    private void stopCurrentPlayer() {
        if (audioControls != null) {
            audioControls.setPlayer(null);
        }

        if (playerView != null) {
            playerView.setPlayer(null);
        }

        if (player != null) {
            try {
                player.stop();
                player.release();
            } catch (Exception ignored) {
            }
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null) {
            stopPlayback();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        stopCurrentPlayer();
        super.onDestroy();
    }
}
