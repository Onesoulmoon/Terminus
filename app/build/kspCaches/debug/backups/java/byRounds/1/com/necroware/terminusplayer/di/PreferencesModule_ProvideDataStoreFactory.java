package com.necroware.terminusplayer.di;

import android.content.Context;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class PreferencesModule_ProvideDataStoreFactory implements Factory<DataStore<Preferences>> {
  private final Provider<Context> contextProvider;

  public PreferencesModule_ProvideDataStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DataStore<Preferences> get() {
    return provideDataStore(contextProvider.get());
  }

  public static PreferencesModule_ProvideDataStoreFactory create(
      Provider<Context> contextProvider) {
    return new PreferencesModule_ProvideDataStoreFactory(contextProvider);
  }

  public static DataStore<Preferences> provideDataStore(Context context) {
    return Preconditions.checkNotNullFromProvides(PreferencesModule.INSTANCE.provideDataStore(context));
  }
}
