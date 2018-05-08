/**MarkerArrayLayer derived from Willow Garage*/

package org.ros.android.rviz_for_android.layers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import org.ros.android.renderer.Camera;
import org.ros.android.rviz_for_android.MainActivity;
import org.ros.android.rviz_for_android.prop.ButtonProperty;
import org.ros.android.rviz_for_android.prop.LayerWithProperties;
import org.ros.android.rviz_for_android.prop.Property;
import org.ros.android.rviz_for_android.prop.ReadOnlyProperty;
import org.ros.android.rviz_for_android.urdf.ServerConnection;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.rosjava_geometry.FrameTransformTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;


import visualization_msgs.MarkerArray;

public class MarkerArrayLayer extends EditableStatusSubscriberLayer<visualization_msgs.MarkerArray> implements LayerWithProperties {

    private List<String> namespaceList = new LinkedList<String>();
    private List<String> enabledNamespaces = new LinkedList<String>();

    // Map from namespace to map from ID to marker
    private Map<String, HashMap<Integer, org.ros.android.rviz_for_android.drawable.Marker>> markers = new HashMap<String, HashMap<Integer, org.ros.android.rviz_for_android.drawable.Marker>>();

    private FrameTransformTree ftt;
    private long nextPruneTime;
    private static final long PRUNE_PERIOD = 300; // Milliseconds
    private Object lockObj = new Object();
    private final ServerConnection serverConnection;

    public MarkerArrayLayer(Camera cam, GraphName topicName) {
        super(topicName, visualization_msgs.MarkerArray._TYPE, cam);
        this.serverConnection = ServerConnection.getInstance();
        nextPruneTime = System.currentTimeMillis() + PRUNE_PERIOD;
        super.prop.addSubProperty(new ButtonProperty("Namespaces ", "Select", new Property.PropertyUpdateListener<String>() {
            @Override
            public void onPropertyChanged(String newval) {
                showNamespaceSelectDialog();
            }
        }));
    }
    @Override
    protected String getMessageFrameId(MarkerArray msg) {
        Log.i("RawMessage",msg.getMarkers().get(0).getText());
        return msg.getMarkers().get(0).getHeader().getFrameId();
    }

    @Override
    protected void onMessageReceived(visualization_msgs.MarkerArray msg) {
        super.onMessageReceived(msg);
        for(int i=0;i<msg.getMarkers().size();i++) {
            String ns = msg.getMarkers().get(i).getNs();
            int id = msg.getMarkers().get(i).getId();

            synchronized (lockObj) {
                switch (
                        msg.getMarkers().get(i).getAction()
                        ) {
                    case visualization_msgs.Marker.ADD:
                        if (!markers.containsKey(ns)) {
                            markers.put(ns, new HashMap<Integer, org.ros.android.rviz_for_android.drawable.Marker>());
                            enabledNamespaces.add(ns);
                            namespaceList.add(ns);
                        }
                        markers.get(ns).put(id, new org.ros.android.rviz_for_android.drawable.Marker(msg.getMarkers().get(i), super.camera, ftt));
                        Log.i("MarkerArrayLayer", "Adding marker " + ns + id);
                        break;
                    case visualization_msgs.Marker.DELETE:
                        Log.i("MarkerArrayLayer", "Deleting marker " + ns + ":" + id);
                        if (markers.containsKey(ns))
                            markers.get(ns).remove(id);
                        break;
                    default:
                        Log.e("MarkerArrayLayer", "Received a message with unknown action " + msg.getMarkers().get(i).getAction());
                        return;
                }
            }
        }
    }

    @Override
    public void draw(GL10 glUnused) {
        synchronized(lockObj) {
            for(String namespace : enabledNamespaces) {
                for (org.ros.android.rviz_for_android.drawable.Marker marker : markers.get(namespace).values()) {
                    marker.draw(glUnused);
                    Log.i("Drawing", "Drawing" + namespace);
                }
            }
            if(System.currentTimeMillis() >= nextPruneTime) {
                pruneMarkers();
            }
        }
    }

    private void pruneMarkers() {
        boolean error = false;

        // Prune markers which have expired
        for(HashMap<Integer, org.ros.android.rviz_for_android.drawable.Marker> hm : markers.values()) {
            List<Integer> removeIds = new LinkedList<Integer>();
            for(Integer i : hm.keySet()) {
                org.ros.android.rviz_for_android.drawable.Marker marker = hm.get(i);
                if(marker.isExpired())
                    removeIds.add(i);
                else if(marker.isError()) {
                    super.statusController.setFrameChecking(false);
                    super.statusController.setStatus("Marker " + marker.getId() + " in " + marker.getNamespace() + " is invalid", ReadOnlyProperty.StatusColor.WARN);
                    error = true;
                }
            }
            for(Integer i : removeIds) {
                hm.remove(i);
            }
        }

        if(!error && super.messageCount > 0)
            super.statusController.setFrameChecking(true);

        nextPruneTime += PRUNE_PERIOD;

    }

    @Override
    public void onStart(ConnectedNode connectedNode, Handler handler, FrameTransformTree frameTransformTree, Camera camera) {
        super.onStart(connectedNode, handler, frameTransformTree, camera);
        this.ftt = frameTransformTree;
    }

    @Override
    public Property<?> getProperties() {
        return super.prop;
    }

    @Override
    public boolean isEnabled() {
        return prop.getValue();
    }

    protected void showNamespaceSelectDialog() {
        int count = namespaceList.size();
        boolean[] selected = new boolean[count];
        CharSequence[] namespacesArray = new CharSequence[count];

        for(int i = 0; i < count; i++) {
            selected[i] = enabledNamespaces.contains(namespaceList.get(i));
            namespacesArray[i] = namespaceList.get(i);
        }

        DialogInterface.OnMultiChoiceClickListener coloursDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                synchronized(lockObj) {
                    if(isChecked)
                        enabledNamespaces.add(namespaceList.get(which));
                    else
                        enabledNamespaces.remove(namespaceList.get(which));
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(serverConnection.getContext());
        builder.setTitle("Select Namespaces");
        builder.setMultiChoiceItems(namespacesArray, selected, coloursDialogListener);
        builder.setNeutralButton("Ok", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public MainActivity.AvailableLayerType getType() {
        return MainActivity.AvailableLayerType.MarkerArray;
    }
}

