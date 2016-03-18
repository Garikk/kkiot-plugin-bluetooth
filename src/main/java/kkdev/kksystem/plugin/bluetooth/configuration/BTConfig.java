/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.bluetooth.configuration;

import kkdev.kksystem.base.classes.plugins.ExternalConfiguration;
import kkdev.kksystem.base.constants.SystemConsts;

/**
 *
 * @author blinov_is
 */
public class BTConfig  extends ExternalConfiguration {
    public enum AdapterTypes {
        BlueCove
    }
    
    public String FeatureID=SystemConsts.KK_BASE_FEATURES_BLUETOOTH_UID;
    public AdapterTypes BTAdapter;
    public ServicesConfig[] BTServicesMapping;

}
