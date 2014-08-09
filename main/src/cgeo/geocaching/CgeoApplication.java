package cgeo.geocaching;

import cgeo.geocaching.sensors.DirectionProvider;
import cgeo.geocaching.sensors.GeoDataProvider;
import cgeo.geocaching.sensors.GpsStatusProvider;
import cgeo.geocaching.sensors.GpsStatusProvider.Status;
import cgeo.geocaching.sensors.IGeoData;
import cgeo.geocaching.utils.Log;
import cgeo.geocaching.utils.OOMDumpingUncaughtExceptionHandler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import rx.Observable;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;

import android.app.Application;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

public class CgeoApplication extends Application {

    private boolean forceRelog = false; // c:geo needs to log into cache providers
    public boolean showLoginToast = true; //login toast shown just once.
    private boolean liveMapHintShownInThisSession = false; // livemap hint has been shown
    private static CgeoApplication instance;
    private Observable<IGeoData> geoDataObservable;
    private Observable<Float> directionObservable;
    private Observable<Status> gpsStatusObservable;
    private volatile IGeoData currentGeo = null;
    private volatile float currentDirection = 0.0f;
    private boolean isGooglePlayServicesAvailable = false;

    public static void dumpOnOutOfMemory(final boolean enable) {

        if (enable) {

            if (!OOMDumpingUncaughtExceptionHandler.activateHandler()) {
                Log.e("OOM dumping handler not activated (either a problem occured or it was already active)");
            }
        } else {
            if (!OOMDumpingUncaughtExceptionHandler.resetToDefault()) {
                Log.e("OOM dumping handler not resetted (either a problem occured or it was not active)");
            }
        }
    }

    public CgeoApplication() {
        setInstance(this);
    }

    private static void setInstance(final CgeoApplication application) {
        instance = application;
    }

    public static CgeoApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        try {
            final ViewConfiguration config = ViewConfiguration.get(this);
            final Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ignore) {
        }
        // ensure initialization of lists
        DataStore.getLists();
        // Check if Google Play services is available
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            isGooglePlayServicesAvailable = true;
        }
        Log.i("Google Play services are " + (isGooglePlayServicesAvailable ? "" : "not ") + "available");
    }

    @Override
    public void onLowMemory() {
        Log.i("Cleaning applications cache.");
        DataStore.removeAllFromCache();
    }

    public synchronized Observable<IGeoData> geoDataObservable() {
        if (geoDataObservable == null) {
            final ConnectableObservable<IGeoData> onDemand = GeoDataProvider.create(this).replay(1);
            onDemand.subscribe(new Action1<IGeoData>() {
                                  @Override
                                  public void call(final IGeoData geoData) {
                                      currentGeo = geoData;
                                  }
                              });
            geoDataObservable = onDemand.refCount();
        }
        return geoDataObservable;
    }

    public synchronized Observable<Float> directionObservable() {
        if (directionObservable == null) {
            final ConnectableObservable<Float> onDemand = DirectionProvider.create(this).replay(1);
            onDemand.subscribe(new Action1<Float>() {
                                  @Override
                                  public void call(final Float direction) {
                                      currentDirection = direction;
                                  }
                              });
            directionObservable = onDemand.refCount();
        }
        return directionObservable;
    }

    public synchronized Observable<Status> gpsStatusObservable() {
        if (gpsStatusObservable == null) {
            final ConnectableObservable<Status> onDemand = GpsStatusProvider.create(this).replay(1);
            gpsStatusObservable = onDemand.refCount();
        }
        return gpsStatusObservable;
    }

    public IGeoData currentGeo() {
        return currentGeo != null ? currentGeo : geoDataObservable().toBlocking().first();
    }

    public float currentDirection() {
        return currentDirection;
    }

    public boolean isLiveMapHintShownInThisSession() {
        return liveMapHintShownInThisSession;
    }

    public void setLiveMapHintShownInThisSession() {
        liveMapHintShownInThisSession = true;
    }

    /**
     * Check if cgeo must relog even if already logged in.
     *
     * @return <code>true</code> if it is necessary to relog
     */
    public boolean mustRelog() {
        final boolean mustLogin = forceRelog;
        forceRelog = false;
        return mustLogin;
    }

    /**
     * Force cgeo to relog when reaching the main activity.
     */
    public void forceRelog() {
        forceRelog = true;
    }

    public boolean isGooglePlayServicesAvailable() {
        return isGooglePlayServicesAvailable;
    }

}
