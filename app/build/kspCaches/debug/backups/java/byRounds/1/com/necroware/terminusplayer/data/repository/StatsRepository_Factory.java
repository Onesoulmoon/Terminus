package com.necroware.terminusplayer.data.repository;

import com.necroware.terminusplayer.data.database.dao.PlayEventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class StatsRepository_Factory implements Factory<StatsRepository> {
  private final Provider<PlayEventDao> playEventDaoProvider;

  public StatsRepository_Factory(Provider<PlayEventDao> playEventDaoProvider) {
    this.playEventDaoProvider = playEventDaoProvider;
  }

  @Override
  public StatsRepository get() {
    return newInstance(playEventDaoProvider.get());
  }

  public static StatsRepository_Factory create(Provider<PlayEventDao> playEventDaoProvider) {
    return new StatsRepository_Factory(playEventDaoProvider);
  }

  public static StatsRepository newInstance(PlayEventDao playEventDao) {
    return new StatsRepository(playEventDao);
  }
}
