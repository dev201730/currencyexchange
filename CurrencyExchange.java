
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CurrencyExchange extends JFrame implements ListSelectionListener {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(CurrencyExchange.class.getName());
    private static HttpURLConnection conn;

    static HashMap<String, Double> ratesHashmap;
    static String[] currencyNames;

    static JFrame f;
    static JList b1, b2;
    static JTextField t1;
    static JLabel l1;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static Double conversion(HashMap<String, Double> currencyHashmap, String currencyA, String currencyB,
            String numericalValStr) {

        numericalValStr = numericalValStr.replaceAll("[^\\d.]", ""); // to remove all non-numerical values
        Double numericalVal = Double.parseDouble(numericalValStr);

        double converted_val = numericalVal;

        if (currencyA == currencyB) {
            converted_val = numericalVal;
        } else if (currencyA == "USD" || currencyB == "USD") {
            if (currencyA == "USD") {
                double exchangeToB = currencyHashmap.get(currencyB);
                converted_val = numericalVal * exchangeToB;
            } else {
                double exchangeToA = currencyHashmap.get(currencyA);
                converted_val = numericalVal / exchangeToA;
            }

        } else if (currencyA != "USD" && currencyB != "USD") {
            // eur to gbp
            // euro to usd and then usd to gbp

            double exchangeToA = currencyHashmap.get(currencyA);
            double conversion_stage1 = numericalVal / exchangeToA;

            double exchangeToB = currencyHashmap.get(currencyB);
            converted_val = conversion_stage1 * exchangeToB;

        }
        df.setRoundingMode(RoundingMode.DOWN);
        return Double.parseDouble(df.format(converted_val));
    }

    public static void currencyRatesFromAPI() {

        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        String apiJsonResponseStr = "";
        try {
            URL url = new URL("https://openexchangerates.org/api/latest.json?app_id=5d8bb691b4ce40f68e1cb06fb5198bc0");
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);

            // Test if the response from the server is successful
            int status = conn.getResponseCode();

            if (status >= 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }
            log.info("response code: " + status);
            System.out.println(responseContent.toString());
            apiJsonResponseStr = responseContent.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

        // Get info from json string apiJsonResponseStr and put into a Hashmap key-value
        // user selects usd to currency-x , calculation is performed
        ratesHashmap = new HashMap<String, Double>();

        String[] myArray = apiJsonResponseStr.split("\\{"); // System.out.println(myArray[2]);
        String[] myArray2 = myArray[2].split("\\}"); // System.out.println(myArray2[0]);
        String[] currencySets = myArray2[0].split("\\,"); // System.out.println(currencySets[0]);

        String[] currencyNames1 = {};
        ArrayList<String> currencyNamesArrayList = new ArrayList<String>();

        for (int i = 0; i < currencySets.length; i++) {
            String[] rateSet = currencySets[i].split("\\:");
            String currency_name = rateSet[0].replace('"', ' ');
            currency_name = currency_name.replaceAll("\\s", "");
            Double currency_exchangerate = Double.parseDouble(rateSet[1]);
            ratesHashmap.put(currency_name, currency_exchangerate);
            currencyNamesArrayList.add(currency_name);
        }
        System.out.println("HASHMAP : " + ratesHashmap);
        currencyNames = currencyNamesArrayList.toArray(currencyNames1); // Convert the Arraylist to array


    }

    public static void currencyExchangeWindow() {

        // create a new frame
        JFrame.setDefaultLookAndFeelDecorated(true);
        f = new JFrame("Currency Converter");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a object
        CurrencyExchange s = new CurrencyExchange();

        // create a panel
        JPanel p = new JPanel();

        // create a new label
        l1 = new JLabel();

        // Initializing the text fields with 0 by default and setting the bounds for the
        // text fields
        t1 = new JTextField("10"); // specify number of columns
        t1.setMaximumSize(new Dimension(Integer.MAX_VALUE, t1.getPreferredSize().height));

        // String array to store weekdays
        String[] currencies = currencyNames;

        // create lists
        b1 = new JList(currencies);
        b2 = new JList(currencies);

        // layout for b1,b2 lists
        b1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        b1.setLayoutOrientation(JList.VERTICAL);
        b1.setVisibleRowCount(5);
        b2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        b2.setLayoutOrientation(JList.VERTICAL);
        b2.setVisibleRowCount(5);

        // set a selected index
        b1.setSelectedIndex(1);
        b2.setSelectedIndex(2);

        l1.setText(b1.getSelectedValue() + " to " + b2.getSelectedValue());

        // add item listener
        b1.addListSelectionListener(s);
        b2.addListSelectionListener(s);

        JScrollPane scroll_b1 = new JScrollPane(b1);
        scroll_b1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll_b1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JScrollPane scroll_b2 = new JScrollPane(b2);
        scroll_b2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll_b2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // add list to panel
        BoxLayout boxlayout = new BoxLayout(p, BoxLayout.Y_AXIS);
        p.setLayout(boxlayout);
        p.add(new JLabel(" "));
        p.add(new JLabel("Numerical Value:"));
        p.add(t1);
        p.add(new JLabel("From:"));
        p.add(scroll_b1);
        p.add(new JLabel("To:"));
        p.add(scroll_b2);
        p.add(new JLabel(" "));
        p.add(l1);

        f.add(p);

        // set the size of frame
        f.setSize(500, 600);
        f.setVisible(true);

    }

    // main class
    public static void main(String[] args) {

        // to produce the Currency Rates Hashmap from API
        currencyRatesFromAPI();

        // display the currency exchange window
        currencyExchangeWindow();

    }

    public void valueChanged(ListSelectionEvent e) {
        Double conversion_val = conversion(ratesHashmap, b1.getSelectedValue().toString(),
                b2.getSelectedValue().toString(), t1.getText());

        // set the text of the label to the selected value of lists
        l1.setText(" " + Double.parseDouble(t1.getText()) + " " + b1.getSelectedValue() + " = " + conversion_val + " "
                + b2.getSelectedValue());

    }

}
