package gr.kalymnos.sk3m3l10.greekpodcasts.mvc_controllers.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Toast;

import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_controllers.fragments.portofolio.UploadAudioService;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_model.DataRepository;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_model.StaticFakeDataRepo;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.add_episode.AddEpisodeViewMvc;
import gr.kalymnos.sk3m3l10.greekpodcasts.mvc_views.add_episode.AddEpisodeViewMvcImpl;
import gr.kalymnos.sk3m3l10.greekpodcasts.pojos.Podcast;
import gr.kalymnos.sk3m3l10.greekpodcasts.utils.FileUtils;

public class AddEpisodeActivity extends AppCompatActivity implements AddEpisodeViewMvc.OnActionsClickListener {

    private static final int RC_AUDIO_PIC = 1442;
    private AddEpisodeViewMvc viewMvc;
    private DataRepository repo;

    private Uri cachedAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMvc = new AddEpisodeViewMvcImpl(LayoutInflater.from(this), null);
        viewMvc.setOnActionsClickListener(this);
        //  TODO: Replace with a real service
        repo = new StaticFakeDataRepo();
        setContentView(viewMvc.getRootView());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_AUDIO_PIC) {
            if (resultCode == RESULT_OK && data != null) {
                cachedAudioUri = data.getData();
                viewMvc.bindFileName(FileUtils.fileName(getContentResolver(),cachedAudioUri));
                viewMvc.displayAudioHint(false);
                viewMvc.displayFileName(true);
                viewMvc.drawHeadsetMic(true);
            }else{
                cachedAudioUri=null;
                viewMvc.bindFileName(null);
                viewMvc.displayAudioHint(true);
                viewMvc.displayFileName(false);
                viewMvc.drawHeadsetMic(false);
            }
        }
    }

    @Override
    public void onInsertAudioClick() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, RC_AUDIO_PIC);
    }

    @Override
    public void onUploadActionClick() {
        if (!TextUtils.isEmpty(viewMvc.getInsertedTitle()) && cachedAudioUri != null) {
            UploadAudioService.startActionUploadAudio(this, cachedAudioUri,getIntent().getExtras().getString(Podcast.PUSH_ID_KEY));
        }
    }
}
