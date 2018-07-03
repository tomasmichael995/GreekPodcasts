package gr.kalymnos.sk3m3l10.greekpodcasts.mvc_controllers.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_model.DataRepository;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_model.StaticFakeDataRepo;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.portofolio_screen.publish.PortofolioPublishViewMvc;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.portofolio_screen.publish.PortofolioPublishViewMvcImpl;
import gr.kalymnos.sk3m3l10.greekpodcasts.pojos.Category;
import gr.kalymnos.sk3m3l10.greekpodcasts.pojos.Episode;
import gr.kalymnos.sk3m3l10.greekpodcasts.pojos.Podcast;

public class PortofolioPublishFragment extends Fragment implements LoaderManager.LoaderCallbacks<Object> {

    private static final String TAG = PortofolioPublishFragment.class.getSimpleName();

    private static final int PODCASTS_LOADER_ID = 121;
    private static final int EPISODES_LOADER_ID = 232;
    private static final int CATEGORIES_LOADER_ID = 343;

    private List<Podcast> cachedPodcasts;
    private List<Episode> cachedEpisodes;
    private List<Category> cachedCategories;


    private PortofolioPublishViewMvc viewMvc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewMvc = new PortofolioPublishViewMvcImpl(inflater, container);
        return viewMvc.getRootView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean isSavedStateValid = savedInstanceState != null && savedInstanceState.containsKey(Podcast.PODCASTS_KEY)
                && savedInstanceState.containsKey(Episode.EPISODES_KEY)
                && savedInstanceState.containsKey(Category.CATEGORIES_KEY);

        if (isSavedStateValid) {
            cachedPodcasts = savedInstanceState.getParcelableArrayList(Podcast.PODCASTS_KEY);
            cachedEpisodes = savedInstanceState.getParcelableArrayList(Episode.EPISODES_KEY);
            cachedCategories = savedInstanceState.getParcelableArrayList(Category.CATEGORIES_KEY);
        }

        getLoaderManager().restartLoader(PODCASTS_LOADER_ID, null, this);
        getLoaderManager().restartLoader(CATEGORIES_LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(Podcast.PODCASTS_KEY, (ArrayList<? extends Parcelable>) cachedPodcasts);
        outState.putParcelableArrayList(Episode.EPISODES_KEY, (ArrayList<? extends Parcelable>) cachedEpisodes);
        outState.putParcelableArrayList(Category.CATEGORIES_KEY, (ArrayList<? extends Parcelable>) cachedCategories);
    }

    @NonNull
    @Override
    public Loader<Object> onCreateLoader(int id, @Nullable Bundle args) {
        //  TODO: Replace with real service
        DataRepository repo = new StaticFakeDataRepo();

        switch (id) {
            case PODCASTS_LOADER_ID:
                return new AsyncTaskLoader<Object>(getContext()) {

                    @Override
                    protected void onStartLoading() {
                        if (cachedPodcasts != null) {
                            deliverResult(cachedPodcasts);
                        } else {
                            viewMvc.displayPodcastLoadingIndicator(true);
                            forceLoad();
                        }
                    }

                    @Nullable
                    @Override
                    public Object loadInBackground() {
                        return repo.fetchPodcastsFromPodcaster(repo.getCurrentUserUid());
                    }
                };
            case EPISODES_LOADER_ID:
                return new AsyncTaskLoader<Object>(getContext()) {

                    @Override
                    protected void onStartLoading() {
                        if (cachedEpisodes != null) {
                            deliverResult(cachedEpisodes);
                        } else {
                            viewMvc.displayEpisodesLoadingIndicator(true);
                            forceLoad();
                        }
                    }

                    @Nullable
                    @Override
                    public Object loadInBackground() {
                        if (cachedPodcasts != null) {
                            return repo.fetchEpisodes(cachedPodcasts.get(viewMvc.getSelectedPodcastPosition()).getFirebasePushId());
                        } else {
                            throw new UnsupportedOperationException(TAG + ": cachedPodcasts are null.");
                        }
                    }
                };
            case CATEGORIES_LOADER_ID:
                return new AsyncTaskLoader<Object>(getContext()) {

                    @Override
                    protected void onStartLoading() {
                        if (cachedCategories != null) {
                            deliverResult(cachedCategories);
                        } else {
                            viewMvc.displayCategoryLoadingIndicator(true);
                            forceLoad();
                        }
                    }

                    @Nullable
                    @Override
                    public Object loadInBackground() {
                        return repo.fetchAllCategories();
                    }
                };
            default:
                throw new UnsupportedOperationException(TAG + ": unknown loader id.");
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {
        if (data != null) {
            switch (loader.getId()) {
                case PODCASTS_LOADER_ID:
                    cachedPodcasts = (List<Podcast>) data;

                    if (cachedPodcasts != null) {
                        viewMvc.displayPodcastLoadingIndicator(false);

                        //  Create the titles array.
                        String[] titles = new String[cachedPodcasts.size()];
                        for (int i = 0; i < cachedPodcasts.size(); i++) {
                            titles[i] = cachedPodcasts.get(i).getTitle();
                        }

                        viewMvc.addPodcastsToSpinner(titles);

                        //  Make sure that when episodes are fetched, cachedPodcasts will not be null.
                        getLoaderManager().restartLoader(EPISODES_LOADER_ID, null, PortofolioPublishFragment.this);
                    } else {
                        throw new IllegalArgumentException(TAG + ": data should be of type List<Podcast>.");
                    }
                    break;

                case EPISODES_LOADER_ID:
                    cachedEpisodes = (List<Episode>) data;

                    if (cachedEpisodes != null) {
                        viewMvc.displayEpisodesLoadingIndicator(false);

                    } else {
                        throw new IllegalArgumentException(TAG + ": data should be of type List<Episode>.");
                    }
                    break;
                case CATEGORIES_LOADER_ID:
                    cachedCategories = (List<Category>) data;

                    if (cachedCategories != null) {
                        viewMvc.displayCategoryLoadingIndicator(false);
                        
                        //  Create the titles array.
                        String[] titles = new String[cachedCategories.size()];
                        for (int i = 0; i < cachedCategories.size(); i++) {
                            titles[i] = cachedCategories.get(i).getTitle();
                        }

                        viewMvc.addCategoriesToSpinner(titles);
                    } else {
                        throw new IllegalArgumentException(TAG + ": data should be of type List<Category>.");
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(TAG + ": unknown loader id.");
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Object> loader) {

    }
}
