/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.socialappsharing.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationResult;

import java.util.List;

/**
 * This class refers the Location BroadcastReceiver to get the location at background
 */
public class LocationBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            StringBuilder sb = new StringBuilder();
            if (ACTION_PROCESS_LOCATION.equals(action)) {
                // Processing LocationResult information
                if (LocationResult.hasResult(intent)) {
                    LocationResult result = LocationResult.extractResult(intent);
                    if (result != null) {
                        List<Location> locations = result.getLocations();
                        if (!locations.isEmpty()) {
                            for (Location location : locations) {
                                sb.append(location.getLongitude())
                                        .append(",")
                                        .append(location.getLatitude())
                                        .append(",")
                                        .append(location.getAccuracy());
                            }
                        }
                    }
                }

                // Processing LocationAvailability information
                if (LocationAvailability.hasLocationAvailability(intent)) {
                    LocationAvailability locationAvailability =
                            LocationAvailability.extractLocationAvailability(intent);
                    if (locationAvailability != null) {
                        sb.append("[locationAvailability]:").append(locationAvailability.isLocationAvailable());
                    }
                }
            }
            if (!"".equals(sb.toString())) {
                Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

