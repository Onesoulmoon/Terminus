package com.necroware.terminusplayer.di;

import com.necroware.terminusplayer.data.database.TerminusDatabase;
import com.necroware.terminusplayer.data.database.dao.SongDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideSongDaoFactory implements Factory<SongDao> {
  private final Provider<TerminusDatabase> dbProvider;

  public DatabaseModule_ProvideSongDaoFactory(Provider<TerminusDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SongDao get() {
    return provideSongDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSongDaoFactory create(Provider<TerminusDatabase> dbProvider) {
    return new DatabaseModule_ProvideSongDaoFactory(dbProvider);
  }

  public static SongDao provideSongDao(TerminusDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSongDao(db));
  }
}
