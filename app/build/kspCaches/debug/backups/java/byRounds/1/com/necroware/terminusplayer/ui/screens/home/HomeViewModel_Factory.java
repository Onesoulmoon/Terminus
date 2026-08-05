package com.necroware.terminusplayer.ui.screens.home;

import com.necroware.terminusplayer.data.repository.MusicRepository;
import com.necroware.terminusplayer.data.repository.StatsRepository;
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

  public HomeViewModel_Factory(Provider<MusicRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.statsRepositoryProvider = statsRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repositoryProvider.get(), statsRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<MusicRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider) {
    return new HomeViewModel_Factory(repositoryProvider, statsRepositoryProvider);
  }

  public static HomeViewModel newInstance(MusicRepository repository,
      StatsRepository statsRepository) {
    return new HomeViewModel(repository, statsRepository);
  }
}
