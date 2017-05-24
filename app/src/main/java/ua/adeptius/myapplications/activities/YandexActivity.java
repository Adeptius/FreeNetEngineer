package ua.adeptius.myapplications.activities;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;
import ru.yandex.yandexmapkit.overlay.OnOverlayItemListener;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Visual.MATCH_WRAP;
import static ua.adeptius.myapplications.util.Visual.getIconForMap;

public class YandexActivity extends AppCompatActivity {

    public static List<Task> tasks;
    MapView mapView;
    OverlayManager mOverlayManager;
    MapController mMapController;
    Overlay overlay;
    int iconHeigth;
    public static Double[] currentSityCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yandex);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mapView = (MapView) findViewById(R.id.map);
        mapView.showBuiltInScreenButtons(true);
        mapView.showScaleView(false);
        mMapController = mapView.getMapController();
        mMapController.setPositionNoAnimationTo(new GeoPoint(49.753286, 30.646107), 8);
        mOverlayManager = mMapController.getOverlayManager();
        mOverlayManager.getMyLocation().setEnabled(true);
        overlay = new Overlay(mMapController);
        mOverlayManager.addOverlay(overlay);
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                if (tasks.size() > 0) {
                    try{
                        String city = tasks.get(0).getCity();
                        Utilites.myLog("Ищу город " + city);
                        currentSityCoordinates = getCoordinates(city);
                        mMapController.setPositionNoAnimationTo(new GeoPoint(currentSityCoordinates[1], currentSityCoordinates[0]), 11);
                    } catch (Exception ignored) {
                    }
                }
                showObject();
            }
        });
    }

    private static HashMap<Task, Double[]> markedTasks;
    private List<Task> errorDrawingTasks;

    public void showObject() {
        markedTasks = new HashMap<>();
        errorDrawingTasks = new ArrayList<>();
        google = yandex = 0;
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        for (int i = 0; i < tasks.size(); i++) {
            final int finalI = i;
            EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    final Task task = tasks.get(finalI);
                    try {
                        String addressForMap = task.getAddressForMap();
                        System.out.println("Ищу адрес: " + addressForMap);
                        final Double[] coordinates = getCoordinates(addressForMap);
                        markedTasks.put(task, coordinates);
                        final int iconId = task.getSubject().equals("Юр") ?
                                R.drawable.map_vip : Visual.getIconForMap(task.getType_name());
                        final OverlayItem point = new OverlayItem(
                                new GeoPoint(coordinates[1], coordinates[0]),
                                new BitmapDrawable(getResources(), resizeMapIcons(iconId)));
                        point.setOffsetY((iconHeigth / 2) + 15);
                        point.setOverlayItemListener(new OnOverlayItemListener() {
                            public void onClick(OverlayItem clickItem) {
                                List<Task> tas = findAllByAdress(coordinates);
                                showTask(tas);
                            }
                        });
                        overlay.addOverlayItem(point);
                    } catch (Exception e) {
                        errorDrawingTasks.add(task);
                    } finally {
                        int completed = atomicInteger.incrementAndGet();
                        if (completed == tasks.size()){
                            proceedShowObjects();
                        }
                    }
                }
            });
        }
    }

    private void proceedShowObjects(){
        System.out.println("yandex " + yandex + " google " + google);
        setZoomSpan();
        if (errorDrawingTasks.size() > 0) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String message = "Не удалось найти координаты:";
                    for (Task drawingTask : errorDrawingTasks) {
                        message += "\n" + drawingTask.getAddressForMap();
                    }
                    System.out.println(message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private List<Task> findAllByAdress(Double[] marker) {
        List<Task> foundedTasks = new ArrayList<>();
        for (Map.Entry<Task, Double[]> entry : markedTasks.entrySet()) {
            Double[] entryMarker = entry.getValue();
            Task entryTask = entry.getKey();
            if (entryMarker[0].equals(marker[0]) && entryMarker[1].equals(marker[1])) {
                foundedTasks.add(entryTask);
            }
        }
        return foundedTasks;
    }


    static Double[] getCoordinates(String adress) throws Exception {
        adress = adress.replaceAll(" ", "+");
        URL url = new URL("https://geocode-maps.yandex.ru/1.x/?geocode=" + adress + "&format=json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = in.readLine();
        while (in.ready()) {
            result += in.readLine();
        }
        in.close();

        Double[] firstFoundedCoordinates = null; // сюда ложим первый результат.
        // Если яндекс найдёт не точно, а гугл вообще не найдёт - вернётся результат яндекса.

        JSONArray results = new JSONObject(result).getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember");
        int resultCount = results.length();
        for (int i = 0; i < resultCount; i++) {
            JSONObject obj = results.getJSONObject(i).getJSONObject("GeoObject");
            String country = obj.getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getJSONObject("Address").getString("country_code");
            if (country.equals("UA")) {
                String[] coor = obj.getJSONObject("Point").getString("pos").split(" ");
                Double[] coordinates = {Double.parseDouble(coor[0]), Double.parseDouble(coor[1])};
                if (distanceToPointFromCurrentCity(coordinates) > 100) {
                    continue; // если точка явно далеко
                }
                String precision = obj.getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getString("precision");
                if ("other".equals(precision)) {
                    if (firstFoundedCoordinates == null) {
                        firstFoundedCoordinates = coordinates;
                    }
                    continue;
                }

                yandex++;
                return coordinates;
            }
        }
        return getCoordinatesFromGoogle(adress, firstFoundedCoordinates);
    }

    public static int yandex = 0;
    public static int google = 0;


    static Double[] getCoordinatesFromGoogle(String adress, Double[] findedByYandex) throws Exception {
        adress = adress.replaceAll(" ", "+");
        URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" +
                adress + "&key=" + getRandomGoogleMapsApiKey());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = in.readLine();
        while (in.ready()) {
            result += in.readLine();
        }
        in.close();

        JSONArray results = new JSONObject(result).getJSONArray("results");
        int foundedCount = results.length();
        for (int i = 0; i < foundedCount; i++) {
            JSONObject obj = results.getJSONObject(i);
            JSONArray names = obj.getJSONArray("address_components");
            for (int j = 0; j < names.length(); j++) {
                if ("UA".equals(names.getJSONObject(j).getString("short_name"))) {
                    JSONObject jsonObjectWithCoordinates = obj.getJSONObject("geometry").getJSONObject("location");
                    double lat = jsonObjectWithCoordinates.getDouble("lng");
                    double lon = jsonObjectWithCoordinates.getDouble("lat");
                    Double[] coordinates = new Double[]{lat, lon};
                    if (distanceToPointFromCurrentCity(coordinates) > 100) {
                        continue; // если точка явно далеко
                    }
                    google++;
                    return coordinates;
                }
            }
        }

        if (findedByYandex != null) {
            return findedByYandex;
        }

        if (adress.startsWith("село+")) { // пробуем повторно найти хотябы село.
            String s = adress;
            int numberOfPlus = 0;
            while (s.contains("+")) { // защита от StackOverFlow
                numberOfPlus++;// считаем количество плюсов.
                s = s.substring(s.indexOf("+") + 1);// при повторном поиске ищем
            }
            if (numberOfPlus < 3) {//Если это повторный поиск то их будет меньше 3. село+Рожны или село+Червоная+Слобода
                throw new Exception(); // обрываем цикл
            }
            String newSearth = adress.replaceAll("село\\+", "");
            newSearth = newSearth.substring(0, newSearth.indexOf("+"));
            newSearth = "село+" + newSearth;
            return getCoordinates(newSearth);
        }

        if (adress.contains("Киев+Осокорки")){
            if (adress.equals("Киев+Осокорки")){
                throw new Exception(); // уже это и ищем. защита от StackOverFlow
            }
            return getCoordinates("Киев+Осокорки");
        }

        throw new Exception();
    }

    public static double distanceToPointFromCurrentCity(Double[] pointCoordinates) {
        if (currentSityCoordinates == null) {
            return 0;
        }
        double lon1 = pointCoordinates[0];
        double lat1 = pointCoordinates[1];
        double lon2 = currentSityCoordinates[0]; // долгота
        double lat2 = currentSityCoordinates[1]; // широта
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c * 6400;
    }

    public Bitmap resizeMapIcons(int iconId) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int neededWidht = screenWidth / 10;
        int height = iconHeigth = (int) (neededWidht * 1.6);
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, neededWidht, height, false);
        return resizedBitmap;
    }


    private void showTask(List<Task> task) {
        ScrollView scrollView = new ScrollView(YandexActivity.this);
        LinearLayout layForTask = new LinearLayout(YandexActivity.this);
        layForTask.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layForTask);

        AlertDialog.Builder builder = new AlertDialog.Builder(YandexActivity.this);
        builder.setView(scrollView);
        builder.setCancelable(true);

        for (int i = 0; i < task.size(); i++) {
            Task task1 = task.get(i);
            if (task.size() > 1) {
                layForTask.addView(getTaskView(task1, true));
            } else {
                layForTask.addView(getTaskView(task1, false));
            }
            if (task.size() > 1 && (i + 1) != task.size()) {
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

    private LinearLayout getTaskView(final Task task, boolean commentInVisible) {
        LinearLayout layout = new LinearLayout(YandexActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout header = Visual.getHeader(task, YandexActivity.this);
        header.setLayoutParams(MATCH_WRAP);
        layout.addView(header);

        final LinearLayout comentLayout = new LinearLayout(YandexActivity.this);
        comentLayout.setOrientation(LinearLayout.VERTICAL);
        if (commentInVisible) {
            comentLayout.setVisibility(View.GONE);
        }

        String[] comments = task.getComments();
        for (int i = 0; i < comments.length; i++) {
            TextView commentView = new TextView(YandexActivity.this);
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
                if (comentLayout.getVisibility() == View.VISIBLE) {
                    Intent intent = new Intent(YandexActivity.this, TaskActivity.class);
                    intent.putExtra("id", task.getId());
                    startActivity(intent);
                } else {
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

    private void setZoomSpan() {
        List<OverlayItem> list = overlay.getOverlayItems();
        double maxLat, minLat, maxLon, minLon;
        maxLat = maxLon = Double.MIN_VALUE;
        minLat = minLon = Double.MAX_VALUE;
        for (int i = 0; i < list.size(); i++) {
            GeoPoint geoPoint = list.get(i).getGeoPoint();
            double lat = geoPoint.getLat();
            double lon = geoPoint.getLon();

            maxLat = Math.max(lat, maxLat);
            minLat = Math.min(lat, minLat);
            maxLon = Math.max(lon, maxLon);
            minLon = Math.min(lon, minLon);
        }
        mMapController.setZoomToSpan(maxLat - minLat, maxLon - minLon);
        mMapController.setPositionNoAnimationTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
    }

    public static String getRandomGoogleMapsApiKey() {
        int random = (int) (Math.random() * 5);
        return new String[]{
                "AIzaSyAfLdsVj3h5aMuZM_aTU2AO64DwrYABEI8",
                "AIzaSyB2E9-K7FA2Km7MDvm8oh2S4RcNDODM9oE",
                "AIzaSyA5l3QVCBtiYQ_ZQtHCbsTUUTCjydUjj-o",
                "AIzaSyDPPBPj-DcFapVMFfaXrNMrlviZ9IB93RI",
                "AIzaSyDvvmUytkVGWaNO1JGNfjMPDGGKIaPj0P0"}[random];
    }
}
