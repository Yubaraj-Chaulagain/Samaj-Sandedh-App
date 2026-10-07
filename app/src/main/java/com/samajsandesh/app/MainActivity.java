package com.samajsandesh.app;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.Collections;

@UnstableApi
public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private EditText searchBox;
    private TextView nowPlaying;
    private TextView status;

    private PlayerView tvPlayerView;
    private PlayerControlView radioControls;

    private Button stopButton;
    private Button rotateButton;

    private FrameLayout tvPlayerContainer;

    private ExoPlayer player;

    private boolean tvMode = false;


    // ============================================================
    // RADIO CHANNELS
    // ============================================================

    private final String[][] RADIO = {

            {
                    "Kantipur FM 96.1",
                    "https://radio-broadcast.ekantipur.com/stream"
            },

            {
                    "Kalika FM 95.2",
                    "https://streaming.softnep.net:10828/stream"
            },

            {
                    "BBC Nepali",
                    "https://stream.live.vc.bbcmedia.co.uk/bbc_nepali_radio"
            },

            {
                    "CIN Khabar",
                    "https://streaming.softnep.net:10996/;stream.mp3"
            },

            {
                    "Ujyaalo 90 Network",
                    "http://stream.zenolive.com/wtuvp08xq1duv"
            },

            {
                    "Jayaprithvi FM",
                    "https://streaming.softnep.net:10824/"
            },

            {
                    "Butwal FM",
                    "https://streaming.softnep.net:10994/;stream.nsv"
            },

            {
                    "Image FM",
                    "https://www.hamropatro.com/api/radio/stream/9"
            },

            {
                    "Chitwan Radio Network",
                    "https://www.hamropatro.com/api/radio/stream/123"
            },

            {
                    "Makalu FM",
                    "https://www.hamropatro.com/api/radio/stream/318"
            },

            {
                    "Nepali Radio Network",
                    "https://www.hamropatro.com/api/radio/stream/560"
            }
    };


    // ============================================================
    // DIRECT TV HLS STREAMS
    // ============================================================

    private final String[][] TV = {

            {
                    "Nepal Television HD",
                    "https://nepaltv.nettvnepal.com.np/notoken/NTVNEPAL1500.stream/chunks.m3u8"
            },

            {
                    "NTV Plus HD",
                    "https://nepaltv.nettvnepal.com.np/notoken/hd-NtvPlus-1500.stream/chunks.m3u8"
            },

            {
                    "NTV News HD",
                    "https://nepaltv.nettvnepal.com.np/notoken/hd-NtvNews-1500.stream/chunks.m3u8"
            },

            {
                    "Kantipur TV HD",
                    "https://ktvhdnpicc66.ekantipur.com/ktv_abr/hd/playlist.m3u8"
            },

            {
                    "Kantipur TV HD – Backup",
                    "https://ktvhdsg.ekantipur.com:8443/high_quality_85840165/hd/playlist.m3u8"
            }
    };


    // ============================================================
    // DISPLAY LIST
    // ============================================================

    private final ArrayList<String> displayNames =
            new ArrayList<>();

    private final ArrayList<String> displayUrls =
            new ArrayList<>();

    private ArrayAdapter<String> adapter;


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        // ========================================================
        // FIND VIEWS
        // ========================================================

        listView =
                findViewById(R.id.listView);

        searchBox =
                findViewById(R.id.searchBox);

        nowPlaying =
                findViewById(R.id.nowPlaying);

        status =
                findViewById(R.id.status);

        tvPlayerView =
                findViewById(R.id.tvPlayerView);

        radioControls =
                findViewById(R.id.radioControls);

        stopButton =
                findViewById(R.id.stopButton);

        rotateButton =
                findViewById(R.id.rotateButton);

        tvPlayerContainer =
                findViewById(R.id.tvPlayerContainer);


        // ========================================================
        // TV PLAYER SETTINGS
        // ========================================================

        tvPlayerView.setResizeMode(
                AspectRatioFrameLayout.RESIZE_MODE_FIT
        );

        tvPlayerView.setUseController(true);

        tvPlayerView.setShowBuffering(
                PlayerView.SHOW_BUFFERING_WHEN_PLAYING
        );

        tvPlayerView.setControllerShowTimeoutMs(
                3500
        );

        tvPlayerView.setVisibility(
                View.GONE
        );


        // ========================================================
        // RADIO CONTROL
        // ========================================================

        radioControls.setVisibility(
                View.GONE
        );


        // ========================================================
        // ADAPTER
        // ========================================================

        adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        displayNames
                );

        listView.setAdapter(adapter);


        // ========================================================
        // DEFAULT RADIO
        // ========================================================

        showRadioList();


        // ========================================================
        // LIST ITEM CLICK
        // ========================================================

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position >= 0 &&
                            position < displayUrls.size()) {

                        playSelected(
                                displayNames.get(position),
                                displayUrls.get(position)
                        );
                    }
                }
        );


        // ========================================================
        // SEARCH
        // ========================================================

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

                        filterList(
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                }
        );


        // ========================================================
        // STOP BUTTON
        // ========================================================

        stopButton.setOnClickListener(
                v -> stopPlayback()
        );


        // ========================================================
        // ROTATE BUTTON
        // ========================================================

        rotateButton.setOnClickListener(
                v -> toggleOrientation()
        );


        status.setText(
                "Ready"
        );
    }


    // ============================================================
    // SHOW RADIO LIST
    // ============================================================

    private void showRadioList() {

        tvMode = false;

        stopCurrentPlayer();

        tvPlayerContainer.setVisibility(
                View.GONE
        );

        radioControls.setVisibility(
                View.VISIBLE
        );

        displayNames.clear();
        displayUrls.clear();


        for (String[] r : RADIO) {

            displayNames.add(
                    "📻 " + r[0]
            );

            displayUrls.add(
                    r[1]
            );
        }


        adapter.notifyDataSetChanged();

        status.setText(
                "📻 Select a radio station"
        );
    }


    // ============================================================
    // SHOW TV LIST
    // ============================================================

    private void showTvList() {

        tvMode = true;

        stopCurrentPlayer();

        tvPlayerContainer.setVisibility(
                View.GONE
        );

        radioControls.setVisibility(
                View.GONE
        );

        displayNames.clear();
        displayUrls.clear();


        for (String[] t : TV) {

            displayNames.add(
                    "📺 " + t[0]
            );

            displayUrls.add(
                    t[1]
            );
        }


        adapter.notifyDataSetChanged();

        status.setText(
                "📺 Select a TV channel"
        );
    }


    // ============================================================
    // PLAY SELECTED
    // ============================================================

    private void playSelected(
            String name,
            String url) {

        if (tvMode) {

            playTv(
                    name,
                    url
            );

        } else {

            playRadio(
                    name,
                    url
            );
        }
    }


    // ============================================================
    // PLAY TV
    // ============================================================

    private void playTv(
            String name,
            String url) {

        tvMode = true;

        stopCurrentPlayer();


        radioControls.setVisibility(
                View.GONE
        );

        tvPlayerContainer.setVisibility(
                View.VISIBLE
        );


        nowPlaying.setText(
                name
        );

        status.setText(
                "⏳ Connecting to live video..."
        );


        // ========================================================
        // HTTP DATA SOURCE
        // ========================================================

        DefaultHttpDataSource.Factory httpFactory =
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(
                                "Mozilla/5.0 (Android) Samaj Sandesh/1.8"
                        )
                        .setAllowCrossProtocolRedirects(
                                true
                        )
                        .setDefaultRequestProperties(
                                Collections.singletonMap(
                                        "Accept",
                                        "*/*"
                                )
                        );


        // ========================================================
        // EXOPLAYER
        // ========================================================

        player =
                new ExoPlayer.Builder(this)
                        .build();


        tvPlayerView.setPlayer(
                player
        );


        // ========================================================
        // MEDIA ITEM
        // ========================================================

        MediaItem item =
                MediaItem.fromUri(
                        Uri.parse(url)
                );


        // ========================================================
        // HLS SOURCE
        // ========================================================

        HlsMediaSource source =
                new HlsMediaSource.Factory(
                        httpFactory
                ).createMediaSource(
                        item
                );


        player.setMediaSource(
                source
        );


        // ========================================================
        // LISTENER
        // ========================================================

        addPlayerListener();


        // ========================================================
        // START
        // ========================================================

        player.prepare();

        player.play();
    }


    // ============================================================
    // PLAY RADIO
    // ============================================================

    private void playRadio(
            String name,
            String url) {

        tvMode = false;

        stopCurrentPlayer();


        tvPlayerContainer.setVisibility(
                View.GONE
        );

        radioControls.setVisibility(
                View.VISIBLE
        );


        nowPlaying.setText(
                name
        );

        status.setText(
                "⏳ Loading..."
        );


        DefaultHttpDataSource.Factory httpFactory =
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(
                                "Mozilla/5.0 (Android) Samaj Sandesh/1.8"
                        )
                        .setAllowCrossProtocolRedirects(
                                true
                        )
                        .setDefaultRequestProperties(
                                Collections.singletonMap(
                                        "Accept",
                                        "*/*"
                                )
                        );


        player =
                new ExoPlayer.Builder(this)
                        .build();


        radioControls.setPlayer(
                player
        );


        player.setMediaItem(
                MediaItem.fromUri(
                        Uri.parse(url)
                )
        );


        addPlayerListener();


        player.prepare();

        player.play();
    }


    // ============================================================
    // PLAYER LISTENER
    // ============================================================

    private void addPlayerListener() {

        if (player == null) {
            return;
        }


        player.addListener(
                new Player.Listener() {

                    @Override
                    public void onPlaybackStateChanged(
                            int state) {

                        if (state ==
                                Player.STATE_BUFFERING) {

                            status.setText(
                                    "⏳ Buffering..."
                            );

                        } else if (state ==
                                Player.STATE_READY) {

                            status.setText(
                                    "▶️ LIVE"
                            );

                        } else if (state ==
                                Player.STATE_ENDED) {

                            status.setText(
                                    "⏹ Playback ended"
                            );
                        }
                    }


                    @Override
                    public void onPlayerError(
                            PlaybackException error) {

                        status.setText(
                                "❌ Stream unavailable. Try another channel."
                        );
                    }
                }
        );
    }


    // ============================================================
    // STOP PLAYBACK
    // ============================================================

    private void stopPlayback() {

        stopCurrentPlayer();

        tvPlayerContainer.setVisibility(
                View.GONE
        );

        radioControls.setVisibility(
                View.GONE
        );


        nowPlaying.setText(
                "Nothing playing"
        );

        status.setText(
                "⏹ Stopped"
        );
    }


    // ============================================================
    // STOP CURRENT PLAYER
    // ============================================================

    private void stopCurrentPlayer() {

        if (radioControls != null) {

            radioControls.setPlayer(
                    null
            );
        }


        if (tvPlayerView != null) {

            tvPlayerView.setPlayer(
                    null
            );
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
    // SEARCH FILTER
    // ============================================================

    private void filterList(
            String query) {

        String q =
                query.toLowerCase().trim();


        displayNames.clear();

        displayUrls.clear();


        String[][] source =
                tvMode ? TV : RADIO;


        for (String[] item : source) {

            if (item[0]
                    .toLowerCase()
                    .contains(q)) {

                displayNames.add(
                        (tvMode
                                ? "📺 "
                                : "📻 ")
                                + item[0]
                );

                displayUrls.add(
                        item[1]
                );
            }
        }


        adapter.notifyDataSetChanged();
    }


    // ============================================================
    // RADIO BUTTON
    // ============================================================

    public void openRadio(
            View view) {

        exitFullscreen();

        showRadioList();
    }


    // ============================================================
    // TV BUTTON
    // ============================================================

    public void openTv(
            View view) {

        exitFullscreen();

        showTvList();
    }


    // ============================================================
    // ROTATE / FULLSCREEN
    // ============================================================

    private void toggleOrientation() {

        if (getResources()
                .getConfiguration()
                .orientation
                ==
                Configuration.ORIENTATION_LANDSCAPE) {

            exitFullscreen();

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            );

        } else {

            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            );

            enterFullscreen();
        }
    }


    // ============================================================
    // ENTER FULLSCREEN
    // ============================================================

    private void enterFullscreen() {

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        getWindow()
                .getDecorView()
                .setSystemUiVisibility(

                        View.SYSTEM_UI_FLAG_FULLSCREEN |

                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |

                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |

                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |

                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |

                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );


        if (tvPlayerContainer != null) {

            tvPlayerContainer.setVisibility(
                    View.VISIBLE
            );
        }


        hideNonVideoViews(
                true
        );
    }


    // ============================================================
    // EXIT FULLSCREEN
    // ============================================================

    private void exitFullscreen() {

        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );


        hideNonVideoViews(
                false
        );
    }


    // ============================================================
    // HIDE OTHER UI DURING FULLSCREEN
    // ============================================================

    private void hideNonVideoViews(
            boolean hide) {

        int v =
                hide
                        ? View.GONE
                        : View.VISIBLE;


        findViewById(
                R.id.header
        ).setVisibility(v);


        findViewById(
                R.id.modeButtons
        ).setVisibility(v);


        searchBox.setVisibility(v);

        nowPlaying.setVisibility(v);

        status.setVisibility(v);

        stopButton.setVisibility(v);

        listView.setVisibility(v);


        if (hide) {

            tvPlayerContainer.setVisibility(
                    View.VISIBLE
            );

            tvPlayerView.setVisibility(
                    View.VISIBLE
            );
        }
    }


    // ============================================================
    // SCREEN ROTATION CHANGE
    // ============================================================

    @Override
    public void onConfigurationChanged(
            Configuration newConfig) {

        super.onConfigurationChanged(
                newConfig
        );


        if (newConfig.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
                &&
                tvMode
                &&
                player != null) {

            enterFullscreen();

        } else if (
                newConfig.orientation ==
                        Configuration.ORIENTATION_PORTRAIT) {

            exitFullscreen();
        }
    }


    // ============================================================
    // BACK BUTTON
    // ============================================================

    @Override
    public void onBackPressed() {

        if (
                getResources()
                        .getConfiguration()
                        .orientation
                        ==
                        Configuration.ORIENTATION_LANDSCAPE
                        &&
                        player != null
        ) {

            toggleOrientation();

            return;
        }


        if (player != null) {

            stopCurrentPlayer();

            status.setText(
                    "Stopped"
            );

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

        super.onDestroy();
    }
}
