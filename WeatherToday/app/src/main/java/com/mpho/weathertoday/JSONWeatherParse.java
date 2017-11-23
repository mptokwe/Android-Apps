package com.mpho.weathertoday;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONWeatherParse extends AsyncTask<Void, Void, String> {

    private TextView txtv_temperature,txtv_max_temp,txtv_min_temp,txtv_humidity,txtv_place;
    private ImageView imv_weather_icon;
    private Double lattitude, longitude;
    private Bitmap weather_icon_image;

    public JSONWeatherParse(Double lat, Double lon, ImageView imv_weather_icon, TextView... params){

        this.lattitude=lat;
        this.longitude=lon;
        this.imv_weather_icon= this.imv_weather_icon;
        //this.txtv_today_date=params[0];
        this.txtv_temperature=params[0];
        this.txtv_max_temp=params[1];
        this.txtv_min_temp=params[2];
        this.txtv_humidity=params[3];
        this.txtv_place=params[4];
    }

    private static String getString(String tag_name,JSONObject j_obj) throws JSONException{
        return j_obj.getString(tag_name);
    }
    private JSONObject getObject(String tag_name, JSONObject j_obj) throws JSONException{
        JSONObject sub_obj=j_obj.getJSONObject(tag_name);
        return sub_obj;
    }
    private static float getFloat(String tag_name, JSONObject j_obj) throws JSONException{
        return (float) j_obj.getDouble(tag_name);
    }
    private static int getInt(String tag_name, JSONObject j_obj) throws JSONException{
        return j_obj.getInt(tag_name);
    }

    public  CurrentWeatherDetails getWeatherinfo(String responsedata) throws JSONException{

        //location information object
        UserLocationInformation location_info=new UserLocationInformation();
        //contains detailed weather information
        CurrentWeatherDetails weather_det=new CurrentWeatherDetails();
        //creates JSONObject from response object
        JSONObject j_obj=new JSONObject(responsedata);
        //extract location coordinates info
        JSONObject coord_obj=getObject("coord",j_obj);
        location_info.setLatitude(getFloat("lat",coord_obj));
        location_info.setLongitude(getFloat("lon",coord_obj));

        //extract location details
        JSONObject sys_obj=getObject("sys",j_obj);
        location_info.setCountry(getString("country",sys_obj));
        location_info.setCity(getString("name",j_obj));

        //initialise user_location with location info
        weather_det.setUser_location(location_info);

        //extract weather info using only first value
        JSONArray j_arr=j_obj.getJSONArray("weather");
        JSONObject jw_obj=j_arr.getJSONObject(0);
        weather_det.setCurrent_weather_id(getInt("id",jw_obj));
        weather_det.setDescription(getString("description",jw_obj));
        weather_det.setCondition(getString("main",jw_obj));

        //get icon info to facilitate icon download
        weather_det.setIcon(getString("icon",jw_obj));

        JSONObject main_obj=getObject("main",j_obj);
        weather_det.setHumidity(getInt("humidity",main_obj));
        weather_det.setPressure(getInt("pressure",main_obj));
        weather_det.setMax_temp(getFloat("temp_min",main_obj));
        weather_det.setMin_temp(getFloat("temp_min",main_obj));
        weather_det.setTemperature(getFloat("temp",main_obj));

        //extract wind info
        JSONObject wind_obj=getObject("wind",j_obj);
        weather_det.setWind_speed(getFloat("speed",wind_obj));
        weather_det.setWind_deg(getFloat("deg",wind_obj));

        //extract clouds info
        JSONObject cloud_obj=getObject("clouds",j_obj);
        weather_det.setClouds_perc(getInt("all",cloud_obj));

        return weather_det;
    }

    /*
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */

    @Override
    protected void onPostExecute(String weather_results){
        CurrentWeatherDetails weather_details;
        if(weather_results!=null){
            try {
                weather_details=getWeatherinfo(weather_results);

                StringBuilder location=new StringBuilder();
                UserLocationInformation u_location=weather_details.getUser_location();
                location.append(u_location.getCity());
                location.append(", ");
                location.append(u_location.getCountry());
                imv_weather_icon.setImageBitmap(weather_icon_image);
                txtv_temperature.setText(String.valueOf(weather_details.getTemperature()));
                txtv_max_temp.setText(String.valueOf(weather_details.getMax_temp()));
                txtv_min_temp.setText(String.valueOf(weather_details.getMin_temp()));
                txtv_humidity.setText(weather_details.getHumidity());
                txtv_place.setText(location);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        CurrentWeatherDetails weatherDet;

        try{
            OpenWeatherHttpClient opw_weather=new OpenWeatherHttpClient();
            weatherDet=getWeatherinfo(opw_weather.getCurrentWeatherData(lattitude,longitude));
            this.weather_icon_image=opw_weather.getImage(weatherDet.getIcon());
            return opw_weather.getCurrentWeatherData(lattitude,longitude);

        }catch (Throwable t){
            t.printStackTrace();
        }
        return null;
    }
}
