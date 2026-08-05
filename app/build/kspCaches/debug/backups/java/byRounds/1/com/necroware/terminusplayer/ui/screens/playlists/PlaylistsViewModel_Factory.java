package com.necroware.terminusplayer.ui.screens.playlists;

import com.necroware.terminusplayer.data.repository.MusicRepository;
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
public final class PlaylistsViewModel_Factory implements Factory<PlaylistsViewModel> {
  private final Provider<MusicRepository> repositoryProvider;

  public PlaylistsViewModel_Factory(Provider<MusicRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public PlaylistsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static PlaylistsViewModel_Factory create(Provider<MusicRepository> repositoryProvider) {
    return new PlaylistsViewModel_Factory(repositoryProvider);
  }

  public static PlaylistsViewModel newInstance(MusicRepository repository) {
    return new PlaylistsViewModel(repository);
  }
}
