package com.necroware.terminusplayer.data.mediastore;

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
public final class MediaStoreScanner_Factory implements Factory<MediaStoreScanner> {
  private final Provider<Context> contextProvider;

  public MediaStoreScanner_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaStoreScanner get() {
    return newInstance(contextProvider.get());
  }

  public static MediaStoreScanner_Factory create(Provider<Context> contextProvider) {
    return new MediaStoreScanner_Factory(contextProvider);
  }

  public static MediaStoreScanner newInstance(Context context) {
    return new MediaStoreScanner(context);
  }
}
