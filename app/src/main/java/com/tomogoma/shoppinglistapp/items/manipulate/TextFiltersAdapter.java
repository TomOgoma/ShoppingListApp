package com.tomogoma.shoppinglistapp.items.manipulate;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.FilterQueryProvider;

/**
 * Created by ogoma on 05/03/15.
 */
public class TextFiltersAdapter extends SimpleCursorAdapter {

	private String mColumnFrom;
	private String mIDColumn;
	private Uri mProviderUri;

	public TextFiltersAdapter(Context context, Uri providerUri, String idColumn, String columnFrom) {

		super(
				context,
				android.R.layout.simple_list_item_1,
				null,
				new String[]{columnFrom},
				new int[]{android.R.id.text1},
				0
		);

		this.mColumnFrom = columnFrom;
		this.mIDColumn = idColumn;
		this.mProviderUri = providerUri;

		setFilterQueryProvider(new TextFilterQueryProvider());
		setCursorToStringConverter(new TextCursorToStringConverter());
	}

	private class TextFilterQueryProvider implements FilterQueryProvider {

		@Override
		public android.database.Cursor runQuery(CharSequence str) {

			String selection = mColumnFrom + " LIKE ? ";
			String[] selectionArgs = {"%" + str + "%"};
			String[] projection = new String[]{mIDColumn, mColumnFrom};
			return mContext.getContentResolver()
			              .query(mProviderUri, projection, selection, selectionArgs, null);
		}
	}

	private class TextCursorToStringConverter implements CursorToStringConverter {

		@Override
		public CharSequence convertToString(Cursor cursor) {

			int index = cursor.getColumnIndex(mColumnFrom);
			return cursor.getString(index);
		}
	}

}
