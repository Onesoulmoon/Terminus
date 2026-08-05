package com.necroware.terminusplayer.ui.screens.settings;

import android.content.Context;
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository;
import com.necroware.terminusplayer.data.repository.MusicRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<MusicRepository> repositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public SettingsViewModel_Factory(Provider<Context> contextProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.repositoryProvider = repositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(contextProvider.get(), repositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<Context> contextProvider,
      Provider<MusicRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new SettingsViewModel_Factory(contextProvider, repositoryProvider, preferencesRepositoryProvider);
  }

  public static SettingsViewModel newInstance(Context context, MusicRepository repository,
      UserPreferencesRepository preferencesRepository) {
    return new SettingsViewModel(context, repository, preferencesRepository);
  }
}
