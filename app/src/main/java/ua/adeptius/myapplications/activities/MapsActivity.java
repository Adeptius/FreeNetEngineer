package ua.adeptius.myapplications.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.util.MyInfoWindowAdapter;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Visual.MATCH_WRAP;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static List<Task> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    static LatLng getCoordinates(String adress) throws Exception {
        adress = adress.replaceAll(" ", "+");
        String json = getJsonFromUrl("https://maps.googleapis.com/maps/api/geocode/json?address=" +
                adress + "&key=AIzaSyDV6vNmBDLNuFDN7ZwIyVpWNB8LYjKRiXs");
//        System.out.println(json);

        json = json.substring(json.indexOf("\"location\""));
        json = json.substring(json.indexOf("{"));
        json = json.substring(0, json.indexOf("}") + 1);
        JSONObject jsonObject = new JSONObject(json);
        double lat = jsonObject.getDouble("lat");
        double lon = jsonObject.getDouble("lng");
        return new LatLng(lat, lon);
    }

    static String getJsonFromUrl(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = in.readLine();
        while (in.ready()) {
            result += in.readLine();
        }
        in.close();
        return result;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                drawMarkers(googleMap);
            }
        });
    }

    public Bitmap resizeMapIcons(int iconId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private HashMap<Marker, Task> markedTasks = new HashMap<>();
    private List<Task> errorDrawingTasks;

    private void drawMarkers(final GoogleMap googleMap) {
        errorDrawingTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            final int a = i;
            final Task task = tasks.get(i);
            try {
                final LatLng latLng = getCoordinates(task.getAddressForMap());
                final int iconId = Visual.getIconForMap(task.getType_name());

                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker arg0) {
                                // В список вставляем все таски по выбранному адресу
                                List<Task> tasks = findAllByAdress(arg0);
                                showTask(tasks);
                                return true;
                            }
                        });
                    }
                });

                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(task.getType_name())
                                .snippet(task.getRterm())
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconId, 100, 160)))
                        );
                        markedTasks.put(marker, task);

                        if (a == 0) {
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(13f));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        }
                    }
                });
            } catch (Exception e) {
                errorDrawingTasks.add(task);
            }
        }
        if (errorDrawingTasks.size()>0){
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String message = "Не удалось найти координаты:";
                    for (Task drawingTask : errorDrawingTasks) {
                        message += "\n"+drawingTask.getAddressForMap();
                    }
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private List<Task> findAllByAdress(Marker marker){
        List<Task> foundedTasks = new ArrayList<>();
        for (Map.Entry<Marker, Task> entry : markedTasks.entrySet()) {
            Marker entryMarker = entry.getKey();
            Task entryTask = entry.getValue();
            if (entryMarker.getPosition().latitude == marker.getPosition().latitude
                    && entryMarker.getPosition().longitude == marker.getPosition().longitude){
                foundedTasks.add(entryTask);
            }
        }
        return foundedTasks;
    }


    private void showTask(List<Task> task){
        ScrollView scrollView = new ScrollView(MapsActivity.this);
        LinearLayout layForTask = new LinearLayout(MapsActivity.this);
        layForTask.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layForTask);

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setView(scrollView);
        builder.setCancelable(true);

        for (int i = 0; i < task.size(); i++) {
            Task task1 = task.get(i);
            if (task.size()>1){
                layForTask.addView(getTaskView(task1, true));
            }else {
                layForTask.addView(getTaskView(task1, false));
            }
            if (task.size()>1 && (i+1) != task.size()){
                layForTask.addView(getHorizontalSeparator());
            }
        }
        builder.show();
    }

    View getHorizontalSeparator() {
        TextView justHorizontalSpace = new TextView(this);
        justHorizontalSpace.setTextSize(2);
        justHorizontalSpace.setLayoutParams(MATCH_WRAP);
        justHorizontalSpace.setPadding(0, 0, 8, 0);
        return justHorizontalSpace;
    }

    private LinearLayout getTaskView(final Task task, boolean commentInVisible){
        LinearLayout layout = new LinearLayout(MapsActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout header = Visual.getHeader(task, MapsActivity.this);
        header.setLayoutParams(MATCH_WRAP);
        layout.addView(header);

        final LinearLayout comentLayout = new LinearLayout(MapsActivity.this);
        comentLayout.setOrientation(LinearLayout.VERTICAL);
        if (commentInVisible){
            comentLayout.setVisibility(View.GONE);
        }

        String[] comments = task.getComments();
        for (int i = 0; i < comments.length; i++) {
            TextView commentView = new TextView(MapsActivity.this);
            commentView.setText(comments[i]);
            commentView.setPadding(15, 5, 10, 10);
            commentView.setTextColor(Color.WHITE);
            commentView.setBackgroundColor(Color.parseColor("#1e88e5"));
            if (i % 2 == 0) commentView.setBackgroundColor(Color.parseColor("#1565C0"));
            commentView.setLayoutParams(MATCH_WRAP);
            comentLayout.addView(commentView);
        }
        layout.addView(comentLayout);


        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comentLayout.getVisibility() == View.VISIBLE){
                    Intent intent = new Intent(MapsActivity.this, TaskActivity.class);
                    intent.putExtra("id", task.getId());
                    startActivity(intent);
                }else {
                    comentLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        comentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comentLayout.setVisibility(View.GONE);
            }
        });
        return layout;
    }
















    public static int expand(final View v) {
        v.setVisibility(View.VISIBLE);

        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        int duration = (int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density);
        a.setDuration(duration);
        v.startAnimation(a);
        return duration;
    }

    public static int collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 3dp/ms
        int duration = initialHeight / 3;
        a.setDuration(duration);

        v.startAnimation(a);
        return duration;
    }
}
