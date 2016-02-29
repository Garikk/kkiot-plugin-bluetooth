/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kkdev.kksystem.plugin.bluetooth.adapters.bluecove;

import java.io.IOException;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;
import javax.obex.Operation;
import javax.obex.ServerRequestHandler;
import kkdev.kksystem.plugin.bluetooth.adapters.IBTAdapter;
import kkdev.kksystem.plugin.bluetooth.configuration.PluginSettings;
import kkdev.kksystem.plugin.bluetooth.configuration.ServicesConfig;
import kkdev.kksystem.plugin.bluetooth.manager.BTManager;
import kkdev.kksystem.plugin.bluetooth.services.IBTService;
import kkdev.kksystem.plugin.bluetooth.services.IServiceCallback;
import kkdev.kksystem.plugin.bluetooth.services.rfcomm.BTServiceRFCOMM;

/**
 *
 * @author sayma_000
 */
public class BlueCove implements IBTAdapter, IServiceCallback {

    private boolean State = false;
    private List<Thread> BTServer;

    private HashMap<String, RemoteDevice> AvailableDevices;
    private List<ServicesConfig> ServicesMapping;
    private HashMap<String, IBTService> BTServices;
    private LocalDevice LD;
    private List<BTConnectionWorker> Connections;
    BTManager BTM;

    @Override
    public void StartAdapter(BTManager MyBTM) {
        BTM = MyBTM;
        AvailableDevices = new HashMap<>();
        BTServices = new HashMap<>();
        Connections = new ArrayList<>();
        //
        BTServer = new ArrayList<>();
        //
        try {
            //display local device address and name
            LD = LocalDevice.getLocalDevice();
            State = true;
            // Init Services
            InitServices();
            //
            //Init local devices
             InitLocalDevices();
             //
        } catch (BluetoothStateException ex) {
            State = false;
            out.println("[BT][ERR]" + ex.getMessage());
            out.println("[BT][ERR] Bluetooth adapter disabled");
        }
    }
    @Override
    public void RegisterService(ServicesConfig SC) {
        if (ServicesMapping==null)
            ServicesMapping=new ArrayList<>();
            
        ServicesMapping.add(SC);
    }

