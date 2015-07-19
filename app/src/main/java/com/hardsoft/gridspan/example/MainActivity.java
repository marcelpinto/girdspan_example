package com.hardsoft.gridspan.example;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridSpanTextView tagsView = (GridSpanTextView) findViewById(R.id.filter_span_tags);
        ArrayList<Tag> tags = getHardCodeTags();
        tagsView.setTagsList(tags);
    }

    private ArrayList<Tag> getHardCodeTags() {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("1", "Vegetarish"));
        tags.add(new Tag("2", "Vegan"));
        tags.add(new Tag("3", "Glutenfrei"));
        tags.add(new Tag("4", "Meat"));
        tags.add(new Tag("5", "Drinks"));
        tags.add(new Tag("6", "Dessert"));
        tags.add(new Tag("7", "Beer"));
        tags.add(new Tag("8", "Tag with spaces"));
        tags.add(new Tag("9", "Short"));
        tags.add(new Tag("10", "<b>Html Tag</b>"));
        tags.add(new Tag("11", "<i>Italic</i>"));
        return tags;
    }
}
