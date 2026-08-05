package com.necroware.terminusplayer.ui.screens.library;

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
public final class LibraryViewModel_Factory implements Factory<LibraryViewModel> {
  private final Provider<MusicRepository> repositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  public LibraryViewModel_Factory(Provider<MusicRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public LibraryViewModel get() {
    return newInstance(repositoryProvider.get(), preferencesRepositoryProvider.get(), playbackControllerProvider.get());
  }

  public static LibraryViewModel_Factory create(Provider<MusicRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    return new LibraryViewModel_Factory(repositoryProvider, preferencesRepositoryProvider, playbackControllerProvider);
  }

  public static LibraryViewModel newInstance(MusicRepository repository,
      UserPreferencesRepository preferencesRepository, PlaybackController playbackController) {
    return new LibraryViewModel(repository, preferencesRepository, playbackController);
  }
}
