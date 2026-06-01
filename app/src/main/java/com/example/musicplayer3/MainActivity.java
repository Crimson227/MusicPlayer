package com.example.musicplayer3;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.database.Cursor;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_AUDIO = 10;
    private static final int REQUEST_LYRICS = 11;
    private static final int REQUEST_BACKGROUND = 12;
    private static final int REQUEST_COVER = 13;
    private static final String PREFS = "studio_player_state";
    private static final String KEY_STATE = "state";

    private final ArrayList<Song> librarySongs = new ArrayList<>();
    private final ArrayList<Playlist> playlists = new ArrayList<>();
    private final Handler handler = new Handler();
    private final Random random = new Random();
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private boolean prepared;
    private boolean userSeeking;
    private int activePlaylistIndex;
    private int currentSongIndex = -1;
    private int playMode;
    private int themeIndexValue;
    private int activeTab;
    private int activeLyricLineIndex = -1;
    private String backgroundUri = "";
    private String searchQuery = "";
    private final Set<String> favoriteUris = new HashSet<>();
    private final ArrayList<String> recentPlayedUris = new ArrayList<>();
    private final ArrayList<String> recentSearchQueries = new ArrayList<>();

    private ImageView backgroundImage;
    private ScrollView libraryScroll;
    private FrameLayout playerOverlay;
    private LinearLayout libraryMenu;
    private LinearLayout songList;
    private LinearLayout miniPlayer;
    private TextView miniTitle;
    private TextView miniSubtitle;
    private TextView pageTitle;
    private TextView sectionTitle;
    private ImageView miniCover;
    private ImageView playerCover;
    private TextView statusText;
    private TextView songTitle;
    private TextView songMeta;
    private TextView lyricsView;
    private TextView currentTime;
    private TextView totalTime;
    private SeekBar progressSeek;
    private SeekBar volumeSeek;
    private ImageButton playPauseButton;
    private ImageButton miniPlayButton;
    private ImageButton modeButton;
    private ImageButton favoriteButton;
    private LinearLayout tabLibrary;
    private LinearLayout tabSearch;
    private LinearLayout tabLikes;
    private LinearLayout tabMine;
    private TextView tabLibraryIcon;
    private TextView tabSearchIcon;
    private TextView tabLikesIcon;
    private TextView tabMineIcon;
    private TextView tabLibraryLabel;
    private TextView tabSearchLabel;
    private TextView tabLikesLabel;
    private TextView tabMineLabel;

    private final Runnable progressTicker = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        bindViews();
        applyInsets();
        loadState();
        applyTheme();
        wireActions();
        refreshAll();
        handler.post(progressTicker);
    }

    private void bindViews() {
        backgroundImage = findViewById(R.id.backgroundImage);
        libraryScroll = findViewById(R.id.libraryScroll);
        playerOverlay = findViewById(R.id.playerOverlay);
        libraryMenu = findViewById(R.id.libraryMenu);
        songList = findViewById(R.id.songList);
        miniPlayer = findViewById(R.id.miniPlayer);
        miniTitle = findViewById(R.id.miniTitle);
        miniSubtitle = findViewById(R.id.miniSubtitle);
        pageTitle = findViewById(R.id.pageTitle);
        sectionTitle = findViewById(R.id.sectionTitle);
        miniCover = findViewById(R.id.miniCover);
        playerCover = findViewById(R.id.playerCover);
        statusText = findViewById(R.id.statusText);
        songTitle = findViewById(R.id.songTitle);
        songMeta = findViewById(R.id.songMeta);
        lyricsView = findViewById(R.id.lyricsView);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        progressSeek = findViewById(R.id.progressSeek);
        volumeSeek = findViewById(R.id.volumeSeek);
        playPauseButton = findViewById(R.id.playPauseButton);
        miniPlayButton = findViewById(R.id.miniPlayButton);
        modeButton = findViewById(R.id.modeButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        tabLibrary = findViewById(R.id.tabLibrary);
        tabSearch = findViewById(R.id.tabSearch);
        tabLikes = findViewById(R.id.tabLikes);
        tabMine = findViewById(R.id.tabMine);
        tabLibraryIcon = findViewById(R.id.tabLibraryIcon);
        tabSearchIcon = findViewById(R.id.tabSearchIcon);
        tabLikesIcon = findViewById(R.id.tabLikesIcon);
        tabMineIcon = findViewById(R.id.tabMineIcon);
        tabLibraryLabel = findViewById(R.id.tabLibraryLabel);
        tabSearchLabel = findViewById(R.id.tabSearchLabel);
        tabLikesLabel = findViewById(R.id.tabLikesLabel);
        tabMineLabel = findViewById(R.id.tabMineLabel);
    }

    private void applyInsets() {
        View root = findViewById(R.id.contentRoot);
        View playerContent = findViewById(R.id.playerContent);
        View bottomNav = findViewById(R.id.bottomNav);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(dp(22) + bars.left, dp(22) + bars.top, dp(22) + bars.right, dp(176) + bars.bottom);
            playerContent.setPadding(dp(32) + bars.left, dp(28) + bars.top, dp(32) + bars.right, dp(30) + bars.bottom);
            if (bottomNav != null) {
                ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
                int targetHeight = dp(64) + bars.bottom;
                if (params.height != targetHeight) {
                    params.height = targetHeight;
                    bottomNav.setLayoutParams(params);
                }
                bottomNav.setPadding(dp(14) + bars.left, dp(4), dp(14) + bars.right, dp(4) + bars.bottom);
            }
            if (miniPlayer != null && miniPlayer.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) miniPlayer.getLayoutParams();
                params.setMargins(dp(22) + bars.left, 0, dp(22) + bars.right, dp(84) + bars.bottom);
                miniPlayer.setLayoutParams(params);
            }
            return insets;
        });
    }

    private void wireActions() {
        setAnimatedClickListener(findViewById(R.id.createPlaylistButton), v -> openAudioPicker());
        setAnimatedClickListener(findViewById(R.id.deletePlaylistButton), v -> showTopMoreDialog());
        setAnimatedClickListener(findViewById(R.id.importButton), v -> showEnhancedManageDialog());
        setAnimatedClickListener(findViewById(R.id.previousButton), v -> playPrevious());
        setAnimatedClickListener(findViewById(R.id.nextButton), v -> playNext(false));
        setAnimatedClickListener(findViewById(R.id.miniNextButton), v -> playNext(false));
        setAnimatedClickListener(findViewById(R.id.stopButton), v -> stopPlayback());
        setAnimatedClickListener(playPauseButton, v -> togglePlayback());
        setAnimatedClickListener(miniPlayButton, v -> togglePlayback());
        setAnimatedClickListener(modeButton, v -> cyclePlayMode());
        setAnimatedClickListener(favoriteButton, v -> toggleFavoriteCurrentSong());
        setAnimatedClickListener(miniPlayer, v -> showPlayer());
        setAnimatedClickListener(tabLibrary, v -> switchTab(0));
        setAnimatedClickListener(tabSearch, v -> switchTab(1));
        setAnimatedClickListener(tabLikes, v -> switchTab(2));
        setAnimatedClickListener(tabMine, v -> switchTab(3));
        setAnimatedClickListener(findViewById(R.id.showLibraryButton), v -> hidePlayer());
        setAnimatedClickListener(findViewById(R.id.collapseHandle), v -> hidePlayer());
        setupSeekBars();
    }

    private void setupSeekBars() {
        progressSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentTime.setText(formatTime(progress));
                    updateRemainingTime(progress);
                    updateLyrics(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && prepared) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
                userSeeking = false;
            }
        });
        if (audioManager != null) {
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            volumeSeek.setMax(max);
            volumeSeek.setProgress(current);
            volumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        }
    }

    private void setAnimatedClickListener(View view, View.OnClickListener listener) {
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> {
            playClickAnimation(v);
            listener.onClick(v);
        });
    }

    private void playClickAnimation(View view) {
        view.animate().cancel();
        view.setScaleX(0.94f);
        view.setScaleY(0.94f);
        view.setAlpha(0.82f);
        view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(150).start();
    }

    private void switchTab(int tab) {
        int target = Math.max(0, Math.min(tab, 3));
        if (activeTab == target) {
            animateTabContent();
            return;
        }
        activeTab = target;
        saveState();
        refreshAll();
        animateTabContent();
    }

    private void showPlayer() {
        playerOverlay.setAlpha(0f);
        playerOverlay.setTranslationY(dp(60));
        playerOverlay.setVisibility(View.VISIBLE);
        playerOverlay.animate().alpha(1f).translationY(0).setDuration(220).start();
    }

    private void hidePlayer() {
        playerOverlay.animate().alpha(0f).translationY(dp(60)).setDuration(180)
                .withEndAction(() -> playerOverlay.setVisibility(View.GONE)).start();
    }

    private void animateTabContent() {
        View[] views = {pageTitle, libraryMenu, sectionTitle, songList};
        for (View view : views) {
            if (view == null) {
                continue;
            }
            view.setAlpha(0f);
            view.setTranslationY(dp(18));
            view.animate().alpha(1f).translationY(0).setDuration(220).start();
        }
    }

    private void showEnhancedManageDialog() {
        String[] actions = {"查看歌曲详情", "关联歌词", "设置歌曲封面", "设置播放背景", "从当前列表移除"};
        String[] subtitles = {"查看歌词、封面与文件信息", "选择 txt 或 lrc 歌词文件", "为歌曲指定封面图片", "设置播放详情页背景", "仅从当前播放列表移除"};
        showMenuDialog("当前歌曲设置", actions, subtitles, which -> {
            if (which == 0) showCurrentSongDetails();
            if (which == 1) openLyricsPicker();
            if (which == 2) openCoverPicker();
            if (which == 3) openBackgroundPicker();
            if (which == 4) deleteCurrentSong();
        });
    }

    private void showManageMusicDialog() {
        String[] actions = {
                "导入到资料库",
                "从资料库添加到当前列表",
                "创建播放列表",
                "删除当前播放列表",
                "关联歌词",
                "设置歌曲封面",
                "添加播放背景",
                "从当前列表移除歌曲"
        };
        String[] subtitles = {
                "选择本机音频文件",
                "把资料库歌曲加入当前列表",
                "新建一个播放列表",
                "移除当前播放列表",
                "为当前歌曲选择歌词文件",
                "为当前歌曲指定封面",
                "设置播放详情页背景",
                "仅从当前列表移除歌曲"
        };
        showMenuDialog("管理音乐", actions, subtitles, which -> {
            if (which == 0) openAudioPicker();
            if (which == 1) showAddFromLibraryDialog();
            if (which == 2) showCreatePlaylistDialog();
            if (which == 3) deleteCurrentPlaylist();
            if (which == 4) openLyricsPicker();
            if (which == 5) openCoverPicker();
            if (which == 6) openBackgroundPicker();
            if (which == 7) deleteCurrentSong();
        });
    }

    private void showTopMoreDialog() {
        if (activeTab == 0) {
            showPlaylistManagementDialog();
        } else if (activeTab == 1) {
            showSearchToolsDialog();
        } else if (activeTab == 2) {
            showFavoritesToolsDialog();
        } else {
            showAboutDialog();
        }
    }

    private void showPlaylistManagementDialog() {
        String[] actions = {"切换播放列表", "创建播放列表", "删除当前播放列表"};
        String[] subtitles = {"选择当前使用的播放列表", "新建一个空歌单并切换过去", "至少保留一个播放列表"};
        showMenuDialog("播放列表管理", actions, subtitles, which -> {
            if (which == 0) choosePlaylist();
            if (which == 1) showCreatePlaylistDialog();
            if (which == 2) deleteCurrentPlaylist();
        });
    }

    private void showSearchToolsDialog() {
        String[] actions = {"清除搜索", "清理最近搜索", "导入音乐"};
        String[] subtitles = {"恢复搜索初始状态", "删除所有最近搜索关键词", "从设备中选择歌曲"};
        showMenuDialog("搜索工具", actions, subtitles, which -> {
            if (which == 0) {
                searchQuery = "";
                saveState();
                refreshAll();
            }
            if (which == 1) {
                recentSearchQueries.clear();
                saveState();
                refreshAll();
            }
            if (which == 2) openAudioPicker();
        });
    }

    private void showFavoritesToolsDialog() {
        String[] actions = {"去资料库选择歌曲", "查看最近播放"};
        String[] subtitles = {"从全部歌曲中播放或收藏", "打开播放记录"};
        showMenuDialog("喜欢", actions, subtitles, which -> {
            if (which == 0) switchTab(0);
            if (which == 1) showRecentPlayedDialog();
        });
    }

    private void showSearchDialog() {
        EditText input = new EditText(this);
        input.setHint("输入歌曲名");
        input.setSingleLine(true);
        input.setText(searchQuery);
        input.setSelection(input.getText().length());
        showInputPanel("搜索歌曲", "输入歌曲名", searchQuery, "搜索", text -> {
            searchQuery = text;
            addRecentSearchQuery(searchQuery);
            saveState();
            refreshAll();
        });
    }

    private void showFavoritesDialog() {
        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : librarySongs) {
            if (favoriteUris.contains(song.uri)) {
                songs.add(song);
            }
        }
        showSongPickerDialog("我喜欢", songs);
    }

    private void showRecentPlayedDialog() {
        ArrayList<Song> songs = new ArrayList<>();
        for (String uri : recentPlayedUris) {
            Song song = findLibrarySong(uri);
            if (song != null && !songs.contains(song)) {
                songs.add(song);
            }
        }
        showSongPickerDialog("最近播放", songs);
    }

    private void showSongPickerDialog(String title, ArrayList<Song> songs) {
        if (songs.isEmpty()) {
            toast(title + "暂无歌曲");
            return;
        }
        String[] names = new String[songs.size()];
        String[] subtitles = new String[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            names[i] = songs.get(i).title;
            subtitles[i] = buildSongSubtitle(songs.get(i));
        }
        showMenuDialog(title, names, subtitles, which -> playSongFromLibrary(songs.get(which)));
    }

    private void playSongFromLibrary(Song song) {
        Playlist playlist = getActivePlaylist();
        int index = playlist.songs.indexOf(song);
        if (index < 0) {
            playlist.songs.add(song);
            index = playlist.songs.size() - 1;
        }
        prepareAndMaybePlay(index, true);
        saveState();
        refreshAll();
        showPlayer();
    }

    private void toggleFavoriteCurrentSong() {
        toggleFavorite(getCurrentSong());
    }

    private void showCurrentSongDetails() {
        Song song = getCurrentSong();
        if (song == null) {
            toast("请先选择一首歌曲");
            return;
        }
        showSongDetails(song);
    }

    private void showSongDetails(Song song) {
        StringBuilder builder = new StringBuilder();
        builder.append("歌曲名称：").append(song.title).append("\n");
        builder.append("歌手：").append(TextUtils.isEmpty(song.artist) ? "未知歌手" : song.artist).append("\n");
        builder.append("专辑：").append(TextUtils.isEmpty(song.album) ? "未知专辑" : song.album).append("\n");
        builder.append("当前播放列表：").append(getActivePlaylist().name).append("\n");
        builder.append("歌词状态：").append(song.lyricsUri.isEmpty() ? "未关联" : "已关联").append("\n");
        builder.append("封面状态：").append(song.coverUri.isEmpty() ? (song.embeddedCoverUri.isEmpty() ? "默认封面" : "自动解析") : "已设置").append("\n");
        builder.append("导入时间：").append(song.addedAt > 0 ? formatDateTime(song.addedAt) : "未知").append("\n");
        builder.append("文件名：").append(TextUtils.isEmpty(song.fileName) ? song.title : song.fileName).append("\n");
        builder.append("文件地址：").append(song.uri);
        showMessageDialog("歌曲详情", builder.toString());
    }

    private void showActionPanel(String title, String[] names, String[] subtitles, String[] icons, int checkedIndex, PanelAction action) {
        String[] displayNames = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            String marker = checkedIndex == i ? "✓ " : "";
            String icon = icons == null ? "" : icons[i] + "  ";
            displayNames[i] = marker + icon + names[i];
        }
        showMenuDialog(title, displayNames, subtitles, action);
    }

    private void showMenuDialog(String title, String[] names, String[] subtitles, PanelAction action) {
        Dialog dialog = new Dialog(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(20), dp(22), dp(14));
        applyRoundBackground(panel, Color.WHITE, 20);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(themeText());
        titleView.setTextSize(22);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(8));
        panel.addView(titleView, titleParams);

        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(rows);
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(12), 0, dp(12));
            row.setClickable(true);
            row.setFocusable(true);

            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            TextView nameView = new TextView(this);
            nameView.setText(names[i]);
            nameView.setTextColor(themeText());
            nameView.setTextSize(17);
            nameView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            texts.addView(nameView);
            if (subtitles != null && i < subtitles.length && !TextUtils.isEmpty(subtitles[i])) {
                TextView subtitleView = new TextView(this);
                subtitleView.setText(subtitles[i]);
                subtitleView.setTextColor(themeSubtext());
                subtitleView.setTextSize(13);
                subtitleView.setSingleLine(true);
                subtitleView.setEllipsize(TextUtils.TruncateAt.END);
                texts.addView(subtitleView);
            }
            row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView arrow = new TextView(this);
            arrow.setText("›");
            arrow.setTextColor(Color.rgb(196, 198, 204));
            arrow.setGravity(Gravity.CENTER);
            arrow.setTextSize(30);
            row.addView(arrow, new LinearLayout.LayoutParams(dp(28), dp(52)));
            rows.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (i < names.length - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(Color.rgb(235, 235, 238));
                rows.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(1))));
            }
            setAnimatedClickListener(row, v -> {
                dialog.dismiss();
                action.onSelect(index);
            });
        }
        int menuHeight = Math.min(dp(440), Math.max(dp(56), dp(64) * names.length));
        panel.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, menuHeight));
        showCustomDialog(dialog, panel);
    }

    private void showInputPanel(String title, String hint, String initialValue, String positiveText, TextAction action) {
        Dialog dialog = new Dialog(this);
        LinearLayout panel = createDialogPanel(title);
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(hint);
        input.setText(initialValue == null ? "" : initialValue);
        input.setSelection(input.getText().length());
        input.setTextColor(themeText());
        input.setHintTextColor(themeSubtext());
        input.setTextSize(16);
        input.setPadding(dp(14), dp(8), dp(14), dp(8));
        applyRoundBackground(input, Color.rgb(247, 243, 243), 14);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        inputParams.setMargins(0, dp(16), 0, dp(18));
        panel.addView(input, inputParams);

        LinearLayout buttons = createDialogButtonRow();
        TextView cancel = createDialogButton("取消", false);
        TextView positive = createDialogButton(positiveText, true);
        buttons.addView(cancel, new LinearLayout.LayoutParams(0, dp(44), 1));
        buttons.addView(positive, new LinearLayout.LayoutParams(0, dp(44), 1));
        panel.addView(buttons);
        setAnimatedClickListener(cancel, v -> dialog.dismiss());
        setAnimatedClickListener(positive, v -> {
            dialog.dismiss();
            action.onSubmit(input.getText().toString().trim());
        });
        showCustomDialog(dialog, panel);
    }

    private void showConfirmPanel(String title, String message, String positiveText, Runnable action) {
        Dialog dialog = new Dialog(this);
        LinearLayout panel = createDialogPanel(title);
        TextView messageView = createDialogMessage(message);
        panel.addView(messageView);
        LinearLayout buttons = createDialogButtonRow();
        TextView cancel = createDialogButton("取消", false);
        TextView positive = createDialogButton(positiveText, true);
        buttons.addView(cancel, new LinearLayout.LayoutParams(0, dp(44), 1));
        buttons.addView(positive, new LinearLayout.LayoutParams(0, dp(44), 1));
        panel.addView(buttons);
        setAnimatedClickListener(cancel, v -> dialog.dismiss());
        setAnimatedClickListener(positive, v -> {
            dialog.dismiss();
            action.run();
        });
        showCustomDialog(dialog, panel);
    }

    private void showMessageDialog(String title, String message) {
        Dialog dialog = new Dialog(this);
        LinearLayout panel = createDialogPanel(title);
        panel.addView(createDialogMessage(message));
        LinearLayout buttons = createDialogButtonRow();
        TextView ok = createDialogButton("知道了", true);
        buttons.addView(ok, new LinearLayout.LayoutParams(0, dp(44), 1));
        panel.addView(buttons);
        setAnimatedClickListener(ok, v -> dialog.dismiss());
        showCustomDialog(dialog, panel);
    }

    private LinearLayout createDialogPanel(String title) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(22), dp(22), dp(18));
        applyRoundBackground(panel, Color.WHITE, 22);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(themeText());
        titleView.setTextSize(22);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        panel.addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return panel;
    }

    private TextView createDialogMessage(String message) {
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextColor(themeSubtext());
        messageView.setTextSize(15);
        messageView.setLineSpacing(dp(2), 1.08f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(14), 0, dp(18));
        messageView.setLayoutParams(params);
        return messageView;
    }

    private LinearLayout createDialogButtonRow() {
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        buttons.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        buttons.setDividerPadding(dp(8));
        return buttons;
    }

    private TextView createDialogButton(String text, boolean primary) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(15);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(primary ? Color.WHITE : themeText());
        applyRoundBackground(button, primary ? themeAccent() : Color.rgb(247, 243, 243), 14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(44), 1);
        params.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void showCustomDialog(Dialog dialog, View content) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        content.setAlpha(0f);
        content.setScaleX(0.96f);
        content.setScaleY(0.96f);
        content.setTranslationY(dp(10));
        dialog.setContentView(content);
        configureDialogWindow(dialog);
        dialog.setOnShowListener(d -> {
            configureDialogWindow(dialog);
            content.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f).setDuration(150).start();
        });
        dialog.show();
    }

    private void configureDialogWindow(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = getResources().getDisplayMetrics().widthPixels - dp(44);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.dimAmount = 0.34f;
        window.setAttributes(params);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
    private interface PanelAction {
        void onSelect(int index);
    }

    private interface TextAction {
        void onSubmit(String text);
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_AUDIO);
    }

    private void openLyricsPicker() {
        if (getCurrentSong() == null) {
            toast("请先选择一首歌曲");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/*", "application/octet-stream"});
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_LYRICS);
    }

    private void openBackgroundPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_BACKGROUND);
    }

    private void openCoverPicker() {
        if (getCurrentSong() == null) {
            toast("请先选择一首歌曲");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_COVER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQUEST_AUDIO) {
            importSongs(data);
        } else if (requestCode == REQUEST_LYRICS && data.getData() != null) {
            linkLyrics(data.getData());
        } else if (requestCode == REQUEST_BACKGROUND && data.getData() != null) {
            takeReadPermission(data.getData());
            backgroundUri = data.getData().toString();
            saveState();
            refreshBackground();
            toast("背景已更新");
        } else if (requestCode == REQUEST_COVER && data.getData() != null) {
            takeReadPermission(data.getData());
            Song song = getCurrentSong();
            if (song != null) {
                song.coverUri = data.getData().toString();
                saveState();
                refreshAll();
                toast("歌曲封面已更新");
            }
        }
    }

    private void importSongs(Intent data) {
        Playlist playlist = getActivePlaylist();
        int beforeLibrary = librarySongs.size();
        int beforePlaylist = playlist.songs.size();
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                addSongFromUri(playlist, clipData.getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            addSongFromUri(playlist, data.getData());
        }
        if (currentSongIndex < 0 && !playlist.songs.isEmpty()) {
            currentSongIndex = 0;
        }
        saveState();
        refreshAll();
        toast("资料库新增 " + (librarySongs.size() - beforeLibrary) + " 首，当前列表加入 " + (playlist.songs.size() - beforePlaylist) + " 首");
    }

    private void addSongFromUri(Playlist playlist, Uri uri) {
        takeReadPermission(uri);
        Song song = findLibrarySong(uri.toString());
        if (song == null) {
            String fileName = queryFileName(uri);
            ParsedSongName parsedName = parseSongName(fileName);
            AudioMetadata metadata = readAudioMetadata(uri);
            String title = firstNonEmpty(metadata.title, parsedName.title);
            if (looksLikeCombinedArtistTitle(title)) {
                title = parsedName.title;
            }
            song = new Song(title, uri.toString());
            song.fileName = fileName;
            song.artist = firstNonEmpty(metadata.artist, parsedName.artist);
            song.album = metadata.album;
            song.embeddedCoverUri = metadata.embeddedCoverUri;
            song.addedAt = System.currentTimeMillis();
            librarySongs.add(song);
        } else if (looksLikeCombinedArtistTitle(song.title)
                || TextUtils.isEmpty(song.artist)
                || TextUtils.isEmpty(song.album)
                || TextUtils.isEmpty(song.embeddedCoverUri)) {
            ParsedSongName parsedName = parseSongName(song.fileName);
            AudioMetadata metadata = readAudioMetadata(uri);
            if (looksLikeCombinedArtistTitle(song.title)) {
                song.title = parsedName.title;
            }
            if (TextUtils.isEmpty(song.artist)) {
                song.artist = firstNonEmpty(metadata.artist, parsedName.artist);
            }
            if (TextUtils.isEmpty(song.album)) {
                song.album = metadata.album;
            }
            if (TextUtils.isEmpty(song.embeddedCoverUri)) {
                song.embeddedCoverUri = metadata.embeddedCoverUri;
            }
        }
        addSongToPlaylist(playlist, song);
    }

    private Song findLibrarySong(String uri) {
        for (Song song : librarySongs) {
            if (song.uri.equals(uri)) {
                return song;
            }
        }
        return null;
    }

    private boolean addSongToPlaylist(Playlist playlist, Song song) {
        for (Song existing : playlist.songs) {
            if (existing.uri.equals(song.uri)) {
                return false;
            }
        }
        playlist.songs.add(song);
        return true;
    }

    private void showAddFromLibraryDialog() {
        if (librarySongs.isEmpty()) {
            toast("资料库还没有歌曲，请先导入音乐");
            openAudioPicker();
            return;
        }
        Playlist playlist = getActivePlaylist();
        boolean[] checked = new boolean[librarySongs.size()];
        for (int i = 0; i < librarySongs.size(); i++) {
            Song song = librarySongs.get(i);
            checked[i] = playlistContains(playlist, song.uri);
        }
        Dialog dialog = new Dialog(this);
        LinearLayout panel = createDialogPanel("添加到“" + playlist.name + "”");

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        for (int i = 0; i < librarySongs.size(); i++) {
            final int index = i;
            Song song = librarySongs.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(10), 0, dp(10));
            TextView check = new TextView(this);
            check.setText(checked[index] ? "✓" : "");
            check.setTextColor(Color.WHITE);
            check.setGravity(Gravity.CENTER);
            check.setTextSize(16);
            applyRoundBackground(check, checked[index] ? themeAccent() : Color.rgb(247, 243, 243), 12);
            row.addView(check, new LinearLayout.LayoutParams(dp(34), dp(34)));

            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            TextView name = new TextView(this);
            name.setText(song.title);
            name.setTextColor(themeText());
            name.setTextSize(16);
            name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            texts.addView(name);
            TextView subtitle = new TextView(this);
            subtitle.setText(buildSongSubtitle(song));
            subtitle.setTextColor(themeSubtext());
            subtitle.setTextSize(13);
            texts.addView(subtitle);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            textParams.setMargins(dp(12), 0, 0, 0);
            row.addView(texts, textParams);
            setAnimatedClickListener(row, v -> {
                checked[index] = !checked[index];
                check.setText(checked[index] ? "✓" : "");
                applyRoundBackground(check, checked[index] ? themeAccent() : Color.rgb(247, 243, 243), 12);
            });
            list.addView(row);
        }
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.min(dp(360), dp(58) * Math.max(1, librarySongs.size())));
        scrollParams.setMargins(0, dp(12), 0, dp(18));
        panel.addView(scroll, scrollParams);

        LinearLayout buttons = createDialogButtonRow();
        TextView cancel = createDialogButton("取消", false);
        TextView done = createDialogButton("完成", true);
        buttons.addView(cancel, new LinearLayout.LayoutParams(0, dp(44), 1));
        buttons.addView(done, new LinearLayout.LayoutParams(0, dp(44), 1));
        panel.addView(buttons);
        setAnimatedClickListener(cancel, v -> dialog.dismiss());
        setAnimatedClickListener(done, v -> {
            playlist.songs.clear();
            for (int i = 0; i < checked.length; i++) {
                if (checked[i]) {
                    playlist.songs.add(librarySongs.get(i));
                }
            }
            currentSongIndex = playlist.songs.isEmpty() ? -1 : Math.min(Math.max(0, currentSongIndex), playlist.songs.size() - 1);
            saveState();
            refreshAll();
            dialog.dismiss();
        });
        showCustomDialog(dialog, panel);
    }

    private boolean playlistContains(Playlist playlist, String uri) {
        for (Song song : playlist.songs) {
            if (song.uri.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    private void linkLyrics(Uri uri) {
        takeReadPermission(uri);
        Song song = getCurrentSong();
        if (song == null) {
            return;
        }
        song.lyricsUri = uri.toString();
        song.lyricLines = readLyrics(uri);
        saveState();
        refreshNowPlaying();
        toast("歌词已关联");
    }

    private void takeReadPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
        }
    }

    private String queryDisplayName(Uri uri) {
        return cleanTitle(queryFileName(uri));
    }

    private String queryFileName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        }
        return uri.getLastPathSegment() == null ? "未命名歌曲" : uri.getLastPathSegment();
    }

    private String cleanTitle(String value) {
        return value == null ? "未命名歌曲" : value.replaceAll("\\.[A-Za-z0-9]{2,5}$", "");
    }

    private ParsedSongName parseSongName(String fileName) {
        String clean = cleanTitle(fileName).trim();
        Pattern pattern = Pattern.compile("^(.{1,40}?)\\s+[-–—_]\\s+(.{1,100})$");
        Matcher matcher = pattern.matcher(clean);
        if (matcher.matches()) {
            String artist = cleanMetadataText(matcher.group(1));
            String title = cleanMetadataText(matcher.group(2));
            if (!artist.isEmpty() && !title.isEmpty()) {
                return new ParsedSongName(title, artist);
            }
        }
        return new ParsedSongName(clean.isEmpty() ? "未命名歌曲" : clean, "");
    }

    private boolean looksLikeCombinedArtistTitle(String value) {
        return value != null && Pattern.compile("^.{1,40}?\\s+[-–—_]\\s+.{1,100}$").matcher(value.trim()).matches();
    }

    private AudioMetadata readAudioMetadata(Uri uri) {
        AudioMetadata metadata = new AudioMetadata();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            metadata.title = cleanMetadataText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            metadata.artist = cleanMetadataText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            metadata.album = cleanMetadataText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            metadata.embeddedCoverUri = saveEmbeddedCover(uri, retriever.getEmbeddedPicture());
        } catch (Exception ignored) {
        } finally {
            try {
                retriever.release();
            } catch (IOException | RuntimeException ignored) {
            }
        }
        return metadata;
    }

    private String cleanMetadataText(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.trim();
        return clean.isEmpty() || "<unknown>".equalsIgnoreCase(clean) ? "" : clean;
    }

    private String firstNonEmpty(String preferred, String fallback) {
        return TextUtils.isEmpty(preferred) ? fallback : preferred;
    }

    private String saveEmbeddedCover(Uri audioUri, byte[] coverBytes) {
        if (coverBytes == null || coverBytes.length == 0) {
            return "";
        }
        File directory = new File(getFilesDir(), "metadata_covers");
        if (!directory.exists() && !directory.mkdirs()) {
            return "";
        }
        File coverFile = new File(directory, "cover_" + Math.abs(audioUri.toString().hashCode()) + ".jpg");
        try (FileOutputStream output = new FileOutputStream(coverFile)) {
            output.write(coverBytes);
            return Uri.fromFile(coverFile).toString();
        } catch (IOException ignored) {
            return "";
        }
    }

    private String buildMetadataSubtitle(Song song) {
        ArrayList<String> parts = new ArrayList<>();
        if (!TextUtils.isEmpty(song.artist)) {
            parts.add(song.artist);
        }
        if (!TextUtils.isEmpty(song.album)) {
            parts.add(song.album);
        }
        if (parts.isEmpty()) {
            parts.add(TextUtils.isEmpty(song.fileName) ? "本地歌曲" : song.fileName);
        }
        return TextUtils.join(" · ", parts);
    }

    private ArrayList<LyricLine> readLyrics(Uri uri) {
        ArrayList<LyricLine> lines = new ArrayList<>();
        Pattern lrcPattern = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?\\](.*)");
        try (InputStream stream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int fallbackTime = 0;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = lrcPattern.matcher(line);
                if (matcher.matches()) {
                    int minutes = Integer.parseInt(matcher.group(1));
                    int seconds = Integer.parseInt(matcher.group(2));
                    String fraction = matcher.group(3);
                    int millis = fraction == null ? 0 : Integer.parseInt((fraction + "00").substring(0, 3));
                    lines.add(new LyricLine((minutes * 60 + seconds) * 1000 + millis, matcher.group(4).trim()));
                } else if (!line.trim().isEmpty()) {
                    lines.add(new LyricLine(fallbackTime, line.trim()));
                    fallbackTime += 5000;
                }
            }
        } catch (Exception e) {
            toast("歌词读取失败");
        }
        return lines;
    }

    private void showCreatePlaylistDialog() {
        showInputPanel("创建播放列表", "播放列表名称", "", "创建", name -> {
            String finalName = name;
            if (name.isEmpty()) {
                finalName = "播放列表 " + (playlists.size() + 1);
            }
            playlists.add(new Playlist(finalName));
            activePlaylistIndex = playlists.size() - 1;
            currentSongIndex = -1;
            saveState();
            refreshAll();
        });
    }

    private void deleteCurrentPlaylist() {
        if (playlists.size() <= 1) {
            toast("至少保留一个播放列表");
            return;
        }
        Playlist playlist = getActivePlaylist();
        showConfirmPanel("删除播放列表", "确定删除“" + playlist.name + "”吗？这不会删除资料库中的歌曲。", "删除", () -> {
            stopPlayback();
            playlists.remove(activePlaylistIndex);
            activePlaylistIndex = Math.max(0, activePlaylistIndex - 1);
            currentSongIndex = getActivePlaylist().songs.isEmpty() ? -1 : 0;
            saveState();
            refreshAll();
        });
    }

    private int themeIndex() {
        return Math.max(0, Math.min(themeIndexValue, 2));
    }

    private int themeBackground() {
        int[] colors = {Color.rgb(255, 253, 251), Color.rgb(255, 247, 244), Color.rgb(244, 250, 255)};
        return colors[themeIndex()];
    }

    private int themePanel() {
        int[] colors = {Color.WHITE, Color.rgb(255, 238, 232), Color.rgb(232, 244, 255)};
        return colors[themeIndex()];
    }

    private int themeText() {
        int[] colors = {Color.rgb(22, 19, 22), Color.rgb(64, 35, 36), Color.rgb(22, 43, 68)};
        return colors[themeIndex()];
    }

    private int themeSubtext() {
        int[] colors = {Color.rgb(129, 125, 131), Color.rgb(151, 92, 87), Color.rgb(88, 112, 140)};
        return colors[themeIndex()];
    }

    private int themeAccent() {
        int[] colors = {Color.rgb(240, 68, 89), Color.rgb(244, 112, 79), Color.rgb(78, 139, 211)};
        return colors[themeIndex()];
    }

    private int themePlayerTint() {
        int[] colors = {Color.rgb(58, 50, 56), Color.rgb(92, 46, 45), Color.rgb(34, 63, 91)};
        return colors[themeIndex()];
    }

    private void applyTheme() {
        View main = findViewById(R.id.main);
        if (main != null) {
            main.setBackgroundColor(themeBackground());
        }
        if (libraryScroll != null) {
            libraryScroll.setBackgroundColor(themeBackground());
        }
        View bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setBackgroundColor(themePanel());
        }
        View playerTint = findViewById(R.id.playerTint);
        if (playerTint != null) {
            playerTint.setBackgroundColor(Color.argb(120, Color.red(themePlayerTint()), Color.green(themePlayerTint()), Color.blue(themePlayerTint())));
        }
        tintTopButton(findViewById(R.id.createPlaylistButton), true);
        tintTopButton(findViewById(R.id.deletePlaylistButton), false);
        updateBottomTabs();
    }

    private void updateBottomTabs() {
        TextView[] icons = {tabLibraryIcon, tabSearchIcon, tabLikesIcon, tabMineIcon};
        TextView[] labels = {tabLibraryLabel, tabSearchLabel, tabLikesLabel, tabMineLabel};
        for (int i = 0; i < icons.length; i++) {
            int color = activeTab == i ? themeAccent() : themeSubtext();
            if (icons[i] != null) {
                icons[i].setTextColor(color);
            }
            if (labels[i] != null) {
                labels[i].setTextColor(color);
            }
        }
    }

    private void tintTopButton(View view, boolean accentText) {
        if (view instanceof MaterialButton) {
            MaterialButton button = (MaterialButton) view;
            button.setTextColor(accentText ? themeAccent() : themeSubtext());
            button.setBackgroundTintList(ColorStateList.valueOf(themePanel()));
        }
    }

    private void applyRoundBackground(View view, int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp((int) radiusDp));
        view.setBackground(drawable);
    }

    private void refreshAll() {
        ensurePlaylist();
        applyTheme();
        refreshBackground();
        refreshCurrentTab();
        refreshNowPlaying();
        refreshPlayIcons();
        refreshMode();
        refreshFavoriteIcon();
        updateBottomTabs();
    }

    private void refreshBackground() {
        if (backgroundImage == null) {
            return;
        }
        if (backgroundUri == null || backgroundUri.isEmpty()) {
            backgroundImage.setImageDrawable(null);
        } else {
            backgroundImage.setImageURI(Uri.parse(backgroundUri));
        }
    }

    private void refreshCurrentTab() {
        libraryMenu.removeAllViews();
        songList.removeAllViews();
        pageTitle.setTextColor(themeText());
        sectionTitle.setTextColor(themeText());
        miniTitle.setTextColor(themeText());
        miniSubtitle.setTextColor(themeSubtext());
        activeTab = Math.max(0, Math.min(activeTab, 3));

        if (activeTab == 0) {
            setTopActions(true, true);
            renderLibraryPage();
        } else if (activeTab == 1) {
            setTopActions(false, false);
            renderSearchPage();
        } else if (activeTab == 2) {
            setTopActions(false, false);
            renderFavoritesPage();
        } else {
            setTopActions(false, false);
            renderSettingsPage();
        }
    }

    private void setTopActions(boolean showAdd, boolean showMore) {
        View add = findViewById(R.id.createPlaylistButton);
        View more = findViewById(R.id.deletePlaylistButton);
        if (add != null) {
            add.setVisibility(showAdd ? View.VISIBLE : View.GONE);
        }
        if (more != null) {
            more.setVisibility(showMore ? View.VISIBLE : View.GONE);
        }
    }

    private void renderLibraryPage() {
        pageTitle.setText("资料库");
        libraryMenu.setVisibility(View.VISIBLE);
        songList.setVisibility(View.GONE);
        sectionTitle.setVisibility(View.GONE);

        Playlist playlist = getActivePlaylist();
        addGroupedMenuCard("我的音乐",
                new String[]{"♫", "◷", "▣"},
                new String[]{"全部歌曲", "最近添加", "歌曲文件"},
                new String[]{"共 " + librarySongs.size() + " 首歌曲", "按导入时间查看音乐", "查看已导入歌曲的文件名"},
                new View.OnClickListener[]{
                        v -> showSongPickerDialog("全部歌曲", new ArrayList<>(librarySongs)),
                        v -> showSongPickerDialog("最近添加", getRecentLibrarySongs()),
                        v -> showSongPickerDialog("歌曲文件", new ArrayList<>(librarySongs))
                });
        addGroupedMenuCard("播放列表",
                new String[]{"♬", "⊕"},
                new String[]{"当前播放列表", "新建播放列表"},
                new String[]{playlist.name + " · " + playlist.songs.size() + " 首", "创建一个新的歌单"},
                new View.OnClickListener[]{v -> choosePlaylist(), v -> showCreatePlaylistDialog()});
        addGroupedMenuCard("管理",
                new String[]{"⇩", "⚙"},
                new String[]{"导入音乐", "管理音乐资源"},
                new String[]{"从设备中选择歌曲", "当前歌曲的歌词、封面与背景"},
                new View.OnClickListener[]{v -> openAudioPicker(), v -> showEnhancedManageDialog()});
    }

    private void renderSearchPage() {
        pageTitle.setText("搜索");
        libraryMenu.setVisibility(View.VISIBLE);
        songList.setVisibility(View.VISIBLE);
        sectionTitle.setVisibility(View.GONE);

        addSearchBox();
        addRecentSearches();
        renderSearchResults();
    }

    private void renderFavoritesPage() {
        pageTitle.setText("喜欢");
        libraryMenu.setVisibility(View.VISIBLE);
        songList.setVisibility(View.VISIBLE);
        sectionTitle.setVisibility(View.GONE);

        ArrayList<Song> songs = getFavoriteSongs();
        addSectionTitle(libraryMenu, "收藏歌曲");
        TextView count = new TextView(this);
        count.setText("共 " + songs.size() + " 首");
        count.setTextColor(themeSubtext());
        count.setTextSize(18);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countParams.setMargins(0, dp(8), 0, dp(18));
        libraryMenu.addView(count, countParams);
        if (songs.isEmpty()) {
            addEmptyState(songList, "还没有喜欢的歌曲", "播放歌曲时点击爱心即可收藏", "去资料库选择歌曲", () -> switchTab(0));
        } else {
            renderFavoritesGrid(songList, songs);
        }
    }

    private void renderSettingsPage() {
        pageTitle.setText("设置");
        libraryMenu.setVisibility(View.VISIBLE);
        songList.setVisibility(View.GONE);
        sectionTitle.setVisibility(View.GONE);

        addGroupedMenuCard("播放",
                new String[]{"♫", "≡", "◷"},
                new String[]{"当前歌曲设置", "播放偏好", "最近播放"},
                new String[]{"歌词、封面、背景与移除", "播放模式、自动播放与音量", "查看播放记录"},
                new View.OnClickListener[]{v -> showEnhancedManageDialog(), v -> showPlaybackPreferencesDialog(), v -> showRecentPlayedDialog()});
        addGroupedMenuCard("管理",
                new String[]{"▣", "▤"},
                new String[]{"存储管理", "资源管理"},
                new String[]{"音乐文件与记录维护", "当前歌曲资源设置"},
                new View.OnClickListener[]{v -> showStorageManagementDialog(), v -> showEnhancedManageDialog()});
        addGroupedMenuCard("应用",
                new String[]{"◌", "i"},
                new String[]{"外观设置", "关于应用"},
                new String[]{"主题颜色与显示效果", "版本信息与说明"},
                new View.OnClickListener[]{v -> showAppearanceSettingsDialog(), v -> showAboutDialog()});
    }

    private void addSectionTitle(LinearLayout target, String title) {
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(themeText());
        titleView.setTextSize(22);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleView.setPadding(0, dp(4), 0, dp(10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(22), 0, 0);
        target.addView(titleView, params);
    }

    private void addGroupedMenuCard(String sectionTitleText, String[] icons, String[] titles, String[] subtitles, View.OnClickListener[] listeners) {
        addSectionTitle(libraryMenu, sectionTitleText);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_group_card);
        card.setElevation(dp(3));
        card.setPadding(dp(14), dp(8), dp(14), dp(8));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(8));
        libraryMenu.addView(card, cardParams);

        for (int i = 0; i < titles.length; i++) {
            addGroupedMenuRow(card, icons[i], titles[i], subtitles[i], listeners[i]);
            if (i < titles.length - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(Color.rgb(236, 232, 232));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(1)));
                dividerParams.setMargins(dp(84), 0, 0, 0);
                card.addView(divider, dividerParams);
            }
        }
    }

    private void addGroupedMenuRow(LinearLayout card, String icon, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        row.setClickable(true);
        row.setFocusable(true);

        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextColor(themeAccent());
        iconView.setGravity(Gravity.CENTER);
        iconView.setTextSize(25);
        iconView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        iconView.setBackgroundResource(R.drawable.bg_soft_icon_tile);
        row.addView(iconView, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(18), 0, dp(8), 0);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(themeText());
        titleView.setTextSize(19);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(themeSubtext());
        subtitleView.setTextSize(14);
        subtitleView.setSingleLine(true);
        subtitleView.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(titleView);
        texts.addView(subtitleView);
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(Color.rgb(166, 166, 174));
        arrow.setTextSize(34);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(32), dp(58)));
        card.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setAnimatedClickListener(row, listener);
    }

    private void addSearchBox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(14), dp(6), dp(10), dp(6));
        box.setBackgroundResource(R.drawable.bg_search_box);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        boxParams.setMargins(0, dp(18), 0, dp(28));
        libraryMenu.addView(box, boxParams);

        TextView searchIcon = new TextView(this);
        searchIcon.setText("⌕");
        searchIcon.setGravity(Gravity.CENTER);
        searchIcon.setTextColor(themeSubtext());
        searchIcon.setTextSize(25);
        box.addView(searchIcon, new LinearLayout.LayoutParams(dp(38), ViewGroup.LayoutParams.MATCH_PARENT));

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(searchQuery == null ? "" : searchQuery);
        input.setHint("搜索歌曲或文件名");
        input.setTextColor(themeText());
        input.setHintTextColor(themeSubtext());
        input.setTextSize(18);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSelection(input.getText().length());
        box.addView(input, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView clear = new TextView(this);
        clear.setText("×");
        clear.setGravity(Gravity.CENTER);
        clear.setTextColor(themeSubtext());
        clear.setTextSize(30);
        box.addView(clear, new LinearLayout.LayoutParams(dp(42), ViewGroup.LayoutParams.MATCH_PARENT));
        setAnimatedClickListener(clear, v -> {
            searchQuery = "";
            saveState();
            refreshAll();
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s == null ? "" : s.toString();
                saveState();
                renderSearchResults();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                addRecentSearchQuery(input.getText().toString().trim());
                saveState();
                refreshAll();
                return true;
            }
            return false;
        });
    }

    private void addRecentSearches() {
        addSectionTitle(libraryMenu, "最近搜索");
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams chipsParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipsParams.setMargins(0, 0, 0, dp(18));
        libraryMenu.addView(chips, chipsParams);

        ArrayList<String> labels = new ArrayList<>(recentSearchQueries);
        if (labels.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无最近搜索");
            empty.setTextColor(themeSubtext());
            empty.setTextSize(15);
            chips.addView(empty, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        for (String label : labels) {
            TextView chip = new TextView(this);
            chip.setText("◷  " + label);
            chip.setTextColor(themeText());
            chip.setTextSize(15);
            chip.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(16), dp(9), dp(16), dp(9));
            chip.setBackgroundResource(R.drawable.bg_search_pill);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, dp(10), 0);
            chips.addView(chip, params);
            setAnimatedClickListener(chip, v -> {
                searchQuery = label;
                addRecentSearchQuery(label);
                saveState();
                refreshAll();
            });
        }
    }

    private void renderSearchResults() {
        songList.removeAllViews();
        String query = searchQuery == null ? "" : searchQuery.trim();
        if (query.isEmpty()) {
            addEmptyState(songList, "输入关键词查找资料库歌曲", "搜索歌曲名、歌手或文件名后显示结果", "导入音乐", () -> openAudioPicker());
            return;
        }
        addSectionTitle(songList, "搜索结果");
        ArrayList<Song> songs = getSearchSongs();
        if (songs.isEmpty()) {
            addEmptyState(songList, "没有找到匹配歌曲", "可导入音乐或尝试其他关键词", "清除搜索", () -> {
                searchQuery = "";
                saveState();
                refreshAll();
            });
        } else {
            renderSongListCard(songList, songs, false);
        }
    }

    private void renderSongListCard(LinearLayout target, ArrayList<Song> songs, boolean showFavoriteHeart) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_group_card);
        card.setElevation(dp(3));
        card.setPadding(dp(14), dp(8), dp(14), dp(8));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        target.addView(card, cardParams);
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            card.addView(buildPreviewSongRow(song, showFavoriteHeart), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (i < songs.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(Color.rgb(236, 232, 232));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(1)));
                params.setMargins(dp(84), 0, 0, 0);
                card.addView(divider, params);
            }
        }
    }

    private void renderFavoritesGrid(LinearLayout target, ArrayList<Song> songs) {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        target.addView(grid, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < songs.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dp(18));
            grid.addView(row, rowParams);

            row.addView(buildFavoriteTile(songs.get(i)), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            if (i + 1 < songs.size()) {
                row.addView(buildFavoriteTile(songs.get(i + 1)), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            } else {
                View spacer = new View(this);
                row.addView(spacer, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            }
        }
    }

    private View buildFavoriteTile(Song song) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(6), 0, dp(6), 0);
        tile.setClickable(true);
        tile.setFocusable(true);

        FrameLayout coverFrame = new FrameLayout(this);
        ImageView cover = new ImageView(this);
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        applyCover(cover, song, getCoverResource(Math.max(0, librarySongs.indexOf(song))));
        coverFrame.addView(cover, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView more = new TextView(this);
        more.setText("…");
        more.setGravity(Gravity.CENTER);
        more.setTextColor(Color.WHITE);
        more.setTextSize(22);
        applyRoundBackground(more, Color.argb(95, 0, 0, 0), 18);
        FrameLayout.LayoutParams moreParams = new FrameLayout.LayoutParams(dp(36), dp(36), Gravity.TOP | Gravity.RIGHT);
        moreParams.setMargins(0, dp(8), dp(8), 0);
        coverFrame.addView(more, moreParams);

        LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(164));
        tile.addView(coverFrame, coverParams);

        TextView title = new TextView(this);
        title.setText(song.title);
        title.setTextColor(themeText());
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, dp(8), 0, 0);
        tile.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText(getFavoriteTileSubtitle(song));
        subtitle.setTextColor(isCurrentSong(song) && mediaPlayer != null && prepared && mediaPlayer.isPlaying() ? themeAccent() : themeSubtext());
        subtitle.setTextSize(14);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        tile.addView(subtitle);

        setAnimatedClickListener(tile, v -> playSongFromLibrary(song));
        setAnimatedClickListener(more, v -> showLibrarySongActionDialog(song));
        return tile;
    }

    private String getFavoriteTileSubtitle(Song song) {
        if (isCurrentSong(song) && mediaPlayer != null && prepared && mediaPlayer.isPlaying()) {
            return "正在播放";
        }
        return buildMetadataSubtitle(song);
    }

    private boolean isCurrentSong(Song song) {
        Song current = getCurrentSong();
        return current != null && song != null && current.uri.equals(song.uri);
    }

    private View buildPreviewSongRow(Song song, boolean showFavoriteHeart) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        row.setClickable(true);
        row.setFocusable(true);

        ImageView cover = new ImageView(this);
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        applyCover(cover, song, getCoverResource(Math.max(0, librarySongs.indexOf(song))));
        row.addView(cover, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(18), 0, dp(8), 0);
        TextView title = new TextView(this);
        title.setText(song.title);
        title.setTextColor(themeText());
        title.setTextSize(19);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        TextView subtitle = new TextView(this);
        subtitle.setText(buildSongSubtitle(song));
        subtitle.setTextColor(themeSubtext());
        subtitle.setTextSize(14);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(title);
        texts.addView(subtitle);
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        if (showFavoriteHeart) {
            TextView heart = new TextView(this);
            heart.setText("♥");
            heart.setGravity(Gravity.CENTER);
            heart.setTextColor(themeAccent());
            heart.setTextSize(28);
            row.addView(heart, new LinearLayout.LayoutParams(dp(46), dp(58)));
            setAnimatedClickListener(heart, v -> {
                favoriteUris.remove(song.uri);
                saveState();
                refreshAll();
            });
        }

        TextView more = new TextView(this);
        more.setText("…");
        more.setGravity(Gravity.CENTER);
        more.setTextColor(themeSubtext());
        more.setTextSize(26);
        row.addView(more, new LinearLayout.LayoutParams(dp(42), dp(58)));
        setAnimatedClickListener(row, v -> playSongFromLibrary(song));
        setAnimatedClickListener(more, v -> showLibrarySongActionDialog(song));
        return row;
    }

    private void addEmptyState(LinearLayout target, String title, String subtitle, String actionText, Runnable action) {
        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER_HORIZONTAL);
        empty.setPadding(dp(18), dp(28), dp(18), dp(28));
        empty.setBackgroundResource(R.drawable.bg_group_card);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(themeText());
        titleView.setTextSize(19);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(themeSubtext());
        subtitleView.setTextSize(14);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, dp(8), 0, dp(18));
        TextView actionView = new TextView(this);
        actionView.setText(actionText);
        actionView.setTextColor(Color.WHITE);
        actionView.setTextSize(15);
        actionView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        actionView.setGravity(Gravity.CENTER);
        applyRoundBackground(actionView, themeAccent(), 22);
        actionView.setPadding(dp(18), dp(10), dp(18), dp(10));
        empty.addView(titleView);
        empty.addView(subtitleView);
        empty.addView(actionView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        target.addView(empty, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setAnimatedClickListener(actionView, v -> action.run());
    }

    private void addRecentSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        String clean = query.trim();
        recentSearchQueries.remove(clean);
        recentSearchQueries.add(0, clean);
        while (recentSearchQueries.size() > 5) {
            recentSearchQueries.remove(recentSearchQueries.size() - 1);
        }
    }

    private void showPlaceholderDialog(String title, String message) {
        showMenuDialog(title, new String[]{"暂未开放"}, new String[]{message}, which -> { });
    }

    private void showPlaybackPreferencesDialog() {
        String currentMode = getPlayModeName();
        String volumeText = audioManager == null ? "跟随系统音量" : "当前音量 " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        String[] actions = {"顺序循环", "单曲循环", "随机播放", "音量调节", "停止播放"};
        String[] subtitles = {
                playMode == 0 ? "当前正在使用" : "播完后进入下一首",
                playMode == 1 ? "当前正在使用" : "重复播放当前歌曲",
                playMode == 2 ? "当前正在使用" : "随机选择下一首歌曲",
                volumeText,
                "停止当前播放并重置进度"
        };
        showMenuDialog("播放偏好 · " + currentMode, actions, subtitles, which -> {
            if (which >= 0 && which <= 2) {
                playMode = which;
                saveState();
                refreshMode();
                toast("已切换为" + getPlayModeName());
            } else if (which == 3) {
                showVolumePanel();
            } else if (which == 4) {
                stopPlayback();
            }
        });
    }

    private void showVolumePanel() {
        if (audioManager == null) {
            toast("无法读取系统音量");
            return;
        }
        Dialog dialog = new Dialog(this);
        LinearLayout panel = createDialogPanel("音量调节");
        TextView value = createDialogMessage("");
        SeekBar seek = new SeekBar(this);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seek.setMax(max);
        seek.setProgress(current);
        value.setText("当前音量：" + current + "/" + max);
        panel.addView(value);
        panel.addView(seek, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    value.setText("当前音量：" + progress + "/" + max);
                    if (volumeSeek != null) {
                        volumeSeek.setProgress(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        LinearLayout buttons = createDialogButtonRow();
        TextView done = createDialogButton("完成", true);
        buttons.addView(done, new LinearLayout.LayoutParams(0, dp(44), 1));
        panel.addView(buttons);
        setAnimatedClickListener(done, v -> dialog.dismiss());
        showCustomDialog(dialog, panel);
    }

    private void showStorageManagementDialog() {
        String overview = "资料库：" + librarySongs.size() + " 首\n"
                + "播放列表：" + playlists.size() + " 个\n"
                + "收藏歌曲：" + favoriteUris.size() + " 首\n"
                + "最近播放：" + recentPlayedUris.size() + " 条\n"
                + "最近搜索：" + recentSearchQueries.size() + " 条";
        String[] actions = {"查看存储概况", "整理播放列表引用", "清理最近播放记录", "清理最近搜索记录"};
        String[] subtitles = {overview, "移除播放列表中已经不在资料库的引用", "不会影响资料库歌曲", "不会影响搜索功能"};
        showMenuDialog("存储管理", actions, subtitles, which -> {
            if (which == 0) {
                showMessageDialog("存储概况", overview);
            } else if (which == 1) {
                tidyPlaylistReferences();
            } else if (which == 2) {
                recentPlayedUris.clear();
                saveState();
                refreshAll();
                toast("最近播放已清理");
            } else if (which == 3) {
                recentSearchQueries.clear();
                saveState();
                refreshAll();
                toast("最近搜索已清理");
            }
        });
    }

    private void showAppearanceSettingsDialog() {
        String[] actions = {"珊瑚红", "暖橙", "清蓝", "更换播放背景", "恢复默认背景"};
        String[] subtitles = {
                themeIndex() == 0 ? "当前正在使用" : "浅色卡片与红色强调",
                themeIndex() == 1 ? "当前正在使用" : "更柔和的暖色视觉",
                themeIndex() == 2 ? "当前正在使用" : "清爽蓝色强调",
                "选择一张图片作为播放详情页背景",
                "清除已设置的播放背景"
        };
        showMenuDialog("外观设置", actions, subtitles, which -> {
            if (which >= 0 && which <= 2) {
                themeIndexValue = which;
                saveState();
                refreshAll();
            } else if (which == 3) {
                openBackgroundPicker();
            } else if (which == 4) {
                backgroundUri = "";
                saveState();
                refreshAll();
                toast("已恢复默认背景");
            }
        });
    }

    private void showAboutDialog() {
        showMessageDialog("关于应用", "MusicPlayer3\n"
                + "一个面向本地音乐管理与播放的课程实践应用。\n\n"
                + "当前版本：1.0\n"
                + "适用场景：导入设备中的音频文件，建立自己的资料库、播放列表和喜欢歌曲集合。\n\n"
                + "隐私说明：应用不上传音乐文件或个人数据，播放记录、收藏、搜索历史和界面设置均保存在本机。\n\n"
                + "开发说明：本应用为移动软件设计课程大作业作品，主要用于展示 Android 本地媒体播放、数据持久化和界面交互设计。");
    }

    private void tidyPlaylistReferences() {
        Set<String> libraryUris = new HashSet<>();
        for (Song song : librarySongs) {
            libraryUris.add(song.uri);
        }
        int removed = 0;
        for (Playlist playlist : playlists) {
            for (int i = playlist.songs.size() - 1; i >= 0; i--) {
                if (!libraryUris.contains(playlist.songs.get(i).uri)) {
                    playlist.songs.remove(i);
                    removed++;
                }
            }
        }
        currentSongIndex = getActivePlaylist().songs.isEmpty() ? -1 : Math.min(currentSongIndex, getActivePlaylist().songs.size() - 1);
        saveState();
        refreshAll();
        toast(removed == 0 ? "播放列表引用正常" : "已整理 " + removed + " 条引用");
    }

    private String getPlayModeName() {
        String[] names = {"顺序循环", "单曲循环", "随机播放"};
        return names[Math.max(0, Math.min(playMode, names.length - 1))];
    }

    private ArrayList<Song> getSearchSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.getDefault());
        for (Song song : librarySongs) {
            String fileName = song.fileName == null ? "" : song.fileName;
            String artist = song.artist == null ? "" : song.artist;
            String album = song.album == null ? "" : song.album;
            if (query.isEmpty()
                    || song.title.toLowerCase(Locale.getDefault()).contains(query)
                    || fileName.toLowerCase(Locale.getDefault()).contains(query)
                    || artist.toLowerCase(Locale.getDefault()).contains(query)
                    || album.toLowerCase(Locale.getDefault()).contains(query)) {
                songs.add(song);
            }
        }
        Collections.sort(songs, (left, right) -> left.title.compareToIgnoreCase(right.title));
        return songs;
    }

    private ArrayList<Song> getFavoriteSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : librarySongs) {
            if (favoriteUris.contains(song.uri)) {
                songs.add(song);
            }
        }
        Collections.sort(songs, (left, right) -> left.title.compareToIgnoreCase(right.title));
        return songs;
    }

    private ArrayList<Song> getRecentPlayedSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        for (String uri : recentPlayedUris) {
            Song song = findLibrarySong(uri);
            if (song != null && !songs.contains(song)) {
                songs.add(song);
            }
        }
        return songs;
    }

    private ArrayList<Song> getRecentLibrarySongs() {
        ArrayList<Song> songs = new ArrayList<>(librarySongs);
        Collections.sort(songs, (left, right) -> Long.compare(right.addedAt, left.addedAt));
        return songs;
    }

    private String buildSongSubtitle(Song song) {
        ArrayList<String> parts = new ArrayList<>();
        String metadata = buildMetadataSubtitle(song);
        if (!TextUtils.isEmpty(metadata)) {
            parts.add(metadata);
        }
        if (favoriteUris.contains(song.uri)) {
            parts.add("我喜欢");
        }
        if (!song.lyricsUri.isEmpty()) {
            parts.add("已关联歌词");
        }
        return TextUtils.join(" · ", parts);
    }

    private void showSongActionDialog(int index) {
        Playlist playlist = getActivePlaylist();
        if (index < 0 || index >= playlist.songs.size()) {
            return;
        }
        currentSongIndex = index;
        Song song = playlist.songs.get(index);
        String favoriteAction = favoriteUris.contains(song.uri) ? "取消收藏" : "加入我喜欢";
        String[] actions = {favoriteAction, "查看歌曲详情", "关联歌词", "设置歌曲封面", "从当前列表移除"};
        String[] subtitles = {"同步到喜欢页", "歌词、封面与文件信息", "选择 txt 或 lrc 歌词文件", "为歌曲指定封面图片", "资料库歌曲不会被删除"};
        showMenuDialog(song.title, actions, subtitles, which -> {
            if (which == 0) toggleFavorite(song);
            if (which == 1) showSongDetails(song);
            if (which == 2) {
                currentSongIndex = index;
                openLyricsPicker();
            }
            if (which == 3) {
                currentSongIndex = index;
                openCoverPicker();
            }
            if (which == 4) confirmDeleteSong(index);
        });
    }

    private void showLibrarySongActionDialog(Song song) {
        Playlist playlist = getActivePlaylist();
        int index = playlist.songs.indexOf(song);
        if (index < 0) {
            showLibraryOnlySongActionDialog(song);
            return;
        }
        showSongActionDialog(index);
    }

    private void showLibraryOnlySongActionDialog(Song song) {
        String favoriteAction = favoriteUris.contains(song.uri) ? "取消收藏" : "加入我喜欢";
        String[] actions = {"播放这首歌", "添加到当前列表", favoriteAction, "查看歌曲详情"};
        String[] subtitles = {"播放时会加入当前列表", "只添加引用，不复制文件", "同步到喜欢页", "歌词、封面与文件信息"};
        showMenuDialog(song.title, actions, subtitles, which -> {
            if (which == 0) playSongFromLibrary(song);
            if (which == 1) {
                addSongToPlaylist(getActivePlaylist(), song);
                saveState();
                refreshAll();
                toast("已添加到当前列表");
            }
            if (which == 2) toggleFavorite(song);
            if (which == 3) showSongDetails(song);
        });
    }

    private void toggleFavorite(Song song) {
        if (song == null) {
            toast("请先选择一首歌曲");
            return;
        }
        if (favoriteUris.contains(song.uri)) {
            favoriteUris.remove(song.uri);
            toast("已取消收藏");
        } else {
            favoriteUris.add(song.uri);
            toast("已加入我喜欢");
        }
        saveState();
        refreshAll();
    }

    private void confirmDeleteSong(int index) {
        Playlist playlist = getActivePlaylist();
        if (index < 0 || index >= playlist.songs.size()) {
            return;
        }
        Song song = playlist.songs.get(index);
        showConfirmPanel("移除歌曲", "确定从当前播放列表移除“" + song.title + "”吗？资料库中的歌曲不会被删除。", "移除", () -> {
            if (currentSongIndex == index) {
                stopPlayback();
                currentSongIndex = -1;
            } else if (currentSongIndex > index) {
                currentSongIndex--;
            }
            playlist.songs.remove(index);
            saveState();
            refreshAll();
        });
    }

    private int findSongIndexInActivePlaylist(Song song) {
        Playlist playlist = getActivePlaylist();
        for (int i = 0; i < playlist.songs.size(); i++) {
            if (playlist.songs.get(i).uri.equals(song.uri)) {
                return i;
            }
        }
        return -1;
    }

    private void choosePlaylist() {
        ensurePlaylist();
        String[] names = new String[playlists.size()];
        String[] subtitles = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) {
            names[i] = (i == activePlaylistIndex ? "✓ " : "") + playlists.get(i).name;
            subtitles[i] = playlists.get(i).songs.size() + " 首歌曲";
        }
        showMenuDialog("切换播放列表", names, subtitles, which -> {
            activePlaylistIndex = which;
            currentSongIndex = getActivePlaylist().songs.isEmpty() ? -1 : Math.min(Math.max(0, currentSongIndex), getActivePlaylist().songs.size() - 1);
            saveState();
            refreshAll();
        });
    }

    private void deleteCurrentSong() {
        Song song = getCurrentSong();
        if (song == null) {
            toast("请先选择一首歌曲");
            return;
        }
        confirmDeleteSong(currentSongIndex);
    }

    private void playPrevious() {
        Playlist playlist = getActivePlaylist();
        if (playlist.songs.isEmpty()) {
            toast("播放列表暂无歌曲");
            return;
        }
        int nextIndex = currentSongIndex <= 0 ? playlist.songs.size() - 1 : currentSongIndex - 1;
        prepareAndMaybePlay(nextIndex, true);
    }

    private void playNext(boolean fromCompletion) {
        Playlist playlist = getActivePlaylist();
        if (playlist.songs.isEmpty()) {
            if (!fromCompletion) {
                toast("播放列表暂无歌曲");
            }
            return;
        }
        int nextIndex;
        if (playMode == 1 && fromCompletion && currentSongIndex >= 0) {
            nextIndex = currentSongIndex;
        } else if (playMode == 2 && playlist.songs.size() > 1) {
            do {
                nextIndex = random.nextInt(playlist.songs.size());
            } while (nextIndex == currentSongIndex);
        } else {
            nextIndex = currentSongIndex + 1;
            if (nextIndex >= playlist.songs.size()) {
                nextIndex = 0;
            }
        }
        prepareAndMaybePlay(nextIndex, true);
    }

    private void prepareAndMaybePlay(int index, boolean autoPlay) {
        Playlist playlist = getActivePlaylist();
        if (index < 0 || index >= playlist.songs.size()) {
            return;
        }
        releasePlayer();
        currentSongIndex = index;
        activeLyricLineIndex = -1;
        Song song = playlist.songs.get(index);
        mediaPlayer = MediaPlayer.create(this, Uri.parse(song.uri));
        if (mediaPlayer == null) {
            toast("歌曲无法播放");
            currentSongIndex = -1;
            refreshAll();
            return;
        }
        mediaPlayer.setOnPreparedListener(mp -> {
            prepared = true;
            progressSeek.setMax(mp.getDuration());
            totalTime.setText("-" + formatTime(mp.getDuration()));
            if (autoPlay) {
                mp.start();
                markSongPlayed(song);
            }
            saveState();
            refreshAll();
        });
        mediaPlayer.setOnCompletionListener(mp -> playNext(true));
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            toast("播放失败");
            stopPlayback();
            return true;
        });
        prepared = true;
        progressSeek.setMax(mediaPlayer.getDuration());
        totalTime.setText("-" + formatTime(mediaPlayer.getDuration()));
        if (autoPlay) {
            mediaPlayer.start();
            markSongPlayed(song);
        }
        saveState();
        refreshAll();
    }

    private void markSongPlayed(Song song) {
        song.playCount++;
        song.lastPlayedAt = System.currentTimeMillis();
        recentPlayedUris.remove(song.uri);
        recentPlayedUris.add(0, song.uri);
        while (recentPlayedUris.size() > 30) {
            recentPlayedUris.remove(recentPlayedUris.size() - 1);
        }
    }

    private void togglePlayback() {
        if (mediaPlayer == null || !prepared) {
            Playlist playlist = getActivePlaylist();
            if (playlist.songs.isEmpty()) {
                toast("请先导入音乐");
                return;
            }
            int index = currentSongIndex >= 0 ? currentSongIndex : 0;
            prepareAndMaybePlay(index, true);
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
            Song song = getCurrentSong();
            if (song != null) {
                markSongPlayed(song);
            }
        }
        saveState();
        refreshNowPlaying();
        refreshPlayIcons();
    }

    private void stopPlayback() {
        if (mediaPlayer != null && prepared) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
        progressSeek.setProgress(0);
        currentTime.setText("00:00");
        updateRemainingTime(0);
        refreshNowPlaying();
        refreshPlayIcons();
    }

    private void cyclePlayMode() {
        playMode = (playMode + 1) % 3;
        saveState();
        refreshMode();
        String[] names = {"列表循环", "单曲循环", "随机播放"};
        toast(names[playMode]);
    }

    private int getCoverResource(int index) {
        int[] covers = {R.drawable.bg_album_pink, R.drawable.bg_album_blue, R.drawable.bg_album_green, R.drawable.bg_album_red};
        return covers[Math.abs(index) % covers.length];
    }

    private void applyCover(ImageView imageView, Song song, int fallback) {
        applyRoundedClip(imageView, 12);
        imageView.setBackgroundResource(fallback);
        if (song != null && song.coverUri != null && !song.coverUri.isEmpty()) {
            imageView.setPadding(0, 0, 0, 0);
            imageView.setImageURI(Uri.parse(song.coverUri));
        } else if (song != null && song.embeddedCoverUri != null && !song.embeddedCoverUri.isEmpty()) {
            imageView.setPadding(0, 0, 0, 0);
            imageView.setImageURI(Uri.parse(song.embeddedCoverUri));
        } else {
            imageView.setPadding(dp(10), dp(10), dp(10), dp(10));
            imageView.setImageResource(R.drawable.ic_music_note);
        }
    }

    private void applyRoundedClip(ImageView imageView, int radiusDp) {
        final float radius = dp(radiusDp);
        imageView.setClipToOutline(true);
        imageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
    }

    private void refreshNowPlaying() {
        Song song = getCurrentSong();
        if (song == null) {
            activeLyricLineIndex = -1;
            songTitle.setText("未选择歌曲");
            songMeta.setText("通过菜单导入音乐");
            miniTitle.setText("选择一首歌曲");
            miniSubtitle.setText("导入后即可播放");
            lyricsView.setText("暂无歌词\n\n可在菜单中关联歌词");
            progressSeek.setMax(0);
            progressSeek.setProgress(0);
            currentTime.setText("00:00");
            totalTime.setText("-00:00");
            statusText.setText("已暂停");
            miniCover.setBackgroundResource(R.drawable.bg_album_pink);
            miniCover.setImageResource(R.drawable.ic_music_note);
            playerCover.setBackgroundResource(R.drawable.bg_album_pink);
            playerCover.setImageResource(R.drawable.ic_music_note);
            return;
        }
        songTitle.setText(song.title);
        String listLabel = buildMetadataSubtitle(song);
        if (favoriteUris.contains(song.uri)) {
            listLabel = listLabel + " · 我喜欢";
        }
        songMeta.setText(listLabel);
        miniTitle.setText(song.title);
        miniSubtitle.setText(listLabel);
        statusText.setText(mediaPlayer != null && prepared && mediaPlayer.isPlaying() ? "播放中" : "已暂停");
        updateLyrics(mediaPlayer != null && prepared ? mediaPlayer.getCurrentPosition() : 0);
        int cover = getCoverResource(currentSongIndex);
        applyCover(miniCover, song, cover);
        applyCover(playerCover, song, cover);
    }
    private void refreshMode() {
        int[] icons = {R.drawable.ic_mode_repeat, R.drawable.ic_mode_repeat_one, R.drawable.ic_mode_shuffle};
        String[] modeNames = {"顺序循环", "单曲循环", "随机播放"};
        modeButton.setImageResource(icons[playMode]);
        modeButton.setContentDescription(modeNames[playMode]);
    }

    private void refreshFavoriteIcon() {
        if (favoriteButton == null) {
            return;
        }
        Song song = getCurrentSong();
        boolean liked = song != null && favoriteUris.contains(song.uri);
        favoriteButton.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        favoriteButton.setContentDescription(liked ? "取消喜欢" : "加入喜欢");
    }

    private void updateProgress() {
        if (mediaPlayer == null || !prepared) {
            return;
        }
        int position = mediaPlayer.getCurrentPosition();
        if (!userSeeking) {
            progressSeek.setProgress(position);
            currentTime.setText(formatTime(position));
            updateRemainingTime(position);
        }
        updateLyrics(position);
        refreshPlayIcons();
    }

    private void updateRemainingTime(int position) {
        int duration = mediaPlayer != null && prepared ? mediaPlayer.getDuration() : progressSeek.getMax();
        totalTime.setText("-" + formatTime(Math.max(0, duration - position)));
    }

    private void updateLyrics(int position) {
        Song song = getCurrentSong();
        if (song == null) {
            activeLyricLineIndex = -1;
            return;
        }
        if (song.lyricLines.isEmpty() && !song.lyricsUri.isEmpty()) {
            song.lyricLines = readLyrics(Uri.parse(song.lyricsUri));
        }
        if (song.lyricLines.isEmpty()) {
            activeLyricLineIndex = -1;
            lyricsView.setText("暂无歌词\n\n可在菜单中关联歌词");
            return;
        }
        LyricLine current = song.lyricLines.get(0);
        LyricLine next = null;
        int currentIndex = 0;
        for (int i = 0; i < song.lyricLines.size(); i++) {
            LyricLine candidate = song.lyricLines.get(i);
            if (candidate.timeMs <= position) {
                current = candidate;
                currentIndex = i;
                next = i + 1 < song.lyricLines.size() ? song.lyricLines.get(i + 1) : null;
            } else {
                break;
            }
        }
        if (currentIndex == activeLyricLineIndex) {
            return;
        }
        activeLyricLineIndex = currentIndex;
        lyricsView.setText(buildLyricSpan(current.text, next == null ? "" : next.text));
        lyricsView.setAlpha(0.35f);
        lyricsView.setTranslationY(dp(18));
        lyricsView.animate().alpha(1f).translationY(0).setDuration(260).start();
    }

    private SpannableStringBuilder buildLyricSpan(String current, String next) {
        SpannableStringBuilder builder = new SpannableStringBuilder(current == null ? "" : current);
        if (builder.length() > 0) {
            builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.player_text)), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(1.08f), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (next != null && !next.isEmpty()) {
            int start = builder.length();
            builder.append("\n\n").append(next);
            builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.player_subtext)), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new RelativeSizeSpan(0.82f), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    private void refreshPlayIcons() {
        boolean playing = mediaPlayer != null && prepared && mediaPlayer.isPlaying();
        playPauseButton.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
        miniPlayButton.setImageResource(playing ? R.drawable.ic_pause_dark : R.drawable.ic_play_dark);
    }

    private Playlist getActivePlaylist() {
        ensurePlaylist();
        activePlaylistIndex = Math.min(Math.max(0, activePlaylistIndex), playlists.size() - 1);
        return playlists.get(activePlaylistIndex);
    }

    private Song getCurrentSong() {
        Playlist playlist = getActivePlaylist();
        if (currentSongIndex < 0 || currentSongIndex >= playlist.songs.size()) {
            return null;
        }
        return playlist.songs.get(currentSongIndex);
    }

    private void ensurePlaylist() {
        if (playlists.isEmpty()) {
            playlists.add(new Playlist("我的喜欢"));
        }
    }

    private void releasePlayer() {
        prepared = false;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void loadState() {
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String json = preferences.getString(KEY_STATE, "");
        if (json == null || json.isEmpty()) {
            ensurePlaylist();
            return;
        }
        try {
            JSONObject state = new JSONObject(json);
            activePlaylistIndex = state.optInt("activePlaylistIndex", 0);
            currentSongIndex = state.optInt("currentSongIndex", -1);
            playMode = state.optInt("playMode", 0);
            themeIndexValue = Math.max(0, Math.min(state.optInt("themeIndexValue", 0), 2));
            activeTab = Math.max(0, Math.min(state.optInt("activeTab", 0), 3));
            searchQuery = state.optString("searchQuery", "");
            backgroundUri = state.optString("backgroundUri", "");

            JSONArray libraryArray = state.optJSONArray("librarySongs");
            if (libraryArray != null) {
                for (int i = 0; i < libraryArray.length(); i++) {
                    JSONObject songJson = libraryArray.optJSONObject(i);
                    if (songJson != null) {
                        Song song = Song.fromJson(songJson);
                        normalizeStoredMetadata(song);
                        if (findInList(librarySongs, song.uri) == null) {
                            librarySongs.add(song);
                        }
                    }
                }
            }

            JSONArray listArray = state.optJSONArray("playlists");
            if (listArray != null) {
                for (int i = 0; i < listArray.length(); i++) {
                    playlists.add(Playlist.fromJson(listArray.getJSONObject(i), librarySongs));
                }
            }

            JSONArray favoriteArray = state.optJSONArray("favoriteUris");
            if (favoriteArray != null) {
                for (int i = 0; i < favoriteArray.length(); i++) {
                    favoriteUris.add(favoriteArray.optString(i, ""));
                }
            }
            JSONArray recentArray = state.optJSONArray("recentPlayedUris");
            if (recentArray != null) {
                for (int i = 0; i < recentArray.length(); i++) {
                    String uri = recentArray.optString(i, "");
                    if (!uri.isEmpty()) {
                        recentPlayedUris.add(uri);
                    }
                }
            }
            JSONArray searchArray = state.optJSONArray("recentSearchQueries");
            if (searchArray != null) {
                for (int i = 0; i < searchArray.length(); i++) {
                    String query = searchArray.optString(i, "");
                    if (!query.isEmpty() && !recentSearchQueries.contains(query)) {
                        recentSearchQueries.add(query);
                    }
                }
            }
        } catch (JSONException ignored) {
            playlists.clear();
            librarySongs.clear();
            favoriteUris.clear();
            recentPlayedUris.clear();
            recentSearchQueries.clear();
            activeTab = 0;
        }
        ensurePlaylist();
    }

    private void normalizeStoredMetadata(Song song) {
        if (song == null || TextUtils.isEmpty(song.fileName)) {
            return;
        }
        ParsedSongName parsedName = parseSongName(song.fileName);
        if (looksLikeCombinedArtistTitle(song.title)) {
            song.title = parsedName.title;
        }
        if (TextUtils.isEmpty(song.artist)) {
            song.artist = parsedName.artist;
        }
    }

    private void saveState() {
        try {
            JSONObject state = new JSONObject();
            state.put("activePlaylistIndex", activePlaylistIndex);
            state.put("currentSongIndex", currentSongIndex);
            state.put("playMode", playMode);
            state.put("themeIndexValue", themeIndex());
            state.put("activeTab", Math.max(0, Math.min(activeTab, 3)));
            state.put("searchQuery", searchQuery == null ? "" : searchQuery);
            state.put("backgroundUri", backgroundUri == null ? "" : backgroundUri);
            JSONArray libraryArray = new JSONArray();
            for (Song song : librarySongs) {
                libraryArray.put(song.toJson());
            }
            state.put("librarySongs", libraryArray);
            JSONArray listArray = new JSONArray();
            for (Playlist playlist : playlists) {
                listArray.put(playlist.toJson());
            }
            state.put("playlists", listArray);
            JSONArray favoriteArray = new JSONArray();
            for (String uri : favoriteUris) {
                favoriteArray.put(uri);
            }
            state.put("favoriteUris", favoriteArray);
            JSONArray recentArray = new JSONArray();
            for (String uri : recentPlayedUris) {
                recentArray.put(uri);
            }
            state.put("recentPlayedUris", recentArray);
            JSONArray searchArray = new JSONArray();
            for (String query : recentSearchQueries) {
                searchArray.put(query);
            }
            state.put("recentSearchQueries", searchArray);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_STATE, state.toString()).apply();
        } catch (JSONException ignored) {
        }
    }

    private static Song findInList(ArrayList<Song> songs, String uri) {
        for (Song song : songs) {
            if (song.uri.equals(uri)) {
                return song;
            }
        }
        return null;
    }

    private String formatTime(int millis) {
        int totalSeconds = Math.max(0, millis / 1000);
        return String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private String formatDateTime(long millis) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new java.util.Date(millis));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(progressTicker);
        releasePlayer();
        saveState();
        super.onDestroy();
    }

    private static class Playlist {
        String name;
        final ArrayList<Song> songs = new ArrayList<>();

        Playlist(String name) {
            this.name = name;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("name", name);
            JSONArray uriArray = new JSONArray();
            for (Song song : songs) {
                uriArray.put(song.uri);
            }
            json.put("songUris", uriArray);
            return json;
        }

        static Playlist fromJson(JSONObject json, ArrayList<Song> librarySongs) {
            Playlist playlist = new Playlist(json.optString("name", "我的喜欢"));
            JSONArray uriArray = json.optJSONArray("songUris");
            if (uriArray != null) {
                for (int i = 0; i < uriArray.length(); i++) {
                    Song song = findInList(librarySongs, uriArray.optString(i, ""));
                    if (song != null) {
                        playlist.songs.add(song);
                    }
                }
                return playlist;
            }

            JSONArray oldSongArray = json.optJSONArray("songs");
            if (oldSongArray != null) {
                for (int i = 0; i < oldSongArray.length(); i++) {
                    JSONObject songJson = oldSongArray.optJSONObject(i);
                    if (songJson != null) {
                        Song song = Song.fromJson(songJson);
                        Song existing = findInList(librarySongs, song.uri);
                        if (existing == null) {
                            librarySongs.add(song);
                            existing = song;
                        }
                        playlist.songs.add(existing);
                    }
                }
            }
            return playlist;
        }
    }

    private static class Song {
        String title;
        final String uri;
        String fileName = "";
        String artist = "";
        String album = "";
        String lyricsUri = "";
        String coverUri = "";
        String embeddedCoverUri = "";
        int playCount;
        long addedAt;
        long lastPlayedAt;
        ArrayList<LyricLine> lyricLines = new ArrayList<>();

        Song(String title, String uri) {
            this.title = title;
            this.uri = uri;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("title", title);
            json.put("uri", uri);
            json.put("fileName", fileName);
            json.put("artist", artist);
            json.put("album", album);
            json.put("lyricsUri", lyricsUri);
            json.put("coverUri", coverUri);
            json.put("embeddedCoverUri", embeddedCoverUri);
            json.put("playCount", playCount);
            json.put("addedAt", addedAt);
            json.put("lastPlayedAt", lastPlayedAt);
            return json;
        }

        static Song fromJson(JSONObject json) {
            Song song = new Song(json.optString("title", "未命名歌曲"), json.optString("uri", ""));
            song.fileName = json.optString("fileName", "");
            song.artist = json.optString("artist", "");
            song.album = json.optString("album", "");
            song.lyricsUri = json.optString("lyricsUri", "");
            song.coverUri = json.optString("coverUri", "");
            song.embeddedCoverUri = json.optString("embeddedCoverUri", "");
            song.playCount = json.optInt("playCount", 0);
            song.addedAt = json.optLong("addedAt", 0L);
            song.lastPlayedAt = json.optLong("lastPlayedAt", 0L);
            if (song.fileName.isEmpty()) {
                song.fileName = song.title;
            }
            if (song.addedAt <= 0) {
                song.addedAt = song.lastPlayedAt > 0 ? song.lastPlayedAt : System.currentTimeMillis();
            }
            return song;
        }
    }

    private static class AudioMetadata {
        String title = "";
        String artist = "";
        String album = "";
        String embeddedCoverUri = "";
    }

    private static class ParsedSongName {
        final String title;
        final String artist;

        ParsedSongName(String title, String artist) {
            this.title = title;
            this.artist = artist;
        }
    }

    private static class LyricLine {
        final int timeMs;
        final String text;

        LyricLine(int timeMs, String text) {
            this.timeMs = timeMs;
            this.text = text;
        }
    }
}


