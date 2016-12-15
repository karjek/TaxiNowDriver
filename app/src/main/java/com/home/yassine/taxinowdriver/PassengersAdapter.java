package com.home.yassine.taxinowdriver;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Yassine on 16/09/2016.
 */
public class PassengersAdapter extends ArrayAdapter<Passenger> {

    public interface IDeletePassenger {
        void OnDeletePassenger(Passenger p);
    }

    public PassengersAdapter(Context context, List<Passenger> objects, IDeletePassenger onDeletePassenger) {
        super(context, 0, objects);
        this.deletePassengerCallback = onDeletePassenger;
    }

    private HashMap<Integer, Drawable> drawableCache = new HashMap<>();
    private IDeletePassenger deletePassengerCallback;

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        Passenger item = getItem(i);

        final ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.passenger_avatar_item, parent, false);

            viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.p);
            viewHolder.avatar = (TextView) convertView.findViewById(R.id.passenger);
            viewHolder.trash = (TextView) convertView.findViewById(R.id.trash);
            viewHolder.location = (TextView) convertView.findViewById(R.id.show_location);
            viewHolder.count = (Button) convertView.findViewById(R.id.count);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TextView avatar = viewHolder.avatar;

        if (item.type == Passenger.PassengerType.CLIENT)
        {
            final int resourceId = getContext().getResources().getIdentifier(item.iconName, "drawable",
                    getContext().getPackageName());
            avatar.setBackground(getDrawable(resourceId));

            viewHolder.location.setVisibility(View.VISIBLE);

            if (item.count > 1) {
                viewHolder.count.setText(String.valueOf(item.count));
                viewHolder.count.setVisibility(View.VISIBLE);
            }
        }
        else {
            if (item.gender.equals("F"))
                avatar.setBackground(getDrawable(R.drawable.girl_left));
            else
                avatar.setBackground(getDrawable(R.drawable.boy_left));
        }

        final int shortAnimTime = getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);

        viewHolder.layout.animate().alpha(1).translationX(20).setDuration(shortAnimTime);

        viewHolder.layout.setOnTouchListener(new PassengersAdapter.MyTouchListener(convertView, viewHolder.trash, item, viewHolder.location, new MyTouchListener.IDeleteCallback() {
            @Override
            public void OnDelete(View v, Passenger p) {
                viewHolder.layout.setOnTouchListener(null);
                deletePassengerCallback.OnDeletePassenger(p);
            }
        }));

        return convertView;
    }

    private static class ViewHolder {
        TextView avatar;
        RelativeLayout layout;
        TextView trash;
        TextView location;
        Button count;
    }

    public Drawable getDrawable(int drawableId) {

        if (drawableCache.containsKey(drawableId))
            return drawableCache.get(drawableId);

        Drawable d = getContext().getResources().getDrawable(drawableId);
        drawableCache.put(drawableId, d);
        return d;
    }

    public static class MyTouchListener implements View.OnTouchListener {

        private final View mParent;
        private final TextView mTrash;
        private final TextView mLocation;
        private final IDeleteCallback mDeleteCallback;
        private final Passenger mPassenger;

        float dX, dY;
        private boolean mDeleted = false;
        private float mFirstPositionX;

        public interface IDeleteCallback {
            void OnDelete(View v, Passenger p);
        }

        public MyTouchListener(View parent, TextView trashView, Passenger passenger, TextView loc, IDeleteCallback deleteCallback) {
            mParent = parent;
            mTrash = trashView;
            mLocation = loc;
            mDeleteCallback = deleteCallback;
            mPassenger = passenger;
            mFirstPositionX = -1;
        }

        public boolean onTouch(View view, MotionEvent event) {

            if (mDeleted) {
                return false;
            }

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();

                    if (mFirstPositionX == -1) {
                        mFirstPositionX = view.getX();
                    }

                    break;

                case MotionEvent.ACTION_MOVE:

                    if (event.getRawX() + dX < mParent.getX() ||
                            event.getRawX() + dX > mParent.getX() + mParent.getWidth() - view.getWidth())
                        return false;

                    view.getParent().requestDisallowInterceptTouchEvent(true);

                    if (mLocation.getVisibility() != View.GONE) {
                        mLocation.setVisibility(View.INVISIBLE);
                    }

                    view.animate()
                            .x(event.getRawX() + dX)
                            .setDuration(0)
                            .start();

                    if (mTrash.getVisibility() == View.GONE || mTrash.getAlpha() == 0) {
                        mTrash.setVisibility(View.VISIBLE);
                        mTrash.setAlpha(1);
                    }

                    if (view.getX() + view.getWidth()/3 > mTrash.getX()) {
                        view.setScaleX(.8f);
                        view.setScaleY(.8f);
                    }
                    else {
                        view.setScaleX(1);
                        view.setScaleY(1);
                    }

                    break;
                case MotionEvent.ACTION_UP:

                    if (view.getX() + (view.getWidth()/2.5) > mTrash.getX()) {
                        mDeleted = true;
                        view.setScaleX(1);
                        view.setScaleY(1);
                        mTrash.setVisibility(View.GONE);
                        mTrash.setAlpha(0);
                        mDeleteCallback.OnDelete(view, mPassenger);
                    }
                    else
                    {
                        mTrash.setAlpha(0);
                        mTrash.setVisibility(View.GONE);

                        if (mLocation.getVisibility() == View.INVISIBLE) {
                            mLocation.setVisibility(View.VISIBLE);
                        }

                        view.animate().x(mFirstPositionX).setDuration(200);
                    }

                    return false;
                default:
                    return false;
            }
            return true;
        }
    }
}
