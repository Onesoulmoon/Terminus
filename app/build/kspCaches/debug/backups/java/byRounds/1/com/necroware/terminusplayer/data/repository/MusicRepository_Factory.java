package com.necroware.terminusplayer.data.repository;

import android.content.Context;
import com.necroware.terminusplayer.data.database.dao.LikedSongDao;
import com.necroware.terminusplayer.data.database.dao.PlayEventDao;
import com.necroware.terminusplayer.data.database.dao.PlaylistDao;
import com.necroware.terminusplayer.data.database.dao.SongDao;
import com.necroware.terminusplayer.data.mediastore.MediaStoreScanner;
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
public final class MusicRepository_Factory implements Factory<MusicRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<SongDao> songDaoProvider;

  private final Provider<LikedSongDao> likedSongDaoProvider;

  private final Provider<PlayEventDao> playEventDaoProvider;

  private final Provider<PlaylistDao> playlistDaoProvider;

  private final Provider<MediaStoreScanner> scannerProvider;

  public MusicRepository_Factory(Provider<Context> contextProvider,
      Provider<SongDao> songDaoProvider, Provider<LikedSongDao> likedSongDaoProvider,
      Provider<PlayEventDao> playEventDaoProvider, Provider<PlaylistDao> playlistDaoProvider,
      Provider<MediaStoreScanner> scannerProvider) {
    this.contextProvider = contextProvider;
    this.songDaoProvider = songDaoProvider;
    this.likedSongDaoProvider = likedSongDaoProvider;
    this.playEventDaoProvider = playEventDaoProvider;
    this.playlistDaoProvider = playlistDaoProvider;
    this.scannerProvider = scannerProvider;
  }

  @Override
  public MusicRepository get() {
    return newInstance(contextProvider.get(), songDaoProvider.get(), likedSongDaoProvider.get(), playEventDaoProvider.get(), playlistDaoProvider.get(), scannerProvider.get());
  }

  public static MusicRepository_Factory create(Provider<Context> contextProvider,
      Provider<SongDao> songDaoProvider, Provider<LikedSongDao> likedSongDaoProvider,
      Provider<PlayEventDao> playEventDaoProvider, Provider<PlaylistDao> playlistDaoProvider,
      Provider<MediaStoreScanner> scannerProvider) {
    return new MusicRepository_Factory(contextProvider, songDaoProvider, likedSongDaoProvider, playEventDaoProvider, playlistDaoProvider, scannerProvider);
  }

  public static MusicRepository newInstance(Context context, SongDao songDao,
      LikedSongDao likedSongDao, PlayEventDao playEventDao, PlaylistDao playlistDao,
      MediaStoreScanner scanner) {
    return new MusicRepository(context, songDao, likedSongDao, playEventDao, playlistDao, scanner);
  }
}
