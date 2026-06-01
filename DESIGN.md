# MusicPlayer3 Design Specification

## Product

MusicPlayer3 is a local Android music player for importing, organizing, searching, and playing songs stored on the device. The app should feel like a modern personal music library: warm, clear, lightweight, and polished enough for a course project demo.

## Design Goals

- Build a mobile-first Android interface for a local music player.
- Keep the experience simple: import music, browse playlists, search songs, favorite songs, and open the full player.
- Use a fresh light library page and a darker immersive playback page.
- Make all controls large enough for touch, with rounded shapes and soft visual feedback.
- Preserve a clean student-project style: practical, readable, and not overloaded.

## Existing Screens

The current app contains these main surfaces:

- Library screen: song library and playlist management.
- Search screen: search songs by name and show recent searches.
- Favorites screen: liked songs and recent playback entry.
- Settings/About screen: app information and management actions.
- Mini player: persistent bottom playback bar above navigation.
- Full player overlay: immersive playback screen with cover art, lyrics, progress, volume, favorite, import/more actions, play mode, stop, and return-to-library controls.
- Dialog panels: rounded modal sheets for playlist management, music management, song details, search, and confirmations.

Reference screenshots available in the project:

- `current_musicplayer3.png`
- `phase2_launch.png`
- `more_dialog_musicplayer3.png`
- `create_dialog_musicplayer3.png`
- `create_dialog_musicplayer3_2.png`

## Visual Direction

Use a warm light background for the library experience, with dark near-black text and vivid accent colors. The playback experience should contrast with a dark atmospheric gradient and translucent controls.

The app should avoid heavy decoration. Prioritize spacious layout, rounded controls, soft cards, readable type, and clear hierarchy.

## Color Tokens

Core colors from the current Android resources:

- Library background: `#FFFDFB`
- Primary text: `#161316`
- Secondary text: `#817D83`
- White: `#FFFFFF`
- Fresh red accent: `#F04459`
- Fresh blue accent: `#7E9DDE`
- Fresh green accent: `#51D88A`
- Fresh yellow accent: `#FCEB68`
- Player text: `#FFFFFF`
- Player secondary text: `#CFC5CB`
- Player lyric muted text: `#8B8087`
- Player gradient: `#403B3B` to `#584C51` to `#7D5367`
- App dark gradient: `#321C24` to `#151719` to `#1A2730`

## Typography

- Use a clean Android sans-serif style.
- Large page titles: 36-40sp, bold.
- Section titles: 24-29sp, bold.
- Song titles: 16-18sp, bold.
- Metadata and helper text: 13-15sp, regular or medium.
- Bottom navigation labels: 11-12sp, bold.
- Full player lyric text: about 26-28sp, bold, with generous line spacing.

## Layout

Target a standard Android phone portrait layout.

Library layout:

- Root is full screen with warm off-white background.
- Content padding: about 22dp on left and right.
- Top bar contains a large title on the left and two circular icon buttons on the right.
- Playlist/menu area appears under the title with about 34dp vertical gap.
- Section title and song list follow below.
- Content must leave room for mini player and bottom navigation.

Mini player:

- Fixed near the bottom, above tab navigation.
- Height about 76dp.
- Horizontal pill with 38dp radius.
- Contains album tile, song title, subtitle, play button, and next button.
- Background is translucent warm white with a subtle white stroke.

Bottom navigation:

- Fixed at bottom, white background.
- Four equal tabs: Library, Search, Favorites, Settings.
- Each tab has a simple icon and label.
- Active state should use the fresh red accent.

Full player overlay:

- Full-screen dark gradient.
- Optional blurred or dim background image at low opacity.
- Top center collapse handle.
- Header row with cover, title, artist/album metadata, favorite button, and more/import button.
- Lyrics area takes most of the vertical space.
- Status, progress bar, current time and remaining time.
- Large playback controls centered: previous, play/pause, next.
- Volume control row.
- Secondary action row: play mode, stop, return to list.

Dialogs:

- Centered rounded panel.
- Width should be screen width minus about 44dp.
- Background uses soft panel colors matching the current theme.
- Dim background at about 34%.
- Title 22sp bold.
- Rows have title, subtitle, and optional arrow.
- Use short 150ms fade/scale animation.

## Components

### Song Row

- Rounded 8dp card row.
- Left album/color tile with music note icon.
- Main area: song title, artist or file info, playlist metadata.
- Right side can include more/favorite indicators.
- Selected/current song may use a stronger translucent accent.

### Album Tile

- Square rounded cover tile.
- When no album art is available, use one of the vivid gradient/color backgrounds:
  - pink/red
  - blue
  - green
  - yellow
- Center a simple music-note icon.

### Icon Buttons

- Circular or pill-shaped.
- Light mode: soft neutral fill, accent icon.
- Player mode: translucent white fill, white icon.
- Minimum touch size around 48-62dp.

### Search

- Rounded search field with placeholder text.
- Recent searches can be shown as small rounded pills or list rows.
- Empty state should explain that imported songs will appear here.

## Interaction Requirements

- Import audio files from local storage.
- Support selecting multiple audio files.
- Add imported songs to the library and current playlist.
- Create, switch, and delete playlists.
- Search songs by title.
- Mark or unmark a song as favorite.
- Show recently played songs.
- Open the full player from the mini player.
- Collapse the full player back to the library.
- Link lyrics text or LRC files.
- Set custom cover art for a song.
- Set a custom playback background image.
- Support play, pause, previous, next, stop, volume, progress seeking, repeat, repeat-one, and shuffle.

## Content Labels

Use clear Simplified Chinese labels:

- App title: `MusicPlayer3`
- Library tab: `资料库`
- Search tab: `搜索`
- Favorites tab: `喜欢`
- Settings tab: `设置`
- Recent section: `最近添加`
- Mini player empty title: `选择一首歌曲`
- Mini player empty subtitle: `导入后即可播放`
- Full player empty title: `未选择歌曲`
- Full player empty subtitle: `通过菜单导入音乐`
- Empty lyric text: `暂无歌词\n\n可在菜单中关联歌词`
- Management dialogs:
  - `播放列表管理`
  - `管理音乐`
  - `当前歌曲设置`
  - `搜索工具`
  - `歌曲详情`

## Preferred Redesign Direction

If improving the design, keep the app recognizable but make it more polished:

- Make the home/library screen feel like a premium local music app.
- Keep the warm light background but add clearer song cards and playlist chips.
- Make the mini player more prominent and easier to tap.
- Make the full player more immersive with a stronger cover-art area and clearer controls.
- Keep red as the main accent, but use it sparingly for active state and primary actions.
- Avoid clutter: no dense toolbars, no unnecessary marketing copy, no complex decorative illustrations.

## Technical Context

The current implementation is native Android Java with XML layouts and drawable XML assets.

Important source files:

- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/drawable/*.xml`
- `app/src/main/java/com/example/musicplayer3/MainActivity.java`

The generated design should be compatible with an Android music app and should be easy to translate back into XML or native Android views.

