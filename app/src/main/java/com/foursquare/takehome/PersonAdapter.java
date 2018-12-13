package com.foursquare.takehome;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public final class PersonAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final List<Person> timeSlots;
    private final SimpleDateFormat df;

    public PersonAdapter(List<Person> timeSlots) {
        this.timeSlots = timeSlots;
        df = new SimpleDateFormat("h:mm a", Locale.US);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_view_holder, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Person person = this.timeSlots.get(position);
        ((PersonViewHolder)holder).tv_person_name.setText(person.getName());
        ((PersonViewHolder)holder).tv_person_times.setText(getPersonTimesRange(person));
    }

    public String getPersonTimesRange(Person person) {
        return df.format(new Date(person.getArriveTime())) + " - " + df.format(new Date(person.getLeaveTime()));
    }

    @Override
    public int getItemCount() {
        return this.timeSlots.size();
    }


    private class PersonViewHolder extends ViewHolder {
        final TextView tv_person_name;
        final TextView tv_person_times;

        PersonViewHolder(View view) {
            super(view);
            tv_person_name = view.findViewById(R.id.tv_person_name);
            tv_person_times = view.findViewById(R.id.tv_person_times);
        }
    }
}
