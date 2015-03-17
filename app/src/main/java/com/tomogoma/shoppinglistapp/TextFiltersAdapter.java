package com.tomogoma.shoppinglistapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.FilterQueryProvider;

/**
 * Created by ogoma on 05/03/15.
 */
public class TextFiltersAdapter extends SimpleCursorAdapter {

	private String columnFrom;
	private String idColumn;
	private Uri providerUri;
	private Context context;

	public TextFiltersAdapter(Context context, Uri providerUri, String idColumn, String columnFrom) {

		super(
				context,
				android.R.layout.simple_list_item_1,
				null,
				new String[]{columnFrom},
				new int[]{android.R.id.text1},
				0
		);

		this.columnFrom = columnFrom;
		this.idColumn = idColumn;
		this.providerUri = providerUri;
		this.context = context;

		setFilterQueryProvider(new TextFilterQueryProvider());
		setCursorToStringConverter(new TextCursorToStringConverter());
	}

	private class TextFilterQueryProvider implements FilterQueryProvider {

		@Override
		public android.database.Cursor runQuery(CharSequence str) {

			String selection = columnFrom + " LIKE ? ";
			String[] selectionArgs = {"%" + str + "%"};
			String[] projection = new String[]{idColumn, columnFrom};
			return context.getContentResolver()
			              .query(providerUri, projection, selection, selectionArgs, null);
		}
	}

	private class TextCursorToStringConverter implements CursorToStringConverter {

		@Override
		public CharSequence convertToString(Cursor cursor) {

			int index = cursor.getColumnIndex(columnFrom);
			return cursor.getString(index);
		}
	}

}
