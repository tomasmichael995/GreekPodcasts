package gr.kalymnos.sk3m3l10.greekpodcasts.mvc_controllers.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.all_episodes.AllEpisodesViewMvc;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.all_episodes.AllEpisodesViewMvcImpl;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.all_episodes.EpisodesAdapter;
import gr.kalymnos.sk3m3l10.greekpodcasts.playback_service.PlaybackService;

public class AllEpisodesFragment extends Fragment implements AllEpisodesViewMvc.OnEpisodeClickListener,
        AllEpisodesViewMvc.OnPopUpMenuClickListener {

    private static final String TAG = AllEpisodesFragment.class.getSimpleName();

    public interface AllEpisodesFragmentCommunicator {
        void onEpisodeClicked(int position);

        void onEpisodePopUpMenuClicked(int position);
    }

    private AllEpisodesFragmentCommunicator mCallback;

    private AllEpisodesViewMvc viewMvc;

    private List<MediaBrowserCompat.MediaItem> cachedMediaItems;

    private MediaBrowserCompat mediaBrowser;
    private ConnectionCallback connectionCallback;
    private MediaControllerCompat.Callback mediaControllerCallback;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewMvc = new AllEpisodesViewMvcImpl(inflater, container);
        viewMvc.setOnEpisodeClickListener(this);
        viewMvc.setOnPopUpMenuClickListener(this);
        //  Display loading bar until the fragment get connected to PlaybackService and fetched mediaItems
        viewMvc.displayLoadingIndicator(true);
        return viewMvc.getRootView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (AllEpisodesFragmentCommunicator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChosenEpisodeChangedListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mediaBrowser = new MediaBrowserCompat(getContext(),
                new ComponentName(getActivity().getApplicationContext(), PlaybackService.class),
                connectionCallback = new ConnectionCallback(),
                getArguments());
    }

    @Override
    public void onStart() {
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(mediaControllerCallback);
        }
        this.mediaBrowser.disconnect();
    }

    @Override
    public void onEpisodeClick(int position) {
        if (cachedMediaItems != null && cachedMediaItems.size() > 0) {
            String mediaId = cachedMediaItems.get(position).getMediaId();
            MediaControllerCompat.getMediaController(getActivity()).getTransportControls().prepareFromMediaId(mediaId, null);
            mCallback.onEpisodeClicked(position);
        }
    }

    @Override
    public void onPopUpMenuClick(int position) {
        Toast.makeText(getContext(), "Menu Clicked", Toast.LENGTH_SHORT).show();
    }

    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        @Override
        public void onConnected() {
            try {
                MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                //  Create controller
                MediaControllerCompat mediaController = new MediaControllerCompat(getContext(), token);
                //  Save controller
                MediaControllerCompat.setMediaController(getActivity(), mediaController);

                //  Subscribe to get the children of the PlaybackService
                //  "It is ok to subscribe while not connected but the results will not be returned
                //  until the connection completes"
                mediaBrowser.subscribe(mediaBrowser.getRoot(), new MediaBrowserCompat.SubscriptionCallback() {

                    //  Important:  In order to receive the callbacks the Service must call notifyChildrenchanged()

                    @Override
                    public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                        viewMvc.displayLoadingIndicator(false);
                        viewMvc.bindEpisodes(children);
                        cachedMediaItems = children;

                        /* Hacky Solution: When the children are loaded we wait for some time so the EpisodesAdapter.cachedViewHolder
                         * can take some time to initialize.*/
                        Thread markPositionTask = new Thread(() -> {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //  If a track is allready playing, mark it's position in the list
                            MediaMetadataCompat metadata = mediaController.getMetadata();
                            if (metadata != null) {
                                markPlayingItemInList(metadata);
                            }
                        });
                        markPositionTask.start();
                    }

                    @Override
                    public void onError(@NonNull String parentId) {
                        viewMvc.displayLoadingIndicator(false);
                        cachedMediaItems = null;
                        Toast.makeText(getContext(), "Error fetching episodes!", Toast.LENGTH_SHORT).show();
                    }
                });

                mediaController.registerCallback(mediaControllerCallback = new MediaControllerCompat.Callback() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        //  If a track is allready playing, mark it's position in the list
                        if (metadata != null) {
                            markPlayingItemInList(metadata);
                        }
                    }
                });

            } catch (RemoteException e) {
                Log.e(TAG, ": Unable to access this media session");
                e.printStackTrace();
            }
        }

        @Override
        public void onConnectionSuspended() {
            //  Service has crashed. Disable transport controls until it automatically reconnects
        }

        @Override
        public void onConnectionFailed() {
            //  Service has refused our connection
        }
    }

    private void markPlayingItemInList(MediaMetadataCompat metadata) {
        String playingItemMediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        int indexToSelectInTheList = viewMvc.getItemPositionFromMediaId(playingItemMediaId);
        if (indexToSelectInTheList != EpisodesAdapter.INVALID_INDEX_POSITION) {
            viewMvc.markSelectedPosition(indexToSelectInTheList);
        }
    }
}
