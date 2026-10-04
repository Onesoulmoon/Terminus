package com.necroware.terminusplayer.ui.screens.nowplaying;

import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository;
import com.necroware.terminusplayer.data.repository.MusicRepository;
import com.necroware.terminusplayer.data.repository.TrackMetadataRepository;
import com.necroware.terminusplayer.playback.AudioVisualizerHelper;
import com.necroware.terminusplayer.playback.PlaybackController;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class PlaybackViewModel_Factory implements Factory<PlaybackViewModel> {
  private final Provider<PlaybackController> controllerProvider;

  private final Provider<MusicRepository> repositoryProvider;

  private final Provider<TrackMetadataRepository> metadataRepositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<AudioVisualizerHelper> visualizerHelperProvider;

  public PlaybackViewModel_Factory(Provider<PlaybackController> controllerProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<TrackMetadataRepository> metadataRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<AudioVisualizerHelper> visualizerHelperProvider) {
    this.controllerProvider = controllerProvider;
    this.repositoryProvider = repositoryProvider;
    this.metadataRepositoryProvider = metadataRepositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.visualizerHelperProvider = visualizerHelperProvider;
  }

  @Override
  public PlaybackViewModel get() {
    return newInstance(controllerProvider.get(), repositoryProvider.get(), metadataRepositoryProvider.get(), preferencesRepositoryProvider.get(), visualizerHelperProvider.get());
  }

  public static PlaybackViewModel_Factory create(Provider<PlaybackController> controllerProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<TrackMetadataRepository> metadataRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<AudioVisualizerHelper> visualizerHelperProvider) {
    return new PlaybackViewModel_Factory(controllerProvider, repositoryProvider, metadataRepositoryProvider, preferencesRepositoryProvider, visualizerHelperProvider);
  }

  public static PlaybackViewModel newInstance(PlaybackController controller,
      MusicRepository repository, TrackMetadataRepository metadataRepository,
      UserPreferencesRepository preferencesRepository, AudioVisualizerHelper visualizerHelper) {
    return new PlaybackViewModel(controller, repository, metadataRepository, preferencesRepository, visualizerHelper);
  }
}
