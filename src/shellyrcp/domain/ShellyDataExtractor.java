package shellyrcp.domain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class ShellyDataExtractor {

	public static String fetchShellyData() throws Exception {
	    // Define the Shelly endpoint URL
	    URL url = new URL(Shelly.RPC_ENDPOINT);
	    
	    // Prepare the HTTP POST request
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");
	    connection.setDoOutput(true);
	    
	    // JSON payload for the POST request
	    String jsonInputString = "{\"id\":1,\"method\":\"Switch.GetStatus\",\"params\":{\"id\":0}}";
	    
	    // Write the payload to the request body
	    try (OutputStream os = connection.getOutputStream()) {
	        byte[] input = jsonInputString.getBytes("utf-8");
	        os.write(input, 0, input.length);
	    }

	    // Read the response
	    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
	    StringBuilder response = new StringBuilder();
	    String responseLine;
	    while ((responseLine = reader.readLine()) != null) {
	        response.append(responseLine.trim());
	    }
	    
	    System.out.println(response);
	    
	    return response.toString();
	}
	
	public static void printSwitchStatus(String jsonData) {
		JSONObject data = new JSONObject(jsonData).getJSONObject("result");
		System.out.println(data);

		// extract and convert relevant data
		float wattNow = (float) data.getDouble("apower");
		float kWhTotal = Math.round(data.getJSONObject("aenergy").getDouble("total") / 1000 * 1000) / 1000f;

		JSONArray byMinuteArray = data.getJSONObject("aenergy").getJSONArray("by_minute");
		float[] pastMinutes = new float[byMinuteArray.length()];
		float[] wattPastMinutes = new float[byMinuteArray.length()];
		for (int i = 0; i < byMinuteArray.length(); i++) {
			pastMinutes[i] = (float) byMinuteArray.getDouble(i);
			wattPastMinutes[i] = Math.round(pastMinutes[i] * 60 / 1000 * 10) / 10f;
		}

		int timestamp = data.getJSONObject("aenergy").getInt("minute_ts");

		float temp = (float) data.getJSONObject("temperature").getDouble("tC");

		// Print results
		System.out.println("Watt Now: " + wattNow);
		System.out.println("kWh Total: " + kWhTotal);
		System.out.print("Watt Past Minutes: ");
		for (float watt : wattPastMinutes) {
			System.out.print(watt + " ");
		}
		System.out.println("\nTimestamp: " + timestamp);
		System.out.println("Temperature in Celsius: " + temp);
	}
	
	public static void main(String[] args) {
		try {
			String jsonData = fetchShellyData();
			printSwitchStatus(jsonData);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
