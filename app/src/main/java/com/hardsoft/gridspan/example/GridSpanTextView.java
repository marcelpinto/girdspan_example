package com.hardsoft.gridspan.example;

/*
 * Copyright (c) 2015 Express Quality Food Global Service GmbH. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This view extends TextView and convert an array list of Strings to a grid formed by clickable
 * tags for each string in the array. You can specify the color also if it's a checkable grid or
 * just clickable.
 * Class to fix the span errors
 * Created by marcel on 18/05/15.
 */
public class GridSpanTextView extends TextView {

    private int tagViewId;
    private ArrayList<Tag> checkedTags;
    private int checkedColor = Color.BLUE;

    public GridSpanTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GridSpanTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridSpanTextView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(getText().toString());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void setGravity(int gravity) {
        try {
            super.setGravity(gravity);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(getText().toString());
            super.setGravity(gravity);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Workaround to fix some problems with spans and textview
            setText(text.toString());
        }
    }

    public void setTagView(@LayoutRes int viewId) {
        this.tagViewId = viewId;
    }

    public ArrayList<Tag> getCheckedTags() {
        return checkedTags;
    }

    public void setTagsList(ArrayList<Tag> tagsList) {
        checkedTags = new ArrayList<Tag>();
        final SpannableStringBuilder sb = new SpannableStringBuilder();
        for (Tag tag : tagsList) {
            String txt = tag.getTitle();
            TextView tv = createTagTextView(txt);
            Drawable bd = convertViewToDrawable(tv);
            bd.setBounds(0, 0, bd.getIntrinsicWidth() + 5, bd.getIntrinsicHeight());
            sb.append(txt).append(" ");
            sb.setSpan(new ImageSpan(bd), sb.length() - (txt.length() + 1), sb.length() - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new TouchableSpan(checkedColor, tag), sb.length() - (txt.length() + 1),
                    sb.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setMovementMethod(LinkTouchMovementMethod.getInstance());
        setText(sb, TextView.BufferType.SPANNABLE);
    }

    private TextView createTagTextView(String text) {
        TextView tv;
        if (tagViewId == 0) {
            tv = new TextView(getContext());
            tv.setPadding(15, 10, 15, 10);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize());
            tv.setTextColor(getResources().getColor(R.color.abc_primary_text_material_light));
        } else {
            View view = LayoutInflater.from(getContext()).inflate(tagViewId, null);
            if (!(view instanceof TextView)) {
                throw new IllegalArgumentException("Only support TextView as root for now");
            }
            tv = (TextView) view;
        }
        tv.setText(Html.fromHtml(text != null ? text : ""));
        return tv;
    }

    private Drawable convertViewToDrawable(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable(getResources(), viewBmp);
    }

    private class TouchableSpan extends ClickableSpan {
        private final Tag tag;
        private boolean isPressed;
        private int pressedBackgroundColor;

        public TouchableSpan(int pressedBackgroundColor, Tag tag) {
            this.pressedBackgroundColor = pressedBackgroundColor;
            this.tag = tag;
        }

        public void setPressed(boolean isSelected) {
            isPressed = isSelected;
        }

        @Override
        public void onClick(View widget) {
            TextView tv = (TextView) widget;
            Spanned s = (Spanned) tv.getText();
            int start = s.getSpanStart(this);
            int end = s.getSpanEnd(this);
            boolean existed = checkedTags.remove(tag);
            SpannableStringBuilder builder = new SpannableStringBuilder(s);
            ImageSpan[] imgSpan = builder.getSpans(start, end, ImageSpan.class);
            if (imgSpan != null && imgSpan.length > 0) {
                if (existed) {
                    imgSpan[0].getDrawable().setColorFilter(null);
                } else {
                    checkedTags.add(tag);
                    imgSpan[0].getDrawable().setColorFilter(checkedColor, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.bgColor = isPressed ? pressedBackgroundColor : 0xffeeeeee;
            ds.setUnderlineText(false);
        }
    }

    private static class LinkTouchMovementMethod extends LinkMovementMethod {
        private static LinkTouchMovementMethod instance;
        private TouchableSpan mPressedSpan;

        public static LinkTouchMovementMethod getInstance() {
            if (instance == null) {
                instance = new LinkTouchMovementMethod();
            }
            return instance;
        }

        @Override
        public boolean onTouchEvent(@NonNull TextView textView, @NonNull Spannable spannable,
                                    @NonNull MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPressedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(true);
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan));
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                    mPressedSpan.setPressed(false);
                    mPressedSpan = null;
                    Selection.removeSelection(spannable);
                }
            } else {
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(false);
                    super.onTouchEvent(textView, spannable, event);
                }
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
            return true;
        }

        private TouchableSpan getPressedSpan(TextView textView, Spannable spannable,
                                             MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);
            TouchableSpan touchedSpan = null;
            if (link.length > 0) {
                touchedSpan = link[0];
            }
            return touchedSpan;
        }
    }
}