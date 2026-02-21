package nturbo1.http;

/**
 * Includes HTTP 1.1 general headers.
 *
 * general-header = Cache-Control
 *                | Connection
 *                | Date
 *                | Pragma
 *                | Trailer
 *                | Transfer-Encoding
 *                | Upgrade
 *                | Via
 *                | Warning
 */
public enum GeneralHeader
{
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    DATE("Date"),
    PRAGMA("Pragma"),
    TRAILER("Trailer"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    UPGRADE("Upgrade"),
    VIA("Via"),
    WARNING("Warning");

    private final String name;

    GeneralHeader(String name) { this.name = name; }

    public String getName() { return this.name; }
}