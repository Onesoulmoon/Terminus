package com.necroware.terminusplayer.ui.screens.albumdetail;

import androidx.lifecycle.SavedStateHandle;
import com.necroware.terminusplayer.data.repository.MusicRepository;
import com.necroware.terminusplayer.data.repository.StatsRepository;
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
public final class AlbumDetailViewModel_Factory implements Factory<AlbumDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<MusicRepository> repositoryProvider;

  private final Provider<StatsRepository> statsRepositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  public AlbumDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.repositoryProvider = repositoryProvider;
    this.statsRepositoryProvider = statsRepositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public AlbumDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), repositoryProvider.get(), statsRepositoryProvider.get(), playbackControllerProvider.get());
  }

  public static AlbumDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    return new AlbumDetailViewModel_Factory(savedStateHandleProvider, repositoryProvider, statsRepositoryProvider, playbackControllerProvider);
  }

  public static AlbumDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      MusicRepository repository, StatsRepository statsRepository,
      PlaybackController playbackController) {
    return new AlbumDetailViewModel(savedStateHandle, repository, statsRepository, playbackController);
  }
}
