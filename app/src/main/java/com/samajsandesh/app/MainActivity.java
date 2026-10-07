package com.samajsandesh.app;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    // =========================
    // UI
    // =========================

    private ListView listView;
    private EditText searchBox;
    private TextView nowPlaying;
    private TextView status;

    private PlayerView tvPlayerView;
    private PlayerControlView radioControls;

    private WebView tvWebView;

    private Button stopButton;

    // =========================
    // Player
    // =========================

    private ExoPlayer player;

    private boolean tvMode = false;

    // =========================
    // Radio List
    // =========================

    private final String[][] RADIO = {

            {"Kantipur FM 96.1",
                    "https://radio-broadcast.ekantipur.com/stream"},

            {"Kalika FM 95.2",
                    "https://streaming.softnep.net:10828/stream"},

            {"BBC Nepali",
                    "https://stream.live.vc.bbcmedia.co.uk/bbc_nepali_radio"},

            {"CIN Khabar",
                    "https://streaming.softnep.net:10996/;stream.mp3"},

            {"Ujyaalo 90 Network",
                    "http://stream.zenolive.com/wtuvp08xq1duv"},

            {"Jayaprithvi FM",
                    "https://streaming.softnep.net:10824/"},

            {"Butwal FM",
                    "https://streaming.softnep.net:10994/;stream.nsv"},

            {"Image FM",
                    "https://www.hamropatro.com/api/radio/stream/9"},

            {"Chitwan Radio Network",
                    "https://www.hamropatro.com/api/radio/stream/123"},

            {"Makalu FM",
                    "https://www.hamropatro.com/api/radio/stream/318"},

            {"Nepali Radio Network",
                    "https://www.hamropatro.com/api/radio/stream/560"}
    };

    // =========================
    // Official TV Pages
    // =========================

    private final String[][] TV = {

            {
                    "Nepal Television – Official Live",
                    "https://nepaltvonline.com/live"
            },

            {
                    "Kantipur TV – Official Live",
                    "https://kantipurtv.com/live"
            },

            {
                    "NetTV – Live TV",
                    "https://webtv.nettv.com.np/livetv"
            }
    };

    // =========================
    // Current List
    // =========================

    private final ArrayList<String> displayNames = new ArrayList<>();
    private final ArrayList<String> displayUrls = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    // =========================
    // onCreate
    // =========================

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Find views
        listView = findViewById(R.id.listView);
        searchBox = findViewById(R.id.searchBox);
        nowPlaying = findViewById(R.id.nowPlaying);
        status = findViewById(R.id.status);

        tvPlayerView = findViewById(R.id.tvPlayerView);
        radioControls = findViewById(R.id.radioControls);

        tvWebView = findViewById(R.id.tvWebView);

        stopButton = findViewById(R.id.stopButton);

        // Initial UI
        tvPlayerView.setVisibility(View.GONE);
        radioControls.setVisibility(View.GONE);
        tvWebView.setVisibility(View.GONE);

        // Setup WebView
        setupTvWebView();

        // Setup list
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayNames
        );

        listView.setAdapter(adapter);

        // Default Radio list
        showRadioList();

        // Item click
        listView.setOnItemClickListener((parent, view, position, id) -> {

            if (position < 0 || position >= displayUrls.size()) {
                return;
            }

            String name = displayNames.get(position);
            String url = displayUrls.get(position);

            playSelected(name, url);
        });

        // Search
        searchBox.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterList(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );

        // Stop button
        stopButton.setOnClickListener(v -> stopPlayback());

        status.setText("Ready");
    }

    // ============================================================
    // WEBVIEW
    // ============================================================

    @SuppressLint("SetJavaScriptEnabled")
    private void setupTvWebView() {

        if (tvWebView == null) {
            return;
        }

        WebSettings settings = tvWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setSupportZoom(false);

        tvWebView.setBackgroundColor(Color.BLACK);

        tvWebView.setWebViewClient(new WebViewClient());

        tvWebView.setWebChromeClient(new WebChromeClient());

        tvWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    // ============================================================
    // SHOW RADIO LIST
    // ============================================================

    private void showRadioList() {

        tvMode = false;

        stopCurrentPlayer();

        tvPlayerView.setVisibility(View.GONE);

        tvWebView.setVisibility(View.GONE);

        radioControls.setVisibility(View.VISIBLE);

        displayNames.clear();
        displayUrls.clear();

        for (String[] radio : RADIO) {

            displayNames.add("📻 " + radio[0]);
            displayUrls.add(radio[1]);
        }

        adapter.notifyDataSetChanged();

        status.setText("📻 Select a radio station");
    }

    // ============================================================
    // SHOW TV LIST
    // ============================================================

    private void showTvList() {

        tvMode = true;

        stopCurrentPlayer();

        tvPlayerView.setVisibility(View.GONE);

        radioControls.setVisibility(View.GONE);

        tvWebView.setVisibility(View.GONE);

        displayNames.clear();
        displayUrls.clear();

        for (String[] tv : TV) {

            displayNames.add("📺 " + tv[0]);
            displayUrls.add(tv[1]);
        }

        adapter.notifyDataSetChanged();

        status.setText("📺 Select a TV channel");
    }

    // ============================================================
    // PLAY SELECTED
    // ============================================================

    private void playSelected(String name, String url) {

        if (tvMode) {

            openOfficialTv(name, url);

        } else {

            playRadio(name, url);
        }
    }

    // ============================================================
    // OFFICIAL TV IN APP
    // ============================================================

    private void openOfficialTv(String name, String url) {

        stopCurrentPlayer();

        tvPlayerView.setVisibility(View.GONE);

        radioControls.setVisibility(View.GONE);

        tvWebView.setVisibility(View.VISIBLE);

        nowPlaying.setText(name);

        status.setText("⏳ Loading official live TV...");

        tvWebView.loadUrl(url);
    }

    // ============================================================
    // RADIO PLAYER
    // ============================================================

    private void playRadio(String name, String url) {

        tvMode = false;

        tvWebView.setVisibility(View.GONE);

        tvPlayerView.setVisibility(View.GONE);

        radioControls.setVisibility(View.VISIBLE);

        nowPlaying.setText(name);

        status.setText("⏳ Loading...");

        stopCurrentPlayer();

        try {

            DefaultHttpDataSource.Factory httpFactory =
                    new DefaultHttpDataSource.Factory()
                            .setUserAgent(
                                    "Mozilla/5.0 (Android) Samaj Sandesh/1.7"
                            )
                            .setAllowCrossProtocolRedirects(true)
                            .setDefaultRequestProperties(
                                    Collections.singletonMap(
                                            "Accept",
                                            "*/*"
                                    )
                            );

            player = new ExoPlayer.Builder(this)
                    .setMediaSourceFactory(
                            new androidx.media3.exoplayer.source.DefaultMediaSourceFactory(
                                    httpFactory
                            )
                    )
                    .build();

            radioControls.setPlayer(player);

            MediaItem mediaItem =
                    new MediaItem.Builder()
                            .setUri(Uri.parse(url))
                            .build();

            player.setMediaItem(mediaItem);

            addPlayerListener();

            player.prepare();

            player.play();

        } catch (Exception e) {

            status.setText(
                    "❌ Radio error:\n" +
                            e.getMessage()
            );
        }
    }

    // ============================================================
    // PLAYER LISTENER
    // ============================================================

    private void addPlayerListener() {

        if (player == null) {
            return;
        }

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int state) {

                if (state == Player.STATE_BUFFERING) {

                    status.setText("⏳ Loading...");

                } else if (state == Player.STATE_READY) {

                    status.setText("▶️ Playing");

                } else if (state == Player.STATE_ENDED) {

                    status.setText("⏹ Playback ended");
                }
            }

            @Override
            public void onPlayerError(
                    PlaybackException error) {

                String errorName =
                        PlaybackException.getErrorCodeName(
                                error.errorCode
                        );

                String message = error.getMessage();

                if (message == null) {
                    message = "";
                }

                status.setText(
                        "❌ Stream error: " +
                                errorName +
                                "\n" +
                                message
                );
            }
        });
    }

    // ============================================================
    // STOP
    // ============================================================

    private void stopPlayback() {

        stopCurrentPlayer();

        if (tvWebView != null) {

            tvWebView.stopLoading();

            tvWebView.setVisibility(View.GONE);
        }

        tvPlayerView.setVisibility(View.GONE);

        radioControls.setVisibility(View.GONE);

        nowPlaying.setText("Nothing playing");

        status.setText("⏹ Stopped");
    }

    // ============================================================
    // STOP PLAYER
    // ============================================================

    private void stopCurrentPlayer() {

        if (radioControls != null) {

            radioControls.setPlayer(null);
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

    // ============================================================
    // FILTER
    // ============================================================

    private void filterList(String query) {

        String q = query.toLowerCase().trim();

        displayNames.clear();
        displayUrls.clear();

        if (tvMode) {

            for (String[] tv : TV) {

                if (tv[0].toLowerCase().contains(q)) {

                    displayNames.add("📺 " + tv[0]);
                    displayUrls.add(tv[1]);
                }
            }

        } else {

            for (String[] radio : RADIO) {

                if (radio[0].toLowerCase().contains(q)) {

                    displayNames.add("📻 " + radio[0]);
                    displayUrls.add(radio[1]);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ============================================================
    // PUBLIC BUTTON METHODS
    // ============================================================

    public void openRadio(View view) {

        showRadioList();
    }

    public void openTv(View view) {

        showTvList();
    }

    // ============================================================
    // BACK BUTTON
    // ============================================================

    @Override
    public void onBackPressed() {

        if (tvWebView != null &&
                tvWebView.getVisibility() == View.VISIBLE) {

            if (tvWebView.canGoBack()) {

                tvWebView.goBack();

            } else {

                tvWebView.setVisibility(View.GONE);

                showTvList();
            }

            return;
        }

        if (player != null) {

            stopCurrentPlayer();

            status.setText("Stopped");

            return;
        }

        super.onBackPressed();
    }

    // ============================================================
    // DESTROY
    // ============================================================

    @Override
    protected void onDestroy() {

        stopCurrentPlayer();

        if (tvWebView != null) {

            tvWebView.stopLoading();

            tvWebView.destroy();
        }

        super.onDestroy();
    }
}
