package com.necroware.terminusplayer.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.necroware.terminusplayer.data.database.entity.PlayEventEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlayEventDao_Impl implements PlayEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayEventEntity> __insertionAdapterOfPlayEventEntity;

  public PlayEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayEventEntity = new EntityInsertionAdapter<PlayEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `play_events` (`id`,`songId`,`artist`,`album`,`albumId`,`startedAtEpochMs`,`msPlayed`,`completed`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayEventEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSongId());
        statement.bindString(3, entity.getArtist());
        statement.bindString(4, entity.getAlbum());
        statement.bindLong(5, entity.getAlbumId());
        statement.bindLong(6, entity.getStartedAtEpochMs());
        statement.bindLong(7, entity.getMsPlayed());
        final int _tmp = entity.getCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
  }

  @Override
  public Object insert(final PlayEventEntity event, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlayEventEntity.insert(event);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlayEventEntity>> observeEventsSince(final long sinceEpochMs) {
    final String _sql = "SELECT * FROM play_events WHERE startedAtEpochMs >= ? ORDER BY startedAtEpochMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"play_events"}, new Callable<List<PlayEventEntity>>() {
      @Override
      @NonNull
      public List<PlayEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSongId = CursorUtil.getColumnIndexOrThrow(_cursor, "songId");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfStartedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAtEpochMs");
          final int _cursorIndexOfMsPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "msPlayed");
          final int _cursorIndexOfCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "completed");
          final List<PlayEventEntity> _result = new ArrayList<PlayEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlayEventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpAlbum;
            _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            final long _tmpAlbumId;
            _tmpAlbumId = _cursor.getLong(_cursorIndexOfAlbumId);
            final long _tmpStartedAtEpochMs;
            _tmpStartedAtEpochMs = _cursor.getLong(_cursorIndexOfStartedAtEpochMs);
            final long _tmpMsPlayed;
            _tmpMsPlayed = _cursor.getLong(_cursorIndexOfMsPlayed);
            final boolean _tmpCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCompleted);
            _tmpCompleted = _tmp != 0;
            _item = new PlayEventEntity(_tmpId,_tmpSongId,_tmpArtist,_tmpAlbum,_tmpAlbumId,_tmpStartedAtEpochMs,_tmpMsPlayed,_tmpCompleted);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object topArtists(final long sinceEpochMs, final int limit,
      final Continuation<? super List<ArtistPlayCount>> $completion) {
    final String _sql = "\n"
            + "        SELECT artist, COUNT(*) as playCount, SUM(msPlayed) as msPlayed\n"
            + "        FROM play_events\n"
            + "        WHERE startedAtEpochMs >= ?\n"
            + "        GROUP BY artist\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ArtistPlayCount>>() {
      @Override
      @NonNull
      public List<ArtistPlayCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfArtist = 0;
          final int _cursorIndexOfPlayCount = 1;
          final int _cursorIndexOfMsPlayed = 2;
          final List<ArtistPlayCount> _result = new ArrayList<ArtistPlayCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ArtistPlayCount _item;
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            final long _tmpMsPlayed;
            _tmpMsPlayed = _cursor.getLong(_cursorIndexOfMsPlayed);
            _item = new ArtistPlayCount(_tmpArtist,_tmpPlayCount,_tmpMsPlayed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object playsByDay(final long sinceEpochMs,
      final Continuation<? super List<DayPlayCount>> $completion) {
    final String _sql = "\n"
            + "        SELECT (startedAtEpochMs / 86400000) * 86400000 as dayEpoch,\n"
            + "               COUNT(*) as playCount,\n"
            + "               SUM(msPlayed) as msPlayed\n"
            + "        FROM play_events\n"
            + "        WHERE startedAtEpochMs >= ?\n"
            + "        GROUP BY dayEpoch\n"
            + "        ORDER BY dayEpoch ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DayPlayCount>>() {
      @Override
      @NonNull
      public List<DayPlayCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDayEpoch = 0;
          final int _cursorIndexOfPlayCount = 1;
          final int _cursorIndexOfMsPlayed = 2;
          final List<DayPlayCount> _result = new ArrayList<DayPlayCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DayPlayCount _item;
            final long _tmpDayEpoch;
            _tmpDayEpoch = _cursor.getLong(_cursorIndexOfDayEpoch);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            final long _tmpMsPlayed;
            _tmpMsPlayed = _cursor.getLong(_cursorIndexOfMsPlayed);
            _item = new DayPlayCount(_tmpDayEpoch,_tmpPlayCount,_tmpMsPlayed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object totalPlays(final long sinceEpochMs,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM play_events WHERE startedAtEpochMs >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object totalMsPlayed(final long sinceEpochMs,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COALESCE(SUM(msPlayed), 0) FROM play_events WHERE startedAtEpochMs >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object playsByHourOfDay(final long sinceEpochMs,
      final Continuation<? super List<HourHistogramRow>> $completion) {
    final String _sql = "\n"
            + "        SELECT CAST(strftime('%H', startedAtEpochMs / 1000, 'unixepoch', 'localtime') AS INTEGER) as hourOfDay,\n"
            + "               COUNT(*) as playCount\n"
            + "        FROM play_events\n"
            + "        WHERE startedAtEpochMs >= ?\n"
            + "        GROUP BY hourOfDay\n"
            + "        ORDER BY hourOfDay ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<HourHistogramRow>>() {
      @Override
      @NonNull
      public List<HourHistogramRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHourOfDay = 0;
          final int _cursorIndexOfPlayCount = 1;
          final List<HourHistogramRow> _result = new ArrayList<HourHistogramRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HourHistogramRow _item;
            final int _tmpHourOfDay;
            _tmpHourOfDay = _cursor.getInt(_cursorIndexOfHourOfDay);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _item = new HourHistogramRow(_tmpHourOfDay,_tmpPlayCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object recentlyPlayedSongIds(final int limit,
      final Continuation<? super List<RecentSongRow>> $completion) {
    final String _sql = "\n"
            + "        SELECT songId, MAX(startedAtEpochMs) as lastPlayedAt\n"
            + "        FROM play_events\n"
            + "        GROUP BY songId\n"
            + "        ORDER BY lastPlayedAt DESC\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecentSongRow>>() {
      @Override
      @NonNull
      public List<RecentSongRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfLastPlayedAt = 1;
          final List<RecentSongRow> _result = new ArrayList<RecentSongRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecentSongRow _item;
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final long _tmpLastPlayedAt;
            _tmpLastPlayedAt = _cursor.getLong(_cursorIndexOfLastPlayedAt);
            _item = new RecentSongRow(_tmpSongId,_tmpLastPlayedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object topPlayedSongIds(final int limit,
      final Continuation<? super List<TopSongRow>> $completion) {
    final String _sql = "\n"
            + "        SELECT songId, COUNT(*) as playCount\n"
            + "        FROM play_events\n"
            + "        GROUP BY songId\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TopSongRow>>() {
      @Override
      @NonNull
      public List<TopSongRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfPlayCount = 1;
          final List<TopSongRow> _result = new ArrayList<TopSongRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TopSongRow _item;
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _item = new TopSongRow(_tmpSongId,_tmpPlayCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object statsForArtist(final String artist,
      final Continuation<? super GroupStatsRow> $completion) {
    final String _sql = "SELECT COUNT(*) as totalPlays, COALESCE(SUM(msPlayed),0) as totalMsPlayed FROM play_events WHERE artist = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, artist);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GroupStatsRow>() {
      @Override
      @Nullable
      public GroupStatsRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTotalPlays = 0;
          final int _cursorIndexOfTotalMsPlayed = 1;
          final GroupStatsRow _result;
          if (_cursor.moveToFirst()) {
            final int _tmpTotalPlays;
            _tmpTotalPlays = _cursor.getInt(_cursorIndexOfTotalPlays);
            final long _tmpTotalMsPlayed;
            _tmpTotalMsPlayed = _cursor.getLong(_cursorIndexOfTotalMsPlayed);
            _result = new GroupStatsRow(_tmpTotalPlays,_tmpTotalMsPlayed);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object statsForAlbum(final long albumId,
      final Continuation<? super GroupStatsRow> $completion) {
    final String _sql = "SELECT COUNT(*) as totalPlays, COALESCE(SUM(msPlayed),0) as totalMsPlayed FROM play_events WHERE albumId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, albumId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GroupStatsRow>() {
      @Override
      @Nullable
      public GroupStatsRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTotalPlays = 0;
          final int _cursorIndexOfTotalMsPlayed = 1;
          final GroupStatsRow _result;
          if (_cursor.moveToFirst()) {
            final int _tmpTotalPlays;
            _tmpTotalPlays = _cursor.getInt(_cursorIndexOfTotalPlays);
            final long _tmpTotalMsPlayed;
            _tmpTotalMsPlayed = _cursor.getLong(_cursorIndexOfTotalMsPlayed);
            _result = new GroupStatsRow(_tmpTotalPlays,_tmpTotalMsPlayed);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object statsForAlbumTitle(final String albumTitle,
      final Continuation<? super GroupStatsRow> $completion) {
    final String _sql = "SELECT COUNT(*) as totalPlays, COALESCE(SUM(msPlayed),0) as totalMsPlayed FROM play_events WHERE album = ? COLLATE NOCASE";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, albumTitle);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<GroupStatsRow>() {
      @Override
      @Nullable
      public GroupStatsRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTotalPlays = 0;
          final int _cursorIndexOfTotalMsPlayed = 1;
          final GroupStatsRow _result;
          if (_cursor.moveToFirst()) {
            final int _tmpTotalPlays;
            _tmpTotalPlays = _cursor.getInt(_cursorIndexOfTotalPlays);
            final long _tmpTotalMsPlayed;
            _tmpTotalMsPlayed = _cursor.getLong(_cursorIndexOfTotalMsPlayed);
            _result = new GroupStatsRow(_tmpTotalPlays,_tmpTotalMsPlayed);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object topSongForArtist(final String artist,
      final Continuation<? super TopSongRow> $completion) {
    final String _sql = "\n"
            + "        SELECT songId, COUNT(*) as playCount\n"
            + "        FROM play_events\n"
            + "        WHERE artist = ?\n"
            + "        GROUP BY songId\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT 1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, artist);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TopSongRow>() {
      @Override
      @Nullable
      public TopSongRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfPlayCount = 1;
          final TopSongRow _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _result = new TopSongRow(_tmpSongId,_tmpPlayCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object topSongForAlbum(final long albumId,
      final Continuation<? super TopSongRow> $completion) {
    final String _sql = "\n"
            + "        SELECT songId, COUNT(*) as playCount\n"
            + "        FROM play_events\n"
            + "        WHERE albumId = ?\n"
            + "        GROUP BY songId\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT 1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, albumId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TopSongRow>() {
      @Override
      @Nullable
      public TopSongRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfPlayCount = 1;
          final TopSongRow _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _result = new TopSongRow(_tmpSongId,_tmpPlayCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object topSongForAlbumTitle(final String albumTitle,
      final Continuation<? super TopSongRow> $completion) {
    final String _sql = "\n"
            + "        SELECT songId, COUNT(*) as playCount\n"
            + "        FROM play_events\n"
            + "        WHERE album = ? COLLATE NOCASE\n"
            + "        GROUP BY songId\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT 1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, albumTitle);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TopSongRow>() {
      @Override
      @Nullable
      public TopSongRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfPlayCount = 1;
          final TopSongRow _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _result = new TopSongRow(_tmpSongId,_tmpPlayCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object topSongsWithTitles(final long sinceEpochMs, final int limit,
      final Continuation<? super List<TopSongTitleRow>> $completion) {
    final String _sql = "\n"
            + "        SELECT pe.songId as songId, s.title as title, COUNT(*) as playCount\n"
            + "        FROM play_events pe\n"
            + "        JOIN songs s ON s.mediaStoreId = pe.songId\n"
            + "        WHERE pe.startedAtEpochMs >= ?\n"
            + "        GROUP BY pe.songId\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TopSongTitleRow>>() {
      @Override
      @NonNull
      public List<TopSongTitleRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfTitle = 1;
          final int _cursorIndexOfPlayCount = 2;
          final List<TopSongTitleRow> _result = new ArrayList<TopSongTitleRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TopSongTitleRow _item;
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _item = new TopSongTitleRow(_tmpSongId,_tmpTitle,_tmpPlayCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object topAlbums(final long sinceEpochMs, final int limit,
      final Continuation<? super List<LabeledPlayCount>> $completion) {
    final String _sql = "\n"
            + "        SELECT album as label, COUNT(*) as playCount\n"
            + "        FROM play_events\n"
            + "        WHERE startedAtEpochMs >= ?\n"
            + "        GROUP BY album COLLATE NOCASE\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LabeledPlayCount>>() {
      @Override
      @NonNull
      public List<LabeledPlayCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfLabel = 0;
          final int _cursorIndexOfPlayCount = 1;
          final List<LabeledPlayCount> _result = new ArrayList<LabeledPlayCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LabeledPlayCount _item;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _item = new LabeledPlayCount(_tmpLabel,_tmpPlayCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object topSongForDateRange(final long startEpochMs, final long endEpochMs,
      final Continuation<? super TopSongTitleRow> $completion) {
    final String _sql = "\n"
            + "        SELECT pe.songId as songId, s.title as title, COUNT(*) as playCount\n"
            + "        FROM play_events pe\n"
            + "        JOIN songs s ON s.mediaStoreId = pe.songId\n"
            + "        WHERE pe.startedAtEpochMs >= ? AND pe.startedAtEpochMs < ?\n"
            + "        GROUP BY pe.songId\n"
            + "        ORDER BY playCount DESC\n"
            + "        LIMIT 1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startEpochMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endEpochMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TopSongTitleRow>() {
      @Override
      @Nullable
      public TopSongTitleRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfTitle = 1;
          final int _cursorIndexOfPlayCount = 2;
          final TopSongTitleRow _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _result = new TopSongTitleRow(_tmpSongId,_tmpTitle,_tmpPlayCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object playsByMonth(final long sinceEpochMs,
      final Continuation<? super List<MonthPlayCount>> $completion) {
    final String _sql = "\n"
            + "        SELECT strftime('%Y-%m', startedAtEpochMs / 1000, 'unixepoch', 'localtime') as monthLabel,\n"
            + "               COUNT(*) as playCount\n"
            + "        FROM play_events\n"
            + "        WHERE startedAtEpochMs >= ?\n"
            + "        GROUP BY monthLabel\n"
            + "        ORDER BY monthLabel ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MonthPlayCount>>() {
      @Override
      @NonNull
      public List<MonthPlayCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMonthLabel = 0;
          final int _cursorIndexOfPlayCount = 1;
          final List<MonthPlayCount> _result = new ArrayList<MonthPlayCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MonthPlayCount _item;
            final String _tmpMonthLabel;
            _tmpMonthLabel = _cursor.getString(_cursorIndexOfMonthLabel);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _item = new MonthPlayCount(_tmpMonthLabel,_tmpPlayCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
