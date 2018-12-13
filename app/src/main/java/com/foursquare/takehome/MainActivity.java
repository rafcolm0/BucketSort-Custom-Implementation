package com.foursquare.takehome;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRecyclerView;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvRecyclerView = findViewById(R.id.rvRecyclerView);
//        personAdapter = new PersonAdapter();  //for simplicity, we are initializing the adapter below directly after we have the data set

        //TODO hook up your adapter and any additional logic here
        loading = findViewById(R.id.loading);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvRecyclerView.setLayoutManager(mLayoutManager);
        //for this case, the loading will barely show up but for real scenarios, something should be shown to indicate the UI thread is waiting
        showLoading();
        new VenueFetcher(this).execute();
    }

    void showLoading(){
        loading.animate();
        rvRecyclerView.setVisibility(View.GONE);
    }

    void hideLoading(PersonAdapter adapter){
        if(adapter != null){  //this should never failed but for safety, an else would be put in place
            rvRecyclerView.setAdapter(adapter);
        }
        loading.setVisibility(View.GONE);
        rvRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Fakes a data fetch and parses json from assets/people.json
     */
    private static class VenueFetcher extends AsyncTask<Void, Void, Venue> {
        private final WeakReference<MainActivity> activityWeakReference;

        public VenueFetcher(MainActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Venue doInBackground(Void... params) {
            return activityWeakReference != null ? VenueStore.get().getVenue(activityWeakReference.get()) : null;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            if (activityWeakReference == null || venue == null) {
                return;
            }

            MainActivity mainActivity = activityWeakReference.get();
            //TODO use the venue object to populate your recyclerview
            /*
              NOTES:
              * Algorithm below imitates a Bucket Sort ordering procedure, where Person objects with the same ARRIVING TIME are contained in hash tables, where each hash table's key is also contained in an ordered Priority Queue.  In our case, as we do not need to sort the Person objects contained in the time slot (e.g. Person objects with the same ARRIVE TIME), we would never reach the standard worst case scenario for Bucket Sort of O(n^2).

              ASSUMPTIONS:
              1) We selected this approach due to fact it would be expected for many Person objects to have the same ARRIVE TIME in a real-case scenario.
              2) The LEAVE TIME ordering (inner priority queues) is used to instantly determine the maximum LEAVE TIME of all the Person objects with the same ARRIVE TIME.  Then, this maximum LEAVE TIME would be used as the starting time for a potential No Visitors time slot proceeding listing the Person objects with the same ARRIVE TIME.
              3) By using Java's built-in PriorityQueue, we ensure expected running times for all native procedures (inserts, remove, search, etc.). Individual running times for these are listed below.

              POTENTIAL CONS (trade-offs):
              1)  Data structure memory overhead: worst case scenario, the approach below would require n single-item HashMaps (time slots) and n PriorityQueue keys (1 per time slot). This can become an issue when dealing with thousands of items and if the assumption that many Person objects would have the same ARRIVE TIME is false.
              2) Time comparisons are based on exact time value. If arrive times were not rounded in real time, then some sort of time range comparision or hash function would be needed to make time slots distribution more even.
              *
             */
            List<Person> visitors = venue.getVisitors();
            @SuppressLint("UseSparseArrays") HashMap<Long, Pair<Long, ArrayList<Person>>> timeSlots = new HashMap<>(); //hashmaps are O(1)
            PriorityQueue<Long> sortedKeys = new PriorityQueue<>(visitors.size(), new Comparator<Long>() {
                @Override
                public int compare(Long o1, Long o2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return Long.compare(o1, o2);
                    }
                    return o1.compareTo(o2);
                }
            });
            //PART 1: total running time = O(n log n)
            for(Person person : visitors){  //loop iterates through n Person objects
                Pair<Long, ArrayList<Person>> slot = timeSlots.get(person.getArriveTime());
                if(slot == null){ //no bucket for current arrive time found: create bucket
                    sortedKeys.add(person.getArriveTime());  //add for PriorityQueue is O(log n)
                    ArrayList<Person> temp = new ArrayList<>();
                    temp.add(person);
                    //insert time = O(log n)
                    timeSlots.put(person.getArriveTime(), Pair.create(person.getLeaveTime(), temp));
                } else {  //bucket for current arrive time found: append Person object
                    ArrayList<Person> temp = slot.second;
                    temp.add(person);
                    if(person.getLeaveTime() > slot.first){   //new max LEAVE TIME found, update bucket with new max
                        timeSlots.put(person.getArriveTime(), Pair.create(person.getLeaveTime(), temp));  //insert time = O(log n)
                    } else {  //update bucket using old max LEAVE TIME
                        timeSlots.put(person.getArriveTime(), Pair.create(slot.first, temp));  //insert time = O(log n)
                    }
                }
            }

            //PART 2: worst case T(n) = O(n log n) -> (1 time slot per person)
            long indexTime = venue.getOpenTime();
            List<Person> adaptedList = new ArrayList<>();
            while(!sortedKeys.isEmpty()){  //loop iterates through all time slots/buckets -> O(k), where k == number of time slots
                Long key = sortedKeys.poll();  //remove time = O(log n)
                Pair<Long, ArrayList<Person>> entry = timeSlots.get(key);
                if(entry != null && key != null) {  //this should never be true, just for safety
                    if (key > indexTime) {  //gap found between current ARRIVE TIME and previous LEAVE TIME: create empty time slot
                        adaptedList.add(new Person(-1, "No Visitors", indexTime, key));
                    }
                    if(indexTime < entry.first) {
                        indexTime = entry.first;
                    }
                    adaptedList.addAll(entry.second);
                }
            }

            //TOTAL RUNNING TIME (PART 1 & 2): T(n) =  O(2(n log n) + n) < O(n^2) -> WRONG, as submitted
            //TOTAL RUNNING TIME (PART 1 & 2): T(n) =  O(2(n log n) + z), where z == final number of Person objects listed by the adapter
            mainActivity.hideLoading(new PersonAdapter(adaptedList));
        }
    }
}
