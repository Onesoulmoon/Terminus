package com.necroware.terminusplayer;

import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository;
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
public final class MainActivityViewModel_Factory implements Factory<MainActivityViewModel> {
  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public MainActivityViewModel_Factory(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public MainActivityViewModel get() {
    return newInstance(preferencesRepositoryProvider.get());
  }

  public static MainActivityViewModel_Factory create(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new MainActivityViewModel_Factory(preferencesRepositoryProvider);
  }

  public static MainActivityViewModel newInstance(UserPreferencesRepository preferencesRepository) {
    return new MainActivityViewModel(preferencesRepository);
  }
}