    private void InitServices() {
        IServiceCallback WorkerCallback = this;

        for (ServicesConfig SC : ServicesMapping) {
            System.out.println("[BT][INF] Check services " + SC.Name);
            if (SC.ServerMode) {
                Thread NS=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //UUID _uuid = new UUID("0000110100001000800000805F9B34FB", false);
                        UUID _uuid = new UUID(SC.ServicesUUID_String[0], false);

                        try {
                            System.out.println("[BT][INF] Init Service " + SC.Name);
                            System.out.println("[BT][INF] Wait connection on btspp://localhost:" + _uuid);
                            StreamConnectionNotifier serverConnection;
                            serverConnection = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + _uuid + "");
                            while (State) {
                                Connections.add(new BTConnectionWorker(WorkerCallback, "", serverConnection.acceptAndOpen()));
                                //
                                // Clean closed connections
                                List<BTConnectionWorker> CR = new ArrayList();
                                for (BTConnectionWorker CW : Connections) {
                                    if (!CW.Active) {
                                        CR.add(CW);
                                    }
                                }
                                for (BTConnectionWorker CW : CR) {
                                    Connections.remove(CW);
                                }
                                CR.clear();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(BlueCove.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("[BT][INF] STOP " + SC.Name);
                    }
                });
                BTServer.add(NS);
                NS.start();
            }
        }
    }

    private void InitLocalDevices() {
        for (ServicesConfig SC : ServicesMapping) {
            if (!SC.ServerMode) {

                if (SC.DevType == ServicesConfig.BT_ServiceType.RFCOMM) {
                    out.println("[BT][INF] SVC CONN " + SC.DevAddr);
                    IBTService Svc = null;
                    Svc = new BTServiceRFCOMM();
                    Svc.ConnectService(SC.DevAddr, "btspp://" + SC.DevAddr, this);
                } else {
                    out.println("[BT][INF] Not supported service type detected " + SC.Name);
                }
            }

        }
    }

    

    @Override
    public void StopAdaper() {
        State = false;
    }

    ServerRequestHandler ServerBTEXA = new ServerRequestHandler() {
        @Override
        public int onPut(Operation op) {
            return super.onPut(op); //To change body of generated methods, choose Tools | Templates.
        }

    };

    DiscoveryListener BTDiscovery = new DiscoveryListener() {

        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
            //add the device to the vector
            if (!AvailableDevices.containsKey(btDevice.getBluetoothAddress())) {
                AvailableDevices.put(btDevice.getBluetoothAddress(), btDevice);
            }

        }

        @Override
        public void servicesDiscovered(int i, ServiceRecord[] srs) {
            //   if (servRecord != null && servRecord.length > 0) {
            //  connectionURL = servRecord[0].getConnectionURL(0, false);
            //
     ///       ServicesConfig Conf = ServicesMapping.get(srs[0].getHostDevice().getBluetoothAddress());
      //      for (ServiceRecord SR : srs) {
      //          out.println("[BT] Found SVC: " + SR.getAttributeValue(0x0001) + " " + SR.getConnectionURL(0, false));

       //     }

            /*
                if (connectionURL != null) {
                    IBTService Svc = null;
                    if (Conf.DevType == ServicesConfig.BT_ServiceType.RFCOMM) {
                        Svc = new BTServiceRFCOMM();
                        Svc.ConnectService(connectionURL, this);
                    }

                    BTServices.put(Dev.getBluetoothAddress(), Svc);
                }
             */
        }

        @Override
        public void serviceSearchCompleted(int i, int i1) {
            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void inquiryCompleted(int i) {
          //  for (RemoteDevice RD : AvailableDevices.values()) {
          //      out.println("[BT] Found devices: " + RD.getBluetoothAddress());
          //  }

           // try {
              //  ConnectLocalDevicesAfterDiscovery();

           // } catch (BluetoothStateException ex) {
           //     Logger.getLogger(BlueCove.class
            //            .getName()).log(Level.SEVERE, null, ex);
           // }
        }
    };

    @Override
    public boolean State() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void ReceiveServiceData(String Tag, String SrcAddr, Byte[] Data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void ReceiveServiceData(String Tag, String SrcAddr, String Data) {
        BTM.BT_ReceiveData(Tag, Data);
    }

    @Override
    public void SendJsonData(String Json) {
       for (BTConnectionWorker CN:Connections)
       {
           CN.SendData(Json);
       }
    }


}

//    private void ConnectLocalDevicesAfterDiscovery() throws BluetoothStateException {

/*
        UUID[] uuidSet;
        for (RemoteDevice Dev : AvailableDevices.values()) {
            if (ServicesMapping.containsKey(Dev.getBluetoothAddress())) {
                ServicesConfig Conf = ServicesMapping.get(Dev.getBluetoothAddress());
                //
                uuidSet = new UUID[Conf.ServicesUUID_long.length + Conf.ServicesUUID_String.length];
                //
                for (int i = 0; i < Conf.ServicesUUID_String.length; i++) {
                    uuidSet[i] = new UUID(Conf.ServicesUUID_String[i], false);
                }
                for (int i = Conf.ServicesUUID_String.length; i < Conf.ServicesUUID_long.length + Conf.ServicesUUID_String.length; i++) {
                    uuidSet[i] = new UUID(Conf.ServicesUUID_long[i]);
                }
                //
                StartServicesSearch(uuidSet, Dev);

            }
        }
 */
   // }
/*
    private void StartDevicesSearch() throws BluetoothStateException {
        DiscoveryAgent agent = LD.getDiscoveryAgent();
        agent.startInquiry(DiscoveryAgent.GIAC, BTDiscovery);
    }

    private void StartServicesSearch(UUID[] uuidSet, RemoteDevice Dev) throws BluetoothStateException {
        DiscoveryAgent agent = LD.getDiscoveryAgent();
        agent.searchServices(null, uuidSet, Dev, BTDiscovery);

    }
    */