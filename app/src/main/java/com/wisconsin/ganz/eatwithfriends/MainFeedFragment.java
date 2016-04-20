package com.wisconsin.ganz.eatwithfriends;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFeedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EventsFetchTask mEventsTask = null;

    // View and Adapter Objects
    private ListView eventListView;
    private EventListCursorAdapter eventAdapter;

    public MainFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFeedFragment newInstance(String param1, String param2) {
        MainFeedFragment fragment = new MainFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            Log.i("Frag", "Inside Fragment Create!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_feed, container, false);
        eventListView = (ListView) view.findViewById(R.id.event_list);

        populateEventList();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Get events from the REST API or from the SQLite Cache
     */
    private Cursor getEvents(){
        // Temporarily use
        String[] columns = new String[] {"_id", "fieldA", "fieldB"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        matrixCursor.addRow(new Object[] {1, "ItemA1", "ItemA2"});
        matrixCursor.addRow(new Object[] {2, "ItemB1", "ItemB2"});
        matrixCursor.addRow(new Object[] {3, "ItemC1", "ItemC2"});

        return matrixCursor;
    }

    /**
     * Populate the Event List Adapter
     *
     */
    private void populateEventList(){
        Cursor cursor = getEvents();

        if (cursor != null && cursor.moveToFirst()) {
            eventAdapter = new EventListCursorAdapter(getActivity(), cursor, 0);
            eventListView.setAdapter(eventAdapter);
        }
    }

    /**
     * Represents an asynchronous event fetching task used to pull events from
     * the server.
     */
    public class EventsFetchTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mRowCount;

        EventsFetchTask(String email, String rowCount) {
            mEmail = email;
            mRowCount = rowCount;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .authority(getString(R.string.host_name))
                        .path("events")
                        .appendQueryParameter("user_email", mEmail)
                        .appendQueryParameter("row_count", mRowCount)
                        .build();

                URL url = new URL(uri.toString());
                Log.w("URI", uri.toString());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(20000);
                urlConnection.setReadTimeout(20000);

                if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 201) {
                    BufferedReader br = new BufferedReader(new InputStreamReader
                            (urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.w("URL", "Response:" + sb.toString());
                    return sb.toString();
                }
            } catch (MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // If response code is not 2xx or if something else wen't wrong.
            return "{\"error\":\"Something wen't wrong. Try again.\"}";
        }

        @Override
        protected void onPostExecute(final String response) {
            mEventsTask = null;

        }

        @Override
        protected void onCancelled() {
            mEventsTask = null;
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
