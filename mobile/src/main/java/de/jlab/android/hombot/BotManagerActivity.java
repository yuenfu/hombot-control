package de.jlab.android.hombot;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import de.jlab.android.hombot.data.BotCursorAdapter;
import de.jlab.android.hombot.data.EditBotDialog;
import de.jlab.android.hombot.common.data.HombotDataContract;
import de.jlab.android.hombot.common.data.HombotDataOpenHelper;
import de.jlab.android.hombot.utils.Colorizer;

public class BotManagerActivity extends AppCompatActivity implements EditBotDialog.BotEditDialogListener {

    private BotCursorAdapter mBotAdapter;

    private static class ViewHolder {
        Toolbar windowToolbar;
        FloatingActionButton fab;
    }
    private ViewHolder mViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_manager);

        mViewHolder = new ViewHolder();
        mViewHolder.windowToolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewHolder.fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(mViewHolder.windowToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        mViewHolder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                ContentValues values = new ContentValues();
                values.put(HombotDataContract.BotEntry.COLUMN_NAME_NAME, "Testbot");
                values.put(HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS, "192.168.0.30:6260");
                db.insertOrThrow(HombotDataContract.BotEntry.TABLE_NAME, null, values);
                */
                Snackbar.make(view, "Testbot added", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showEditDialog(null, null, -1);
            }
        });


        final ListView botList = (ListView) findViewById(R.id.bot_list);
        mBotAdapter = new BotCursorAdapter(this, null);
        loadBots();
        botList.setAdapter(mBotAdapter);

        mBotAdapter.setSecondaryItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = botList.getPositionForView(v);
                Cursor cursor = mBotAdapter.getCursor();
                cursor.moveToPosition(position);
                long botId = cursor.getLong(cursor.getColumnIndex(HombotDataContract.BotEntry._ID));

                deleteBot(botId);
            }
        });

        botList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((CursorAdapter)parent.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                long botId = cursor.getLong(cursor.getColumnIndex(HombotDataContract.BotEntry._ID));
                String name = cursor.getString(cursor.getColumnIndex(HombotDataContract.BotEntry.COLUMN_NAME_NAME));
                String address = cursor.getString(cursor.getColumnIndex(HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS));

                showEditDialog(name, address, botId);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        Colorizer colorizer = new Colorizer(this);

        getWindow().getDecorView().setBackgroundColor(colorizer.getColorBackground());
        colorizer.colorizeToolbar(mViewHolder.windowToolbar, this);

        colorizer.colorizeDrawable(mViewHolder.fab.getBackground(), colorizer.getColorPrimary());
        colorizer.colorizeDrawable(mViewHolder.fab.getDrawable(), colorizer.getContrastingTextColor(colorizer.getColorPrimary()));
    }

    private void deleteBot(long id) {
        HombotDataOpenHelper dataHelper = new HombotDataOpenHelper(this);
        SQLiteDatabase db = dataHelper.getWritableDatabase();

        db.delete(HombotDataContract.BotEntry.TABLE_NAME, HombotDataContract.BotEntry._ID + "=" + id, null);

        dataHelper.close();
        loadBots();
    }

    private void showEditDialog(String name, String address, long id) {
        FragmentManager fm = getSupportFragmentManager();
        EditBotDialog editBotDialog = EditBotDialog.newInstance(name, address, id);
        editBotDialog.show(fm, "fragment_edit_bot");
    }

    private void loadBots() {
        HombotDataOpenHelper dataHelper = new HombotDataOpenHelper(this);
        final SQLiteDatabase db = dataHelper.getReadableDatabase();
        Cursor botCursor = db.query(HombotDataContract.BotEntry.TABLE_NAME, new String[]{HombotDataContract.BotEntry._ID, HombotDataContract.BotEntry.COLUMN_NAME_NAME, HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS}, null, new String[0], null, null, HombotDataContract.BotEntry.COLUMN_NAME_NAME);
        mBotAdapter.changeCursor(botCursor);
    }

    @Override
    public void onFinishEditDialog(String name, String address, long id) {
        HombotDataOpenHelper dataHelper = new HombotDataOpenHelper(this);
        SQLiteDatabase db = dataHelper.getWritableDatabase();

        if (address.indexOf(":") < 0) {
            address += ":6260";
        }

        ContentValues values = new ContentValues();
        values.put(HombotDataContract.BotEntry.COLUMN_NAME_NAME, name);
        values.put(HombotDataContract.BotEntry.COLUMN_NAME_ADDRESS, address);
        if (id > -1) {
            db.update(HombotDataContract.BotEntry.TABLE_NAME, values, HombotDataContract.BotEntry._ID + "=" + id, null);
        } else {
            db.insertOrThrow(HombotDataContract.BotEntry.TABLE_NAME, null, values);
        }

        dataHelper.close();
        loadBots();
    }

}
