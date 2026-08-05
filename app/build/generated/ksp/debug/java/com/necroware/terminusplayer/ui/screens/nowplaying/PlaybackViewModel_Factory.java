package com.necroware.terminusplayer.ui.screens.nowplaying;

import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository;
import com.necroware.terminusplayer.data.repository.MusicRepository;
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

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public PlaybackViewModel_Factory(Provider<PlaybackController> controllerProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.controllerProvider = controllerProvider;
    this.repositoryProvider = repositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public PlaybackViewModel get() {
    return newInstance(controllerProvider.get(), repositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static PlaybackViewModel_Factory create(Provider<PlaybackController> controllerProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new PlaybackViewModel_Factory(controllerProvider, repositoryProvider, preferencesRepositoryProvider);
  }

  public static PlaybackViewModel newInstance(PlaybackController controller,
      MusicRepository repository, UserPreferencesRepository preferencesRepository) {
    return new PlaybackViewModel(controller, repository, preferencesRepository);
  }
}
