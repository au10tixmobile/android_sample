# Au10tix SDK Implementation Example - Android

This example application presents an implementation suggestion for the Au10tix Mobile SDK.

## Usage

This SDK is prepared using the JWT token produced by the client's server.
Acquire the JWT token and modify Au10NetworkHelper.java to correctly include the values attained from your contact.
```java
public class Au10NetworkHelper {
    public static final String JWT_for_Bearer = "xxx.xxx.xxx";
```
