package com.necroware.terminusplayer;

import android.content.Context;
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository;
import com.necroware.terminusplayer.playback.PlaybackController;
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
public final class MainActivityViewModel_Factory implements Factory<MainActivityViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  public MainActivityViewModel_Factory(Provider<Context> contextProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    this.contextProvider = contextProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public MainActivityViewModel get() {
    return newInstance(contextProvider.get(), preferencesRepositoryProvider.get(), playbackControllerProvider.get());
  }

  public static MainActivityViewModel_Factory create(Provider<Context> contextProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    return new MainActivityViewModel_Factory(contextProvider, preferencesRepositoryProvider, playbackControllerProvider);
  }

  public static MainActivityViewModel newInstance(Context context,
      UserPreferencesRepository preferencesRepository, PlaybackController playbackController) {
    return new MainActivityViewModel(context, preferencesRepository, playbackController);
  }
}
