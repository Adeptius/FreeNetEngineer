package ua.adeptius.myapplications.connection;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ua.adeptius.myapplications.activities.LoginActivity;


public class Network {

    public static boolean isAuthorizationOk(String login, String password){
        try {
            //Log.d(TAG, "Проверяем авторизацию");
            String[] request = new String[3];
            request[0] = "http://188.231.188.188/api/task_api_aut.php";
            request[1] = "begun=" + login;
            request[2] = "drowssap=" + password;
            Map<String, String> map = new DataBase().execute(request).get().get(0);
            if (map.get("authentication").equals("success")) return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Загрузка новой версии c прогресс-диалогом
     */
    public static void downloadFile(String url,  Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        final LoginActivity loginActivity = (LoginActivity) context;

        new AsyncTask<String, Integer, File>() {
            private Exception m_error = null;

            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("Загрузка...");
                progressDialog.setCancelable(false);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
            }

            @Override
            protected File doInBackground(String... params) {
                URL url;
                HttpURLConnection urlConnection;
                InputStream inputStream;
                int totalSize;
                int downloadedSize;
                byte[] buffer;
                int bufferLength;

                File file = null;
                FileOutputStream fos = null;

                try {
                    url = new URL(params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    String fileName = params[0].substring(params[0].lastIndexOf("/") + 1);
                    file = new File("/sdcard/Download/" + fileName);
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                    inputStream = urlConnection.getInputStream();
                    totalSize = urlConnection.getContentLength();
                    downloadedSize = 0;

                    buffer = new byte[1024];

                    // читаем со входа и пишем в выход,
                    // с каждой итерацией публикуем прогресс
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        publishProgress(downloadedSize, totalSize);
                    }
                    fos.close();
                    inputStream.close();

                    return file;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    m_error = e;
                } catch (IOException e) {
                    e.printStackTrace();
                    m_error = e;
                }
                return null;
            }

            // обновляем progressDialog
            protected void onProgressUpdate(Integer... values) {
                progressDialog.setProgress((int) ((values[0] / (float) values[1]) * 100));
            }

            @Override
            protected void onPostExecute(File file) {
                // отображаем сообщение, если возникла ошибка
                if (m_error != null) {
                    m_error.printStackTrace();
                    return;
                }
                progressDialog.hide();
                loginActivity.finish();
            }
        }.execute(url);
    }
}
