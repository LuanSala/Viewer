
package lsa.viewercloudpoints.navigation_drawer;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import lsa.viewercloudpoints.R;

/**
 * Created by Luan Sala on 08/08/2015.
 */
public class MovementSpeedPreference extends DialogPreference {
    private static final String TAG = "MovementSpeedPreference";

    private SeekBar seekBar;
    private int mValue;
    private final int mMaxValue;

    public MovementSpeedPreference(Context context, AttributeSet attrs){
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_mov_speed);
        setPositiveButtonText("Ok");
        setNegativeButtonText("Cancel");
        setDialogIcon(null);

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.MovementSpeedPreference);
        mMaxValue = a.getInt(R.styleable.MovementSpeedPreference_maxValue,100);
        a.recycle();
    }

    public int getMaxValue(){
        return mMaxValue;
    }

    /**
     * Returns the value of the key.
     *
     * @return The value of the key.
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Sets the value of the key.
     *
     * @param value The value to set for the key.
     */
    public void setValue(int value) {
        // Always persist/notify the first time.
        final boolean changed = (mValue!=value);
        if (changed) {
            mValue = value;
            persistInt(value);
            //if (changed) {
                notifyChanged();
            //}
        }
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        seekBar = ((SeekBar)view.findViewById(R.id.seekBar_MovSpeed));
        seekBar.setMax(mMaxValue-1);
        seekBar.setProgress(mValue);
        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int value = seekBar.getProgress();
            if (callChangeListener(value+1)) {
                setValue(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index,mMaxValue);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mValue) : (int)defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value);
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}

