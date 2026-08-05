package com.necroware.terminusplayer.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.necroware.terminusplayer.data.database.entity.PlaylistEntity;
import com.necroware.terminusplayer.data.database.entity.PlaylistSongEntity;
import com.necroware.terminusplayer.data.database.entity.SongEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class PlaylistDao_Impl implements PlaylistDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlaylistEntity> __insertionAdapterOfPlaylistEntity;

  private final EntityInsertionAdapter<PlaylistSongEntity> __insertionAdapterOfPlaylistSongEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeletePlaylist;

  private final SharedSQLiteStatement __preparedStmtOfDeletePlaylistSongs;

  public PlaylistDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlaylistEntity = new EntityInsertionAdapter<PlaylistEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `playlists` (`id`,`name`,`createdAt`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaylistEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfPlaylistSongEntity = new EntityInsertionAdapter<PlaylistSongEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `playlist_songs` (`playlistId`,`songId`,`position`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaylistSongEntity entity) {
        statement.bindLong(1, entity.getPlaylistId());
        statement.bindLong(2, entity.getSongId());
        statement.bindLong(3, entity.getPosition());
      }
    };
    this.__preparedStmtOfDeletePlaylist = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlists WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePlaylistSongs = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlist_songs WHERE playlistId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPlaylist(final PlaylistEntity playlist,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPlaylistEntity.insertAndReturnId(playlist);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPlaylistSongs(final List<PlaylistSongEntity> songs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaylistSongEntity.insert(songs);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlaylist(final long playlistId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePlaylist.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, playlistId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePlaylist.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlaylistSongs(final long playlistId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePlaylistSongs.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, playlistId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePlaylistSongs.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlaylistWithCount>> observePlaylists() {
    final String _sql = "\n"
            + "        SELECT playlists.id as id, playlists.name as name, playlists.createdAt as createdAt,\n"
            + "               COUNT(playlist_songs.songId) as songCount\n"
            + "        FROM playlists\n"
            + "        LEFT JOIN playlist_songs ON playlist_songs.playlistId = playlists.id\n"
            + "        GROUP BY playlists.id\n"
            + "        ORDER BY playlists.createdAt DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"playlists",
        "playlist_songs"}, new Callable<List<PlaylistWithCount>>() {
      @Override
      @NonNull
      public List<PlaylistWithCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfName = 1;
          final int _cursorIndexOfCreatedAt = 2;
          final int _cursorIndexOfSongCount = 3;
          final List<PlaylistWithCount> _result = new ArrayList<PlaylistWithCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlaylistWithCount _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpSongCount;
            _tmpSongCount = _cursor.getInt(_cursorIndexOfSongCount);
            _item = new PlaylistWithCount(_tmpId,_tmpName,_tmpCreatedAt,_tmpSongCount);
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
  public Object getSongsForPlaylist(final long playlistId,
      final Continuation<? super List<SongEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT songs.* FROM songs\n"
            + "        INNER JOIN playlist_songs ON playlist_songs.songId = songs.mediaStoreId\n"
            + "        WHERE playlist_songs.playlistId = ?\n"
            + "        ORDER BY playlist_songs.position ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, playlistId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SongEntity>>() {
      @Override
      @NonNull
      public List<SongEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaStoreId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfAlbum = CursorUtil.getColumnIndexOrThrow(_cursor, "album");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfUriString = CursorUtil.getColumnIndexOrThrow(_cursor, "uriString");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAdded");
          final int _cursorIndexOfTrackNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "trackNumber");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfFolderPath = CursorUtil.getColumnIndexOrThrow(_cursor, "folderPath");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final List<SongEntity> _result = new ArrayList<SongEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SongEntity _item;
            final long _tmpMediaStoreId;
            _tmpMediaStoreId = _cursor.getLong(_cursorIndexOfMediaStoreId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final String _tmpAlbum;
            _tmpAlbum = _cursor.getString(_cursorIndexOfAlbum);
            final long _tmpAlbumId;
            _tmpAlbumId = _cursor.getLong(_cursorIndexOfAlbumId);
            final long _tmpDuration;
            _tmpDuration = _cursor.getLong(_cursorIndexOfDuration);
            final String _tmpUriString;
            _tmpUriString = _cursor.getString(_cursorIndexOfUriString);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final int _tmpTrackNumber;
            _tmpTrackNumber = _cursor.getInt(_cursorIndexOfTrackNumber);
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            final String _tmpFolderPath;
            _tmpFolderPath = _cursor.getString(_cursorIndexOfFolderPath);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            _item = new SongEntity(_tmpMediaStoreId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpAlbumId,_tmpDuration,_tmpUriString,_tmpDateAdded,_tmpTrackNumber,_tmpYear,_tmpFolderPath,_tmpSizeBytes);
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
  public Object getPlaylistName(final long playlistId,
      final Continuation<? super String> $completion) {
    final String _sql = "SELECT name FROM playlists WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, playlistId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<String>() {
      @Override
      @Nullable
      public String call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final String _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getString(0);
            }
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
