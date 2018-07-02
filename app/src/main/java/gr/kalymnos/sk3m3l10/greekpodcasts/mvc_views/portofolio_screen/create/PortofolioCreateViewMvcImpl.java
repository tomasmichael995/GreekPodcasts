package gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.portofolio_screen.create;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import gr.kalymnos.sk3m3l10.greekpodcasts.R;

public class PortofolioCreateViewMvcImpl implements PortofolioCreateViewMvc {

    private View rootView;
    private EditText titleEditText, descriptionEditText;
    private Spinner categorySpinner;
    private ImageView updatePodcastImageView;

    public PortofolioCreateViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        initializeViews(inflater, parent);
    }

    private void initializeViews(LayoutInflater inflater, ViewGroup parent) {
        rootView = inflater.inflate(R.layout.portofolio_creation, parent, false);
        titleEditText = rootView.findViewById(R.id.podcast_title_editText);
        descriptionEditText = rootView.findViewById(R.id.description_editText);
        categorySpinner = rootView.findViewById(R.id.categories_spinner);
        updatePodcastImageView = rootView.findViewById(R.id.update_podcast_pic_imageview);
    }

    @Override
    public void bindPoster(Bitmap poster) {
        updatePodcastImageView.setImageBitmap(poster);
    }

    @Override
    public void setOnPosterClickListener(OnPosterClickListener listener) {
        updatePodcastImageView.setOnClickListener(view -> {
            if (listener != null)
                listener.onPosterClick();
        });
    }

    @Override
    public View getRootView() {
        return rootView;
    }
}