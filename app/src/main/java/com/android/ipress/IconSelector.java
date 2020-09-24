package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class IconSelector extends AppCompatActivity {

    GridView mGridView;
    List<String> mUrlList = new ArrayList<>();
    IconAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_selector);
        updateGridView();
    }

    private void updateGridView() {
        mGridView = findViewById(R.id.SampleGridView);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GlobalClass.iconUrl = mUrlList.get(position);
                finish();
                overridePendingTransition(0,0);
            }
        });
        FirebaseDatabase.getInstance().getReference("icons").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUrlList.clear();
                for (DataSnapshot iconSnapshot : snapshot.getChildren()) {
                    mUrlList.add(iconSnapshot.child("iconUrl").getValue().toString());
                }
                mAdapter = new IconAdapter(IconSelector.this, mUrlList);
                mGridView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class IconAdapter extends BaseAdapter {

        Context mContext;
        List<String> mList;

        public IconAdapter(Context mContext, List<String> list) {
            this.mContext = mContext;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View grid;
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            String url = mList.get(position);
            if (convertView == null) {
                grid = layoutInflater.inflate(R.layout.icon_options_item, null);
                ImageView Icon = grid.findViewById(R.id.Icon);
                Picasso.with(IconSelector.this)
                        .load(url)
                        .placeholder(R.drawable.image_icon)
                        .into(Icon);
            } else {
                grid = convertView;
            }
            return grid;
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "No icon selected", Toast.LENGTH_SHORT).show();
    }
}