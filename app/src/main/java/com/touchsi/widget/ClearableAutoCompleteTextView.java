/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 21/2/2013
 * Time: 12:04
 */

package com.touchsi.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class ClearableAutoCompleteTextView extends AutoCompleteTextView implements View.OnTouchListener,
        View.OnFocusChangeListener {

    public ClearableAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.l = l;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener f) {
        this.f = f;
    }

    private OnTouchListener l;
    private OnFocusChangeListener f;
    private Drawable xD;
    private Listener listener;


    private void init() {
        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setClearShown(s != null && s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setClearShown(boolean shown) {
        Drawable x = shown ? xD : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
    }

    public void setClearDrawable(int drawableResId) {
        xD = getResources().getDrawable(drawableResId);
        xD.setBounds(0, 0, 45, 45);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearShown(getText() != null && getText().toString().trim().length() > 0);
        } else {
            setClearShown(false);
        }
        if (f != null) {
            f.onFocusChange(v, hasFocus);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean tappedX = event.getX() > (getWidth()
                        - getPaddingRight() - xD.getIntrinsicWidth());
                if (tappedX) {
                    setText("");
                    if (listener != null) {
                        listener.didClearEditText();
                    }
                    return true;
                }
            }
        }
        if (l != null) {
            return l.onTouch(v, event);
        }
        return false;
    }

    public interface Listener {
        public void didClearEditText();
    }

}
