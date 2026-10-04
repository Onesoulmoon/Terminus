package com.necroware.terminusplayer.ui.screens.home;

import com.necroware.terminusplayer.data.repository.MusicRepository;
import com.necroware.terminusplayer.data.repository.StatsRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<MusicRepository> repositoryProvider;

  private final Provider<StatsRepository> statsRepositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  private final Provider<AudioVisualizerHelper> visualizerHelperProvider;

  public HomeViewModel_Factory(Provider<MusicRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider,
      Provider<AudioVisualizerHelper> visualizerHelperProvider) {
    this.repositoryProvider = repositoryProvider;
    this.statsRepositoryProvider = statsRepositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
    this.visualizerHelperProvider = visualizerHelperProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repositoryProvider.get(), statsRepositoryProvider.get(), playbackControllerProvider.get(), visualizerHelperProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<MusicRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider,
      Provider<AudioVisualizerHelper> visualizerHelperProvider) {
    return new HomeViewModel_Factory(repositoryProvider, statsRepositoryProvider, playbackControllerProvider, visualizerHelperProvider);
  }

  public static HomeViewModel newInstance(MusicRepository repository,
      StatsRepository statsRepository, PlaybackController playbackController,
      AudioVisualizerHelper visualizerHelper) {
    return new HomeViewModel(repository, statsRepository, playbackController, visualizerHelper);
  }
}
