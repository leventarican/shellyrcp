package shellyrcp.domain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShellyView extends ViewPart {

    private Text shellyDataText;  // Widget to display the Shelly data

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout());

        // Create a button as before
        Button button = new Button(parent, SWT.PUSH);
        button.setText("Fetch Shelly Data");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fetchAndDisplayShellyData();
            }
        });

        // Add a Text widget to display the Shelly data
        shellyDataText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        shellyDataText.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    @Override
    public void setFocus() {
    }

    private void fetchAndDisplayShellyData() {
        try {
            String jsonData = ShellyDataExtractor.fetchShellyData();
            String displayData = extractDisplayData(jsonData);
            shellyDataText.setText(displayData);
        } catch (Exception e) {
            e.printStackTrace();
            shellyDataText.setText("Error: " + e.getMessage());
        }
    }

    private String extractDisplayData(String jsonData) {
        JSONObject data = new JSONObject(jsonData).getJSONObject("result");
        StringBuilder displayData = new StringBuilder();

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

        // Build the display data
        displayData.append("Watt Now: " + wattNow + "\n");
        displayData.append("kWh Total: " + kWhTotal + "\n");
        displayData.append("Watt Past Minutes: ");
        for (float watt : wattPastMinutes) {
            displayData.append(watt + " ");
        }
        displayData.append("\nTimestamp: " + timestamp + "\n");
        displayData.append("Temperature in Celsius: " + temp);

        return displayData.toString();
    }
}

