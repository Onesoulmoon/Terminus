package com.necroware.terminusplayer;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.necroware.terminusplayer.data.database.TerminusDatabase;
import com.necroware.terminusplayer.data.database.dao.LikedSongDao;
import com.necroware.terminusplayer.data.database.dao.PlayEventDao;
import com.necroware.terminusplayer.data.database.dao.PlaylistDao;
import com.necroware.terminusplayer.data.database.dao.SongDao;
import com.necroware.terminusplayer.data.mediastore.MediaStoreScanner;
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository;
import com.necroware.terminusplayer.data.repository.MusicRepository;
import com.necroware.terminusplayer.data.repository.StatsRepository;
import com.necroware.terminusplayer.di.DatabaseModule_ProvideDatabaseFactory;
import com.necroware.terminusplayer.di.DatabaseModule_ProvideLikedSongDaoFactory;
import com.necroware.terminusplayer.di.DatabaseModule_ProvidePlayEventDaoFactory;
import com.necroware.terminusplayer.di.DatabaseModule_ProvidePlaylistDaoFactory;
import com.necroware.terminusplayer.di.DatabaseModule_ProvideSongDaoFactory;
import com.necroware.terminusplayer.di.PreferencesModule_ProvideDataStoreFactory;
import com.necroware.terminusplayer.playback.MusicService;
import com.necroware.terminusplayer.playback.MusicService_MembersInjector;
import com.necroware.terminusplayer.playback.PlaybackController;
import com.necroware.terminusplayer.ui.screens.albumdetail.AlbumDetailViewModel;
import com.necroware.terminusplayer.ui.screens.albumdetail.AlbumDetailViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.artistdetail.ArtistDetailViewModel;
import com.necroware.terminusplayer.ui.screens.artistdetail.ArtistDetailViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.home.HomeViewModel;
import com.necroware.terminusplayer.ui.screens.home.HomeViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.library.LibraryViewModel;
import com.necroware.terminusplayer.ui.screens.library.LibraryViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.nowplaying.PlaybackViewModel;
import com.necroware.terminusplayer.ui.screens.nowplaying.PlaybackViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.playlists.PlaylistDetailViewModel;
import com.necroware.terminusplayer.ui.screens.playlists.PlaylistDetailViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.playlists.PlaylistsViewModel;
import com.necroware.terminusplayer.ui.screens.playlists.PlaylistsViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.search.SearchViewModel;
import com.necroware.terminusplayer.ui.screens.search.SearchViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.settings.SettingsViewModel;
import com.necroware.terminusplayer.ui.screens.settings.SettingsViewModel_HiltModules;
import com.necroware.terminusplayer.ui.screens.stats.StatsViewModel;
import com.necroware.terminusplayer.ui.screens.stats.StatsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerTerminusApplication_HiltComponents_SingletonC {
  private DaggerTerminusApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public TerminusApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements TerminusApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public TerminusApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements TerminusApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public TerminusApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements TerminusApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public TerminusApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements TerminusApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TerminusApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements TerminusApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TerminusApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements TerminusApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public TerminusApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements TerminusApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public TerminusApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends TerminusApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends TerminusApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends TerminusApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends TerminusApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(11).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_albumdetail_AlbumDetailViewModel, AlbumDetailViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_artistdetail_ArtistDetailViewModel, ArtistDetailViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_library_LibraryViewModel, LibraryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_MainActivityViewModel, MainActivityViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_nowplaying_PlaybackViewModel, PlaybackViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_playlists_PlaylistDetailViewModel, PlaylistDetailViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_playlists_PlaylistsViewModel, PlaylistsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_search_SearchViewModel, SearchViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_stats_StatsViewModel, StatsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_necroware_terminusplayer_ui_screens_stats_StatsViewModel = "com.necroware.terminusplayer.ui.screens.stats.StatsViewModel";

      static String com_necroware_terminusplayer_ui_screens_search_SearchViewModel = "com.necroware.terminusplayer.ui.screens.search.SearchViewModel";

      static String com_necroware_terminusplayer_ui_screens_library_LibraryViewModel = "com.necroware.terminusplayer.ui.screens.library.LibraryViewModel";

      static String com_necroware_terminusplayer_MainActivityViewModel = "com.necroware.terminusplayer.MainActivityViewModel";

      static String com_necroware_terminusplayer_ui_screens_playlists_PlaylistsViewModel = "com.necroware.terminusplayer.ui.screens.playlists.PlaylistsViewModel";

      static String com_necroware_terminusplayer_ui_screens_settings_SettingsViewModel = "com.necroware.terminusplayer.ui.screens.settings.SettingsViewModel";

      static String com_necroware_terminusplayer_ui_screens_albumdetail_AlbumDetailViewModel = "com.necroware.terminusplayer.ui.screens.albumdetail.AlbumDetailViewModel";

      static String com_necroware_terminusplayer_ui_screens_home_HomeViewModel = "com.necroware.terminusplayer.ui.screens.home.HomeViewModel";

      static String com_necroware_terminusplayer_ui_screens_artistdetail_ArtistDetailViewModel = "com.necroware.terminusplayer.ui.screens.artistdetail.ArtistDetailViewModel";

      static String com_necroware_terminusplayer_ui_screens_nowplaying_PlaybackViewModel = "com.necroware.terminusplayer.ui.screens.nowplaying.PlaybackViewModel";

      static String com_necroware_terminusplayer_ui_screens_playlists_PlaylistDetailViewModel = "com.necroware.terminusplayer.ui.screens.playlists.PlaylistDetailViewModel";

      @KeepFieldType
      StatsViewModel com_necroware_terminusplayer_ui_screens_stats_StatsViewModel2;

      @KeepFieldType
      SearchViewModel com_necroware_terminusplayer_ui_screens_search_SearchViewModel2;

      @KeepFieldType
      LibraryViewModel com_necroware_terminusplayer_ui_screens_library_LibraryViewModel2;

      @KeepFieldType
      MainActivityViewModel com_necroware_terminusplayer_MainActivityViewModel2;

      @KeepFieldType
      PlaylistsViewModel com_necroware_terminusplayer_ui_screens_playlists_PlaylistsViewModel2;

      @KeepFieldType
      SettingsViewModel com_necroware_terminusplayer_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      AlbumDetailViewModel com_necroware_terminusplayer_ui_screens_albumdetail_AlbumDetailViewModel2;

      @KeepFieldType
      HomeViewModel com_necroware_terminusplayer_ui_screens_home_HomeViewModel2;

      @KeepFieldType
      ArtistDetailViewModel com_necroware_terminusplayer_ui_screens_artistdetail_ArtistDetailViewModel2;

      @KeepFieldType
      PlaybackViewModel com_necroware_terminusplayer_ui_screens_nowplaying_PlaybackViewModel2;

      @KeepFieldType
      PlaylistDetailViewModel com_necroware_terminusplayer_ui_screens_playlists_PlaylistDetailViewModel2;
    }
  }

  private static final class ViewModelCImpl extends TerminusApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AlbumDetailViewModel> albumDetailViewModelProvider;

    private Provider<ArtistDetailViewModel> artistDetailViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<LibraryViewModel> libraryViewModelProvider;

    private Provider<MainActivityViewModel> mainActivityViewModelProvider;

    private Provider<PlaybackViewModel> playbackViewModelProvider;

    private Provider<PlaylistDetailViewModel> playlistDetailViewModelProvider;

    private Provider<PlaylistsViewModel> playlistsViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<StatsViewModel> statsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.albumDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.artistDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.libraryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.mainActivityViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.playbackViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.playlistDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.playlistsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.statsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(11).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_albumdetail_AlbumDetailViewModel, ((Provider) albumDetailViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_artistdetail_ArtistDetailViewModel, ((Provider) artistDetailViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_library_LibraryViewModel, ((Provider) libraryViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_MainActivityViewModel, ((Provider) mainActivityViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_nowplaying_PlaybackViewModel, ((Provider) playbackViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_playlists_PlaylistDetailViewModel, ((Provider) playlistDetailViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_playlists_PlaylistsViewModel, ((Provider) playlistsViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_search_SearchViewModel, ((Provider) searchViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_necroware_terminusplayer_ui_screens_stats_StatsViewModel, ((Provider) statsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_necroware_terminusplayer_MainActivityViewModel = "com.necroware.terminusplayer.MainActivityViewModel";

      static String com_necroware_terminusplayer_ui_screens_stats_StatsViewModel = "com.necroware.terminusplayer.ui.screens.stats.StatsViewModel";

      static String com_necroware_terminusplayer_ui_screens_artistdetail_ArtistDetailViewModel = "com.necroware.terminusplayer.ui.screens.artistdetail.ArtistDetailViewModel";

      static String com_necroware_terminusplayer_ui_screens_settings_SettingsViewModel = "com.necroware.terminusplayer.ui.screens.settings.SettingsViewModel";

      static String com_necroware_terminusplayer_ui_screens_nowplaying_PlaybackViewModel = "com.necroware.terminusplayer.ui.screens.nowplaying.PlaybackViewModel";

      static String com_necroware_terminusplayer_ui_screens_home_HomeViewModel = "com.necroware.terminusplayer.ui.screens.home.HomeViewModel";

      static String com_necroware_terminusplayer_ui_screens_playlists_PlaylistDetailViewModel = "com.necroware.terminusplayer.ui.screens.playlists.PlaylistDetailViewModel";

      static String com_necroware_terminusplayer_ui_screens_library_LibraryViewModel = "com.necroware.terminusplayer.ui.screens.library.LibraryViewModel";

      static String com_necroware_terminusplayer_ui_screens_albumdetail_AlbumDetailViewModel = "com.necroware.terminusplayer.ui.screens.albumdetail.AlbumDetailViewModel";

      static String com_necroware_terminusplayer_ui_screens_playlists_PlaylistsViewModel = "com.necroware.terminusplayer.ui.screens.playlists.PlaylistsViewModel";

      static String com_necroware_terminusplayer_ui_screens_search_SearchViewModel = "com.necroware.terminusplayer.ui.screens.search.SearchViewModel";

      @KeepFieldType
      MainActivityViewModel com_necroware_terminusplayer_MainActivityViewModel2;

      @KeepFieldType
      StatsViewModel com_necroware_terminusplayer_ui_screens_stats_StatsViewModel2;

      @KeepFieldType
      ArtistDetailViewModel com_necroware_terminusplayer_ui_screens_artistdetail_ArtistDetailViewModel2;

      @KeepFieldType
      SettingsViewModel com_necroware_terminusplayer_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      PlaybackViewModel com_necroware_terminusplayer_ui_screens_nowplaying_PlaybackViewModel2;

      @KeepFieldType
      HomeViewModel com_necroware_terminusplayer_ui_screens_home_HomeViewModel2;

      @KeepFieldType
      PlaylistDetailViewModel com_necroware_terminusplayer_ui_screens_playlists_PlaylistDetailViewModel2;

      @KeepFieldType
      LibraryViewModel com_necroware_terminusplayer_ui_screens_library_LibraryViewModel2;

      @KeepFieldType
      AlbumDetailViewModel com_necroware_terminusplayer_ui_screens_albumdetail_AlbumDetailViewModel2;

      @KeepFieldType
      PlaylistsViewModel com_necroware_terminusplayer_ui_screens_playlists_PlaylistsViewModel2;

      @KeepFieldType
      SearchViewModel com_necroware_terminusplayer_ui_screens_search_SearchViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.necroware.terminusplayer.ui.screens.albumdetail.AlbumDetailViewModel 
          return (T) new AlbumDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.statsRepositoryProvider.get(), singletonCImpl.playbackControllerProvider.get());

          case 1: // com.necroware.terminusplayer.ui.screens.artistdetail.ArtistDetailViewModel 
          return (T) new ArtistDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.statsRepositoryProvider.get(), singletonCImpl.playbackControllerProvider.get());

          case 2: // com.necroware.terminusplayer.ui.screens.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.statsRepositoryProvider.get());

          case 3: // com.necroware.terminusplayer.ui.screens.library.LibraryViewModel 
          return (T) new LibraryViewModel(singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get(), singletonCImpl.playbackControllerProvider.get());

          case 4: // com.necroware.terminusplayer.MainActivityViewModel 
          return (T) new MainActivityViewModel(singletonCImpl.userPreferencesRepositoryProvider.get());

          case 5: // com.necroware.terminusplayer.ui.screens.nowplaying.PlaybackViewModel 
          return (T) new PlaybackViewModel(singletonCImpl.playbackControllerProvider.get(), singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          case 6: // com.necroware.terminusplayer.ui.screens.playlists.PlaylistDetailViewModel 
          return (T) new PlaylistDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.playbackControllerProvider.get());

          case 7: // com.necroware.terminusplayer.ui.screens.playlists.PlaylistsViewModel 
          return (T) new PlaylistsViewModel(singletonCImpl.musicRepositoryProvider.get());

          case 8: // com.necroware.terminusplayer.ui.screens.search.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.playbackControllerProvider.get());

          case 9: // com.necroware.terminusplayer.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.musicRepositoryProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          case 10: // com.necroware.terminusplayer.ui.screens.stats.StatsViewModel 
          return (T) new StatsViewModel(singletonCImpl.statsRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends TerminusApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends TerminusApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectMusicService(MusicService musicService) {
      injectMusicService2(musicService);
    }

    @CanIgnoreReturnValue
    private MusicService injectMusicService2(MusicService instance) {
      MusicService_MembersInjector.injectStatsRepository(instance, singletonCImpl.statsRepositoryProvider.get());
      MusicService_MembersInjector.injectPreferencesRepository(instance, singletonCImpl.userPreferencesRepositoryProvider.get());
      MusicService_MembersInjector.injectMusicRepository(instance, singletonCImpl.musicRepositoryProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends TerminusApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<TerminusDatabase> provideDatabaseProvider;

    private Provider<MediaStoreScanner> mediaStoreScannerProvider;

    private Provider<MusicRepository> musicRepositoryProvider;

    private Provider<StatsRepository> statsRepositoryProvider;

    private Provider<PlaybackController> playbackControllerProvider;

    private Provider<DataStore<Preferences>> provideDataStoreProvider;

    private Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private SongDao songDao() {
      return DatabaseModule_ProvideSongDaoFactory.provideSongDao(provideDatabaseProvider.get());
    }

    private LikedSongDao likedSongDao() {
      return DatabaseModule_ProvideLikedSongDaoFactory.provideLikedSongDao(provideDatabaseProvider.get());
    }

    private PlayEventDao playEventDao() {
      return DatabaseModule_ProvidePlayEventDaoFactory.providePlayEventDao(provideDatabaseProvider.get());
    }

    private PlaylistDao playlistDao() {
      return DatabaseModule_ProvidePlaylistDaoFactory.providePlaylistDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<TerminusDatabase>(singletonCImpl, 1));
      this.mediaStoreScannerProvider = DoubleCheck.provider(new SwitchingProvider<MediaStoreScanner>(singletonCImpl, 2));
      this.musicRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MusicRepository>(singletonCImpl, 0));
      this.statsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<StatsRepository>(singletonCImpl, 3));
      this.playbackControllerProvider = DoubleCheck.provider(new SwitchingProvider<PlaybackController>(singletonCImpl, 4));
      this.provideDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStore<Preferences>>(singletonCImpl, 6));
      this.userPreferencesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferencesRepository>(singletonCImpl, 5));
    }

    @Override
    public void injectTerminusApplication(TerminusApplication terminusApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.necroware.terminusplayer.data.repository.MusicRepository 
          return (T) new MusicRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.songDao(), singletonCImpl.likedSongDao(), singletonCImpl.playEventDao(), singletonCImpl.playlistDao(), singletonCImpl.mediaStoreScannerProvider.get());

          case 1: // com.necroware.terminusplayer.data.database.TerminusDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.necroware.terminusplayer.data.mediastore.MediaStoreScanner 
          return (T) new MediaStoreScanner(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.necroware.terminusplayer.data.repository.StatsRepository 
          return (T) new StatsRepository(singletonCImpl.playEventDao());

          case 4: // com.necroware.terminusplayer.playback.PlaybackController 
          return (T) new PlaybackController(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.necroware.terminusplayer.data.prefs.UserPreferencesRepository 
          return (T) new UserPreferencesRepository(singletonCImpl.provideDataStoreProvider.get());

          case 6: // androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> 
          return (T) PreferencesModule_ProvideDataStoreFactory.provideDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
