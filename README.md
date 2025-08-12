## Its a Music Player Application
A custom-built Android music player with splash screen, song list adapter, bound + foreground service for playback, and full notification controls.
### Features
1. Splash Screen – Quick app startup with branding.

2. Song List Adapter – Displays songs from local resources or storage in a RecyclerView.

3. Bound + Foreground Service –

    Handles music playback using MediaPlayer.

    Stays alive in the background with ongoing notification.

    Allows activities to bind for real-time playback state updates.

4. Notification Controls –

    Play, pause, next, and previous buttons in notification.

    Works even when the app is in background or closed.

5. MediaSessionCompat Integration – Ensures system-wide audio focus and media button handling.

6. Binder Communication – Activity and service share playback status & control functions.

7. Supports Playback Speed Control – Change speed dynamically on Android M+.