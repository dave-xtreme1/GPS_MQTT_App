package com.example.gpsmqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {

    private static final String TAG = "MqttHelper";
    private static final String MQTT_BROKER = "tcp://test.mosquitto.org:1883"; // Change to your broker
    private static final String CLIENT_ID = MqttClient.generateClientId();
    private static final String DEFAULT_TOPIC = "gps/cord"; // Change as needed

    private MqttClient mqttClient;
    private Context context;

    public MqttHelper(Context context) {
        this.context = context;
        setupMqttClient();
    }

    private void setupMqttClient() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "MQTT Connection Lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d(TAG, "Message Received: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Message Delivery Complete");
                }
            });

            mqttClient.connect(options);
            Log.d(TAG, "MQTT Connected Successfully");

        } catch (MqttException e) {
            Log.e(TAG, "MQTT Connection Error: " + e.getMessage());
        }
    }

    public void publishMessage(String payload) {
        publishMessage(DEFAULT_TOPIC, payload);
    }

    public void publishMessage(String topic, String payload) {
        if (topic == null || topic.isEmpty()) {
            Log.e(TAG, "No topic specified, using default: " + DEFAULT_TOPIC);
            topic = DEFAULT_TOPIC;
        }

        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(1);
                mqttClient.publish(topic, message);
                Log.d(TAG, "Message Published to Topic: " + topic);
            } else {
                Log.e(TAG, "MQTT Client is not connected");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Error Publishing Message: " + e.getMessage());
        }
    }
}
