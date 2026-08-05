package com.necroware.terminusplayer.playback;

import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository;
import com.necroware.terminusplayer.data.repository.MusicRepository;
import com.necroware.terminusplayer.data.repository.StatsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MusicService_MembersInjector implements MembersInjector<MusicService> {
  private final Provider<StatsRepository> statsRepositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<MusicRepository> musicRepositoryProvider;

  public MusicService_MembersInjector(Provider<StatsRepository> statsRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<MusicRepository> musicRepositoryProvider) {
    this.statsRepositoryProvider = statsRepositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.musicRepositoryProvider = musicRepositoryProvider;
  }

  public static MembersInjector<MusicService> create(
      Provider<StatsRepository> statsRepositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<MusicRepository> musicRepositoryProvider) {
    return new MusicService_MembersInjector(statsRepositoryProvider, preferencesRepositoryProvider, musicRepositoryProvider);
  }

  @Override
  public void injectMembers(MusicService instance) {
    injectStatsRepository(instance, statsRepositoryProvider.get());
    injectPreferencesRepository(instance, preferencesRepositoryProvider.get());
    injectMusicRepository(instance, musicRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.necroware.terminusplayer.playback.MusicService.statsRepository")
  public static void injectStatsRepository(MusicService instance, StatsRepository statsRepository) {
    instance.statsRepository = statsRepository;
  }

  @InjectedFieldSignature("com.necroware.terminusplayer.playback.MusicService.preferencesRepository")
  public static void injectPreferencesRepository(MusicService instance,
      UserPreferencesRepository preferencesRepository) {
    instance.preferencesRepository = preferencesRepository;
  }

  @InjectedFieldSignature("com.necroware.terminusplayer.playback.MusicService.musicRepository")
  public static void injectMusicRepository(MusicService instance, MusicRepository musicRepository) {
    instance.musicRepository = musicRepository;
  }
}
