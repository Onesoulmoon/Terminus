package com.necroware.terminusplayer.di;

import com.necroware.terminusplayer.data.database.TerminusDatabase;
import com.necroware.terminusplayer.data.database.dao.PlayEventDao;
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
public final class DatabaseModule_ProvidePlayEventDaoFactory implements Factory<PlayEventDao> {
  private final Provider<TerminusDatabase> dbProvider;

  public DatabaseModule_ProvidePlayEventDaoFactory(Provider<TerminusDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlayEventDao get() {
    return providePlayEventDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePlayEventDaoFactory create(
      Provider<TerminusDatabase> dbProvider) {
    return new DatabaseModule_ProvidePlayEventDaoFactory(dbProvider);
  }

  public static PlayEventDao providePlayEventDao(TerminusDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlayEventDao(db));
  }
}
