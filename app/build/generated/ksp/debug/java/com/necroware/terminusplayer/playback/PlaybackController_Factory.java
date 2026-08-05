package com.necroware.terminusplayer.playback;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PlaybackController_Factory implements Factory<PlaybackController> {
  private final Provider<Context> contextProvider;

  public PlaybackController_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PlaybackController get() {
    return newInstance(contextProvider.get());
  }

  public static PlaybackController_Factory create(Provider<Context> contextProvider) {
    return new PlaybackController_Factory(contextProvider);
  }

  public static PlaybackController newInstance(Context context) {
    return new PlaybackController(context);
  }
}
