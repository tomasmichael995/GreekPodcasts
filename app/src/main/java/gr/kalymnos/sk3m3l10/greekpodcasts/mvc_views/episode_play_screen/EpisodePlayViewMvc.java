package gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.episode_play_screen;

import android.graphics.Bitmap;
import android.support.v7.widget.Toolbar;
import android.widget.SeekBar;

import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.ViewMvc;

public interface EpisodePlayViewMvc extends ViewMvc {

    int getDownloadStartedMessage();

    int getDownloadCompletedMessage();

    interface OnActionButtonsClickListener {
        void onStarClick();

        void onDownloadClick();

        void onInfoClick();
    }

    interface OnTransportControlsClickListener {
        void onPlayButtonClick();

        void onPauseButtonClick();

        void onSkipToNextButtonClick();

        void onSkipToPreviousButtonClick();
    }

    interface OnPodcasterClickListener {
        void onPodcasterClick();
    }

    void bindPoster(String url);

    void drawStarButton();

    void unDrawStarButton();

    void drawDownloadButton();

    void unDrawDownloadButton();

    void bindPoster(Bitmap bitmap);

    void bindPodcaster(String name);

    void bindEpisodeTitle(String title);

    void bindPlaybackPosition(String position);

    void bindPlaybackDuration(String duration);

    void displayPlayButton(boolean display);

    void enableSeekBar(boolean enable);

    int getSeekBarProgress();

    void resetSeekBarProgress();

    void bindSeekBarMax(int max);

    void disableTransportControls(boolean disable);

    void bindSeekBarProgress(int progress);

    void setSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener);

    Toolbar getToolbar();

    void setOnActionButtonsClickListener(OnActionButtonsClickListener listener);

    void setOnTransportControlsClickListener(OnTransportControlsClickListener listener);

    void setOnPodcasterClickListener(OnPodcasterClickListener listener);

    int getInfoContainerId();
}
