package com.necroware.terminusplayer.data.repository;

import com.necroware.terminusplayer.data.database.dao.PlayEventDao;
import com.necroware.terminusplayer.data.database.dao.SongDao;
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

  private final Provider<SongDao> songDaoProvider;

  public StatsRepository_Factory(Provider<PlayEventDao> playEventDaoProvider,
      Provider<SongDao> songDaoProvider) {
    this.playEventDaoProvider = playEventDaoProvider;
    this.songDaoProvider = songDaoProvider;
  }

  @Override
  public StatsRepository get() {
    return newInstance(playEventDaoProvider.get(), songDaoProvider.get());
  }

  public static StatsRepository_Factory create(Provider<PlayEventDao> playEventDaoProvider,
      Provider<SongDao> songDaoProvider) {
    return new StatsRepository_Factory(playEventDaoProvider, songDaoProvider);
  }

  public static StatsRepository newInstance(PlayEventDao playEventDao, SongDao songDao) {
    return new StatsRepository(playEventDao, songDao);
  }
}
