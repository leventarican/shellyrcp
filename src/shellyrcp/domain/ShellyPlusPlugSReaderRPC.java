package shellyrcp.domain;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ShellyPlusPlugSReaderRPC {



    public static void main(String[] args) {
        try {
            URL url = new URL(Shelly.RPC_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // RPC payload
            String rpcPayload = "{\"id\":1,\"method\":\"Switch.GetStatus\",\"params\":{\"id\":0}}";
            
            // Send the payload
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = rpcPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response.toString()); // Print the full JSON response
            } else {
                System.out.println("POST request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
