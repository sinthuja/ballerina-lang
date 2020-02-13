import ballerina/cache;
import ballerina/io;
import ballerina/runtime;

public function main() {
    // This creates a new cache. The cache cleanup task runs every 5 seconds
    // and clears all the expired caches. In this example, the cache expiry time
    // is set to four seconds in order to demonstrate how cache cleaning is
    // carried out.
    cache:Cache cache = new (4000);

    // Adds a new entry to the cache.
    cache.put("Name", "Ballerina");

    // Fetches the cached value.
    string name = "";
    if (cache.hasKey("Name")) {
        name = <string>cache.get("Name");
    }
    io:println("Name: " + name);

    // This sends the current worker to the sleep mode for six seconds.
    // No execution takes place during this period.
    runtime:sleep(6000);

    // The cache expires after 4 seconds. The cache cleanup task runs during the
    // fifth second and cleans the cache while this thread is in the sleep mode.
    // As a result, the value in the cache becomes null.
    if (cache.hasKey("Name")) {
        name = <string>cache.get("Name");
    } else {
        name = "";
    }
    io:println("Name: " + name);
}
