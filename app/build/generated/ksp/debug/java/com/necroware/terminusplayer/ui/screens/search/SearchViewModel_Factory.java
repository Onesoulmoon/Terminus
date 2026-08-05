package com.necroware.terminusplayer.ui.screens.search;

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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<MusicRepository> repositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  public SearchViewModel_Factory(Provider<MusicRepository> repositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(repositoryProvider.get(), playbackControllerProvider.get());
  }

  public static SearchViewModel_Factory create(Provider<MusicRepository> repositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    return new SearchViewModel_Factory(repositoryProvider, playbackControllerProvider);
  }

  public static SearchViewModel newInstance(MusicRepository repository,
      PlaybackController playbackController) {
    return new SearchViewModel(repository, playbackController);
  }
}
