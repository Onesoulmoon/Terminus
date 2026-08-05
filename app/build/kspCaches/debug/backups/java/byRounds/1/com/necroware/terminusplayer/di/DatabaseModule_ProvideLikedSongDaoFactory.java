package com.necroware.terminusplayer.di;

import com.necroware.terminusplayer.data.database.TerminusDatabase;
import com.necroware.terminusplayer.data.database.dao.LikedSongDao;
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
public final class DatabaseModule_ProvideLikedSongDaoFactory implements Factory<LikedSongDao> {
  private final Provider<TerminusDatabase> dbProvider;

  public DatabaseModule_ProvideLikedSongDaoFactory(Provider<TerminusDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LikedSongDao get() {
    return provideLikedSongDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideLikedSongDaoFactory create(
      Provider<TerminusDatabase> dbProvider) {
    return new DatabaseModule_ProvideLikedSongDaoFactory(dbProvider);
  }

  public static LikedSongDao provideLikedSongDao(TerminusDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLikedSongDao(db));
  }
}
