package com.necroware.terminusplayer.ui.screens.stats;

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
public final class StatsViewModel_Factory implements Factory<StatsViewModel> {
  private final Provider<StatsRepository> statsRepositoryProvider;

  public StatsViewModel_Factory(Provider<StatsRepository> statsRepositoryProvider) {
    this.statsRepositoryProvider = statsRepositoryProvider;
  }

  @Override
  public StatsViewModel get() {
    return newInstance(statsRepositoryProvider.get());
  }

  public static StatsViewModel_Factory create(Provider<StatsRepository> statsRepositoryProvider) {
    return new StatsViewModel_Factory(statsRepositoryProvider);
  }

  public static StatsViewModel newInstance(StatsRepository statsRepository) {
    return new StatsViewModel(statsRepository);
  }
}
