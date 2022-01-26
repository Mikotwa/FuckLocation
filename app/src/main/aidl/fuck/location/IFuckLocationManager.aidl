// IFuckLocationManager.aidl
package fuck.location;

// Declare any non-default types here with import statements

interface IFuckLocationManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean inWhiteList(String packageName);
}