package com.katepka.guessflag;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private String HOME_URL = "https://www.sciencekids.co.nz/pictures/flags.html";
    private String BASE_URL = "https://www.sciencekids.co.nz";
    private List<String> countries;
    private List<String> imageUrls;
    private List<Bitmap> images;

    private ImageView imageView;
    private Button button0;
    private Button button1;
    private Button button2;
    private Button button3;
    private ArrayList<Button> buttons;

    private int numberOfQuestion;
    private int numberOfRightAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        countries = new ArrayList<>();
        imageUrls = new ArrayList<>();
        images = new ArrayList<>();
        buttons = new ArrayList<>();
        buttons.add(button0);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);

        getContent();
        playGame();

    }

    private void getContent() {
        DownloadContentTask downloadContentTask = new DownloadContentTask();
        try {
            String content = downloadContentTask.execute(HOME_URL).get();
            Log.i("content", content);
            String splitContent = splitContent(content);
            countries = getListOfCountries(splitContent);
            imageUrls = getListOfFlagsUrls(splitContent);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onClickAnswer(View view) {
        Button button = (Button) view;
        String tag = String.valueOf(button.getTag());
        if (Integer.parseInt(tag) == numberOfRightAnswer) {
            Toast.makeText(this, "Right!", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Wrong! Right answer is "
                    + countries.get(numberOfRightAnswer), Toast.LENGTH_SHORT).show();
        }
        playGame();
    }

    private void playGame() {
        generateQuestion();
        DownloadImageTask task = new DownloadImageTask();
        try {
            Bitmap bitmap = task.execute(imageUrls.get(numberOfRightAnswer)).get();
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                for (int i = 0; i < buttons.size(); i++) {
                    if (i == numberOfRightAnswer) {
                        buttons.get(i).setText(countries.get(numberOfRightAnswer));
                    } else {
                        int num = generateWrongAnswer();
                        buttons.get(i).setText(countries.get(num));
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generateQuestion() {
        Random random = new Random();
        numberOfQuestion = random.nextInt(countries.size());
        Random random2 = new Random();
        numberOfRightAnswer = random2.nextInt(buttons.size());
    }

    private int generateWrongAnswer() {
        Random random = new Random();
        return random.nextInt(countries.size());
    }

    private String splitContent(String content) {
        String splitContent = "";
        String start = "<td width=\"240\"><img src=\"../images/220logo3.jpg\" alt=\"Science for Kids\" width=\"217\" height=\"90\" vspace=\"14\" align=\"right\" /></td>";
        String finish = "<td width=\"240\"><div align=\"left\"> <img src=\"../images/220logo5.jpg\" alt=\"Fun Science and Technology for Kids\" width=\"217\" height=\"90\" align=\"left\" /></div></td>";
        Pattern patter = Pattern.compile(start + "(.*?)" + finish);
        Matcher matcher = patter.matcher(content);
        while (matcher.find()) {
            splitContent = matcher.group(1);
        }
        return splitContent;
    }

    private List<String> getListOfCountries(String content) {
        List<String> countries = new ArrayList<>();
        Pattern patternName = Pattern.compile("alt=\"Flag of (.*?)\"");
        Matcher matcherName = patternName.matcher(content);
        while (matcherName.find()) {
            countries.add(matcherName.group(1));
        }
        return countries;
    }

    private List<String> getListOfFlagsUrls(String content) {
        List<String> imageUrls = new ArrayList<>();
        Pattern patternImage = Pattern.compile("<img src=\"\\.\\.(.*?)\"");
        Matcher matcherImage = patternImage.matcher(content);
        while (matcherImage.find()) {
            imageUrls.add(BASE_URL + matcherImage.group(1));
        }
        return imageUrls;
    }

    private static class DownloadContentTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder sb = new StringBuilder();

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    sb.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return sb.toString();
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }


}
