package com.wisconsin.ganz.eatwithfriends;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * {@link FriendListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendListFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public FriendListFragment() {
        // Required empty public constructor
    }

    // For progress dialog
    private ProgressDialog progressDialog;

    private FriendsFetchTask mFriendsTask = null;

    // View and Adapter Objects
    private ListView friendListView;
    private EventListCursorAdapter friendsAdapter;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FriendListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendListFragment newInstance() {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           // No Params
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_friend_list, container, false);

        setUpProgressDialog();
        getFriends();

        return view;
    }


    private void getFriends(){

        progressDialog.show();

        mFriendsTask = new FriendsFetchTask(HomeActivity.getUserPrefEmail());
        mFriendsTask.execute((Void) null);

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

    public void setUpProgressDialog(){
        // Set up the Progress Dialog object
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
    }

    private void parseStringAndPopulateList(String response){
        JSONObject result = null;
        JSONArray jsonArray = null;
        String[] columns = new String[] {"user_name", "user_email"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        try {
            result = new JSONObject(response);
            jsonArray =  result.getJSONArray("result");

            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject friend = jsonArray.getJSONObject(i);

                String userName = friend.getString("user_name");
                String userEmail = friend.getString("user_email");

                matrixCursor.addRow(new Object[]{i,
                                                userName,
                                                userEmail});
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (matrixCursor != null && matrixCursor.moveToFirst()) {
            friendsAdapter = new EventListCursorAdapter(getActivity(), matrixCursor, 0);
            friendListView.setAdapter(friendsAdapter);
        }
    }



    public class FriendsFetchTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;

        FriendsFetchTask(String email) {
            mEmail = email;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .authority(getString(R.string.host_name))
                        .path("users")
                        .appendQueryParameter("user_email", mEmail)
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
            if(!hasError(response)){
                parseStringAndPopulateList(response);
            }
            mFriendsTask = null;
            progressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mFriendsTask = null;
            progressDialog.dismiss();
        }

        /**
         * Checks if the server JSON response has any error
         * @param response
         * @return
         */
        protected boolean hasError(String response){
            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.has("error")) {
                    String errorMessage = "Not able to fetch events. " + jsonObject.getString("error");
                    Toast.makeText(getActivity().getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
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
