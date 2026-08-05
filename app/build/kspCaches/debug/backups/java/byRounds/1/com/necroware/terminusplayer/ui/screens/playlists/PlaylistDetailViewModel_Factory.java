package com.necroware.terminusplayer.ui.screens.playlists;

import androidx.lifecycle.SavedStateHandle;
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
public final class PlaylistDetailViewModel_Factory implements Factory<PlaylistDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<MusicRepository> repositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  public PlaylistDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.repositoryProvider = repositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public PlaylistDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), repositoryProvider.get(), playbackControllerProvider.get());
  }

  public static PlaylistDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    return new PlaylistDetailViewModel_Factory(savedStateHandleProvider, repositoryProvider, playbackControllerProvider);
  }

  public static PlaylistDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      MusicRepository repository, PlaybackController playbackController) {
    return new PlaylistDetailViewModel(savedStateHandle, repository, playbackController);
  }
}
